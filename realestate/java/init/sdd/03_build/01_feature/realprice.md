# 03_build · current-state (Overwrite Rule)

> 4단계 '구현' 현재 상태입니다. 이 문서는 누적 로그가 아니라 **현재 사실**만 덮어씁니다.
> 계획 정본은 `02_plan/01_feature/realprice_todos.md`, 수용기준은 `01_planning/01_feature/realprice_ingest.md`.

## 진행 현황
| 작업 | 모듈 | 상태 | 검증 |
| --- | --- | --- | --- |
| T1 @ingestion-dev | ingestion-service | ✅ 구현 완료 | `:ingestion-service:test` PASS |
| T2 @transaction-dev | transaction-service | ✅ 구현 완료 | `:transaction-service:test` PASS |
| T3 @analytics-dev | analytics-service | ✅ 구현 완료 | `:analytics-service:test` PASS |
| T4 (common 계약) | common | ✅ 공유 계약 확정 | `:common:test` PASS |
| T4 (인프라) | discovery·config·gateway | ✅ 확정(reset 보존) | `:*:assemble` bootJar OK |

## T1 구현 내용 (수집 + 정규화 + 회복력)

### common (공유 계약, T4 소유 / T1·2·3 의존)
- `DealAmountParser.toWon(String)` — 콤마·공백 만원 문자열 → 원 단위 long. `" 82,500"` → `825,000,000`. 빈 값·비숫자는 `IllegalArgumentException`. (AC-3 정합, DAR-002)
- `AptTransaction` — 표준 거래 스키마(record). `naturalKey()`(AC-4), `pricePerSquareMeter()`(AC-5 집계용) 포함.

### ingestion-service (T1)
- `client/MolitApiClient` (`@Profile("!stub")`) — data.go.kr 상세 API 호출. **`@Retry(name="molitApi")` + `@CircuitBreaker(name="molitApi", fallbackMethod="fallback")`**, fallback은 빈 리스트로 우아한 저하(AC-2, 부분 수집 허용). XML(`XmlMapper`) → `MolitResponse` → item 평탄화.
- `client/MolitAptTradeItem` · `MolitResponse` — data.go.kr XML item/봉투 매핑(raw).
- `client/AptTradeSource` — 원천 추상화. 운영=MolitApiClient, 실습/E2E=`StubAptTradeSource`(`@Profile("stub")`, 정상4+해제1 결정적 표본).
- `domain/AptTransactionNormalizer` — raw → 표준 `AptTransaction` 정규화. **금액 변환은 common/DealAmountParser에 위임**, `cdealType == "O"` → `canceled=true`(AC-3 해제 표시). 정합 규칙은 common 한곳에서만 강제.
- `service/IngestionService` — 원천 조회 → 정규화 → `lb://transaction-service`에 멱등 bulk 적재 요청.
- `web/IngestionController` — `POST /api/v1/ingest/apt-trade?lawdCd=&dealYmd=`.

### 설정
- `application.yml` `molit.apt-trade-path`를 상세 엔드포인트(`/RTMSDataSvcAptTradeDev/getRTMSDataSvcAptTradeDev`)로 정렬(07_integration 계약 일치). 인증키는 `${MOLIT_SERVICE_KEY:}` 환경변수 참조(SECR-001).

## T2 구현 내용 (멱등 적재 + 조회)

### transaction-service (T2) — 헥사고날 포트/어댑터
- `port/AptTradeStore` — 저장 포트: `existsByNaturalKey` · `save` · `findByRegionMonth`. 영속 기술과 멱등 로직을 분리해 DB 없이 단위 검증 가능.
- `service/TransactionCommandService` — `upsertAll`이 **`existsByNaturalKey`로 중복 차단**(이미 있으면 skip), 새 거래만 저장하고 적재 건수 반환(AC-4 멱등). `common/AptTransaction` 계약을 그대로 사용.
- `adapter/AptTradeEntity` — 영속 엔티티. **자연키 유니크 제약**(`uk_apt_trade_natural`)으로 DB 차원 중복도 차단. `from()`/`toDomain()`으로 도메인 ↔ 엔티티 변환.
- `adapter/AptTradeRepository` — Spring Data JPA(`existsByNaturalKey`, `findBySggCdAndDealYearAndDealMonth`).
- `adapter/JpaAptTradeStore` — 포트의 JPA 어댑터.
- `web/TransactionController` — `POST /api/v1/transactions/bulk`(멱등 적재 건수) · `GET /api/v1/transactions`(시군구·계약월 조회).
- 저장소: H2 인메모리(`application.yml`, ddl-auto create-drop).

## T3 구현 내용 (시세 통계 read model, CQRS 읽기 측)

### analytics-service (T3)
- `domain/MarketStatCalculator` — 순수 도메인 집계. **`canceled=true` 제외(AC-3)** 후 중위 거래금액·중위 ㎡당 단가 계산(홀수=가운데, 짝수=두 가운데 평균). 거래 없으면 빈 통계. (테스트가 `domain` 패키지로 고정 — 화면의 `service/` 라벨과 달리 `domain/`에 둠)
- `domain/MarketStat` — read model record(tradeCount·medianPriceWon·medianPricePerM2Won).
- `service/MarketStatService` — `lb://transaction-service` 조회 → calculator로 집계(AC-5: write model이 아닌 read model에서 산출).
- `web/AnalyticsController` — `GET /api/v1/market-stats?sggCd=&dealYear=&dealMonth=`.
- `AnalyticsApplication` — `@LoadBalanced WebClient.Builder`. analytics→transaction 단방향(CQRS).

## T4 구현 내용 (플랫폼 인프라 — reset 보존, 확정·검증)

### 인프라 3종 (이미 shipped, reset이 비우지 않음)
- `service-discovery` — Eureka Server(`@EnableEurekaServer`), 8761.
- `config-server` — Spring Cloud Config Server, 8888. `config/ingestion-service.yml`로 molit·회복력 설정 외부화.
- `api-gateway` — **8080 단일 진입점**. 라우팅 3개: `/api/v1/ingest/**`→ingestion, `/api/v1/transactions/**`→transaction, `/api/v1/market-stats/**`→analytics. discovery locator 활성.
- T4 공유 계약 `common`은 T1 단계에서 확정(AptTransaction·DealAmountParser).
- 검증: `:api-gateway:assemble :service-discovery:assemble :config-server:assemble` bootJar 빌드 OK.

## proof (이번 실행)
```
JAVA_HOME=jdk-17 (gradle toolchain)
./gradlew test :api-gateway:assemble :service-discovery:assemble :config-server:assemble  → BUILD SUCCESSFUL
  단위 8/8:
  - DealAmountParserTest        (2): AC-3 금액 변환 / 잘못된 값 거부
  - AptTransactionNormalizerTest(2): AC-1 정규화 / AC-3 해제 표시
  - IdempotentUpsertTest        (1): AC-4 재적재 중복 0건
  - MarketStatCalculatorTest    (3): AC-5 중위 집계 / AC-3 해제 제외 / 빈 통계
  인프라 bootJar: api-gateway·service-discovery·config-server assemble OK
```
> 비고: 제공된 테스트의 한글 `@DisplayName`에서 x-windows-949 인코딩 경고가 나오지만 비치명적이며 통과에는 영향 없습니다.

## 비중첩 확인
- T1=`ingestion-service/*`, T2=`transaction-service/*`, T3=`analytics-service/*`, T4=`common/*`+인프라 3종. 서로 다른 모듈만 만져 병렬 안전.
- 도메인 3종은 `common`에만 의존(역의존 0), analytics→transaction 단방향(CQRS). 게이트웨이 8080 단일 진입.

## 종합
- **T1~T4 전부 구현 완료.** 단위 8/8 PASS + 인프라 bootJar 빌드 OK.
- 남은 게이트: `./lab.sh verify`(아키텍처 게이트 7/7 + 단위 8/8). 부팅 E2E는 `deploy_dev`(Docker)로 게이트웨이 통해 ingest→query→market-stats 스모크.
