# API 계약 (05_api)

> 2단계 산출물입니다. 게이트웨이 라우팅과 서비스 엔드포인트 계약을 정의합니다.
> 모든 내부 API는 게이트웨이(Spring Cloud Gateway)를 단일 진입점으로 노출합니다(SIR-003). 서비스 간 호출은 디스커버리 lb(SIR-004)로 합니다.

## 게이트웨이 라우팅
| 경로 | 대상 서비스 |
| --- | --- |
| `/api/v1/ingest/**` | ingestion-service |
| `/api/v1/transactions/**` | transaction-service |
| `/api/v1/market-stats/**` | analytics-service |

## ingestion-service (AC-1·2·3)
```
POST /api/v1/ingest/apt-trade?lawdCd={5자리}&dealYmd={YYYYMM}
→ 200 {"lawdCd":"11110","dealYmd":"202405","upserted":143}
```
- data.go.kr 상세 API를 전량 페이징 호출 → XML 파싱 → 표준 스키마 정규화 → transaction에 멱등 적재.
- 외부 지연·실패 시 재시도→서킷→빈 결과로 저하하고 부분 성공분은 적재(AC-2). 단건 수동 트리거이며, 배치는 (시군구×계약월)로 구동(SFR-010).

## transaction-service (AC-4)
```
POST /api/v1/transactions/bulk      body: AptTransaction[]
→ 200 {"upserted": <신규 적재 건수>}     # 자연키 멱등: 재수집 시 중복 0

GET  /api/v1/transactions?sggCd={}&dealYear={}&dealMonth={}
→ 200 AptTransaction[]                    # 해제 거래 포함(원장 보존), canceled 플래그로 식별
```

## analytics-service (AC-5)
```
GET  /api/v1/market-stats?sggCd={}&dealYear={}&dealMonth={}
→ 200 {"sggCd":"11110","dealYear":2024,"dealMonth":5,
       "tradeCount":141,"medianPriceWon":825000000,"medianPricePerM2Won":9709309}
```
- 조회는 거래원장이 아니라 analytics read model을 통합니다(AC-5). 집계는 해제 거래를 제외합니다(AC-3).
- transaction 조회 결과로 read model을 산출하며, analytics→transaction 단방향 의존입니다(03_architecture).

## 공통
- 회귀 불변(AC-R): 게이트웨이 라우팅·디스커버리 등록·기존 조회 계약은 무손상이어야 합니다.
- 인증키 등 비밀값은 어떤 응답·에러 본문에도 노출하지 않습니다(09_security).
