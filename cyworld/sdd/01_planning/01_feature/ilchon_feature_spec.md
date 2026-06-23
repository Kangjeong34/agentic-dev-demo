# 일촌(관계) · Acceptance Criteria (EARS)

> 01_planning: 일촌 신청·수락·해제 요구사항을 검증 가능한 EARS로 정제. 이 명세가 가드레일.
> 일촌은 콘텐츠 공개범위(다이어리·사진첩)와 파도타기의 기준이 되는 핵심 관계 도메인이다.

**AC-1** When 사용자 A가 사용자 B에게 일촌을 신청하면, the system shall (A→B, pending)
일촌 요청 1건을 생성하고 B에게 노출한다.

**AC-2** While 요청이 pending일 때, when B가 신청을 수락하면, the system shall A와 B 사이에
양방향(accepted) 일촌 관계를 성립시킨다.

**AC-3** While 요청이 pending일 때, when B가 신청을 거절하면, the system shall 요청을 제거하고
관계를 성립시키지 않는다.

**AC-4** When 이미 일촌이거나 동일 방향 요청이 진행 중인 상태에서 다시 신청하면, the system shall
멱등성을 보장해 중복 요청·중복 관계를 생성하지 않는다.

**AC-5** When 어느 한쪽이 일촌을 끊으면, the system shall 양방향 관계를 동시에 해제한다.

**AC-6** When 일촌 성립 시 A와 B가 서로의 일촌명(호칭)과 일촌평을 등록하면, the system shall
관계에 묶어 저장하고 양쪽 미니홈피에 노출한다.

## 검증 매핑

| AC | 테스트 |
| --- | --- |
| AC-1 | `IlchonServiceTest::request_creates_pending` |
| AC-2 | `IlchonServiceTest::accept_makes_bidirectional` |
| AC-3 | `IlchonServiceTest::reject_removes_request` |
| AC-4 | `IlchonServiceTest::duplicate_request_idempotent` |
| AC-5 | `IlchonServiceTest::break_removes_both_sides` |
| AC-6 | `IlchonServiceTest::nickname_and_review_stored` |
| 회귀 | `RegressionTest`(공개범위가 일촌 상태에 의존) |

## Residual Risk

- 단방향 차단(블록) 정책은 1차 범위 밖 — 추후 보강.
- 일촌 신청 알림(푸시/메일) 발송은 미정의(데모는 인앱 노출만).
