# 02_plan · 비중첩 작업 분할 (병렬 에이전트)

> 3단계 '플랜' 산출물입니다. `03_architecture`의 경계(수집·거래원장·분석·플랫폼)를 따라 작업을 나눕니다.
> 비중첩이 핵심입니다. 모듈이 안 겹쳐야 네 에이전트를 동시에 돌려도 충돌이 없습니다.

```
[ ] T1 @ingestion-dev    data.go.kr 수집 + 전량 페이징 + 정규화 + 회복력   (AC-1·AC-2·AC-3 변환)
[ ] T2 @transaction-dev  멱등 적재(자연키 upsert) + 원본 조회 API           (AC-4)
[ ] T3 @analytics-dev    시세 통계 read model(중위가격·㎡단가, 해제 제외)    (AC-5·AC-3)
[ ] T4 @platform-dev     공유 계약(common) + 디스커버리·게이트웨이·Config    (AC-R 라우팅 / 계약 소유)
```

## 비중첩 경계 (서로 다른 모듈만 만짐 → 병렬 안전)
```
T1 → ingestion-service/*       (MolitApiClient·AptTransactionNormalizer)
T2 → transaction-service/*     (포트/어댑터·멱등 upsert 커맨드·JPA)
T3 → analytics-service/*       (MarketStatCalculator·조회 클라이언트)
T4 → common/* · service-discovery/* · config-server/* · api-gateway/*
```

## cross-cutting (공유 계약): common 소유자 = T4 @platform-dev
- `common`은 `AptTransaction`(표준 DTO)와 `DealAmountParser`(정합 규칙: 만원→원, 문자열→숫자, 해제코드→boolean, `YY.MM.DD`→날짜)를 보유합니다. T1·T2·T3가 모두 의존합니다.
- **소유자를 T4(@platform-dev)로 둡니다.** 근거: common은 세 도메인이 공유하는 계약이므로, 특정 도메인 서비스(T1 등)가 소유하면 그 도메인 편향으로 계약이 흔들립니다. 인프라·cross-cutting을 맡는 중립 소유자(T4)가 표준 스키마·정합 규칙을 한곳에 둡니다(03_architecture 판단 3, 16강 4-1 deck과 일치).
- **계약-우선(contract-first):** T4가 `common` 계약을 먼저 확정·고정한 뒤 T1·T2·T3가 그 계약에 맞춰 병렬 구현합니다. 계약 변경이 필요하면 세 도메인 에이전트가 합의 후 T4가 반영합니다.
- 정합 규칙(AC-3)을 common 한 곳에 두면 어느 모듈에서 적재·집계해도 같은 규칙이 강제되어 수요자별 집계 편차가 사라집니다.

## 의존 방향 규칙 (verify 게이트)
- 도메인 3종(T1·T2·T3)은 `common`에만 의존하고, `common`은 어떤 도메인도 역참조하지 않습니다(common 역의존 0).
- analytics(T3)→transaction(T2) 단방향 조회. transaction은 analytics를 모릅니다(CQRS).
