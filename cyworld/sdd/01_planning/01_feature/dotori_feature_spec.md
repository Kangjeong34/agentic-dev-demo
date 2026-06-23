# 도토리(결제) · Acceptance Criteria (EARS)

> 01_planning: 도토리 충전·차감·잔액·멱등 결제 요구사항을 EARS로 정제.
> auth 데모의 OTP 멱등성과 동일한 가드레일 패턴: **결제는 멱등 키로 중복 차감을 막는다.**
> 도토리는 미니룸/BGM(`miniroom_feature_spec.md`) 구매의 결제 수단이다.

**AC-1** When 사용자가 도토리 충전을 요청하면, the system shall (paymentKey)에 묶어 요청 수량만큼
잔액을 증가시키고 거래 내역 1건을 기록한다.

**AC-2** When 사용자가 아이템 구매로 도토리 차감을 요청하면, the system shall 잔액에서 가격만큼
차감하고 거래 내역 1건을 기록한다.

**AC-3** When 잔액보다 큰 금액의 차감을 요청하면, the system shall 잔액 부족으로 거래를 거부하고
잔액을 변경하지 않는다.

**AC-4** When 동일 (paymentKey)로 충전·결제가 재요청되면, the system shall 멱등성을 보장해
잔액을 두 번 변경하지 않고 최초 결과를 반환한다.

**AC-5** While 거래가 처리되는 동안, the system shall 잔액 일관성을 보장해
음수 잔액이 발생하지 않게 한다.

## 검증 매핑

| AC | 테스트 |
| --- | --- |
| AC-1 | `DotoriServiceTest::charge_increases_balance` |
| AC-2 | `DotoriServiceTest::spend_decreases_balance` |
| AC-3 | `DotoriServiceTest::spend_rejected_when_insufficient` |
| AC-4 | `DotoriServiceTest::duplicate_paymentKey_idempotent` |
| AC-5 | `DotoriServiceTest::balance_never_negative` |
| 회귀 | 미니룸 구매가 도토리 차감 흐름을 재사용 |

## Residual Risk

- 실제 PG(결제대행) 연동은 데모 범위 밖 — 충전은 내부 paymentKey 기반 모의 처리.
- 환불·부분취소 정책은 1차 범위 밖 — 추후 보강.
- 동시성(같은 계정 병렬 차감)은 단일 노드 락 가정 — 분산 환경은 비기능 요구로 분리.
