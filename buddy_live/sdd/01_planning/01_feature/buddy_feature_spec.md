# 버디(친구추가/관계/접속상태) · Acceptance Criteria (EARS)

> 01_planning: 버디 신청·수락·거절·삭제 + 접속상태 요구사항을 검증 가능한 EARS로 정제. 이 명세가 가드레일.
> 버디는 대화 가능 여부(친구만 대화)의 기준이 되는 핵심 관계 도메인이다.

**AC-1** When 사용자 A가 사용자 B에게 버디 추가를 신청하면, the system shall (A→B, pending)
버디 요청 1건을 생성하고 B에게 노출한다.

**AC-2** While 요청이 pending일 때, when B가 신청을 수락하면, the system shall A와 B 사이에
양방향(accepted) 버디 관계를 성립시킨다.

**AC-3** While 요청이 pending일 때, when B가 신청을 거절하면, the system shall 요청을 제거하고
관계를 성립시키지 않는다.

**AC-4** When 이미 버디이거나 동일 방향 요청이 진행 중인 상태에서 다시 신청하면, the system shall
멱등성을 보장해 중복 요청·중복 관계를 생성하지 않는다.

**AC-5** When 어느 한쪽이 버디를 삭제하면, the system shall 양방향 관계를 동시에 해제한다.

**AC-6** When 사용자가 접속/종료하면, the system shall 그 사용자의 접속상태(online/offline)를
갱신하고 버디 목록 조회 시 상태와 함께 노출한다.

## 검증 매핑

| AC | 테스트 |
| --- | --- |
| AC-1 | `buddy.test.js::request_creates_pending` |
| AC-2 | `buddy.test.js::accept_makes_bidirectional` |
| AC-3 | `buddy.test.js::reject_removes_request` |
| AC-4 | `buddy.test.js::duplicate_request_idempotent` |
| AC-5 | `buddy.test.js::remove_breaks_both_sides` |
| AC-6 | `buddy.test.js::presence_reflected_in_buddy_list` |
| 회귀 | 대화 가능 여부가 버디 상태에 의존(`chat.test.js::send_rejected_when_not_buddy`) |

## Residual Risk

- 단방향 차단(블록)·신고 정책은 1차 범위 밖.
- presence는 단일 프로세스/탭 신호 가정. 서버 권위 부재(데모는 이벤트 복제).
