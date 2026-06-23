# 02_plan · 작업 계획

> 3단계 '플랜' 산출물입니다. 모듈 의존·런타임 흐름·proof 게이트를 확정합니다.

## 모듈 의존 그래프
```
common(T4) ──┬─→ ingestion-service(T1) ──(WebClient)──→ transaction-service(T2)
             ├─→ transaction-service(T2)
             └─→ analytics-service(T3) ──(WebClient, lb)──→ transaction-service(T2)

service-discovery(T4, Eureka) ← 모든 서비스 등록
config-server(T4)             ← ingestion 설정·인증키 외부화
api-gateway(T4)               ← 단일 진입점 (ingest·transactions·market-stats 라우팅)
```
> common은 T4(@platform-dev) 소유의 공유 계약입니다. 역의존 0 — common은 도메인을 참조하지 않습니다.

## 런타임 흐름 (한 기능 end-to-end)
```
POST /api/v1/ingest/apt-trade?lawdCd=11110&dealYmd=202405
  → api-gateway → ingestion-service
      → MolitApiClient.fetchAptTrades  (data.go.kr 상세 API, 전량 페이징, 재시도·서킷)
      → AptTransactionNormalizer       (common: 금액 변환·해제 표시 — AC-3)
      → POST lb://transaction-service /api/v1/transactions/bulk  (멱등 upsert — AC-4)

GET /api/v1/market-stats?sggCd=11110&dealYear=2024&dealMonth=5
  → api-gateway → analytics-service
      → GET lb://transaction-service /api/v1/transactions
      → MarketStatCalculator           (해제 제외·중위 집계 — AC-5·AC-3)
```

## proof 게이트 (02_plan acceptance)
| AC | 테스트 | 소유 |
| --- | --- | --- |
| AC-1·AC-3 | `AptTransactionNormalizerTest` | T1 |
| AC-3 | `DealAmountParserTest` | T4(common) |
| AC-4 | `IdempotentUpsertTest` | T2 |
| AC-5·AC-3 | `MarketStatCalculatorTest` | T3 |
| 전체 | `./gradlew test` exit 0 + 아키텍처 게이트(7/7) | — |

> 게이트는 "봤다"가 아니라 "통과시켰다"입니다. 단위 테스트는 외부 data.go.kr 호출 없이 결정적으로 AC-3 금액/해제·AC-4 멱등·AC-5 중위 집계를 검증합니다.
