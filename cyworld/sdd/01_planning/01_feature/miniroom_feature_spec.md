# 미니룸/BGM · Acceptance Criteria (EARS)

> 01_planning: 미니룸 아이템·BGM 구매·배치 요구사항을 EARS로 정제.
> 구매는 도토리(`dotori_feature_spec.md`) 차감 흐름을 재사용한다.

**AC-1** When 사용자가 미니룸 아이템(또는 BGM)을 구매하면, the system shall 도토리 차감(결제)을
수행하고 성공 시 해당 아이템을 보유 목록에 추가한다.

**AC-2** When 도토리 잔액이 부족하면, the system shall 구매를 거부하고 아이템을 지급하지 않는다.
(← dotori AC-3)

**AC-3** When 사용자가 보유 아이템을 미니룸에 배치하고 저장하면, the system shall 배치 상태를
저장하고 미니홈피 메인에 반영한다.

**AC-4** When 사용자가 보유하지 않은 아이템을 배치하려 하면, the system shall 거부한다.

**AC-5** When 사용자가 보유 BGM 1곡을 대표곡으로 지정하면, the system shall 미니홈피 진입 시
재생될 BGM으로 저장한다.

## 검증 매핑

| AC | 테스트 |
| --- | --- |
| AC-1 | `MiniroomServiceTest::buy_charges_dotori_and_grants` |
| AC-2 | `MiniroomServiceTest::buy_rejected_when_insufficient` |
| AC-3 | `MiniroomServiceTest::place_and_save_layout` |
| AC-4 | `MiniroomServiceTest::cannot_place_unowned` |
| AC-5 | `MiniroomServiceTest::set_representative_bgm` |
| 회귀 | 도토리 차감 멱등성(중복 구매 클릭) |

## Residual Risk

- 아이템 카탈로그(상점)는 고정 시드 데이터 가정 — 운영 입점/할인은 범위 밖.
- 미니미(아바타) 커스터마이징은 별도 명세로 분리 예정.
