# 대화(1:1 메시지/읽음/멱등) · Acceptance Criteria (EARS)

> 01_planning: 버디 사이 1:1 대화의 송신·수신·읽음·멱등 요구사항을 검증 가능한 EARS로 정제.
> 대화는 버디(accepted) 관계에 의존한다(친구만 대화) — 버디 상태 변화가 전송 가능 여부에 즉시 반영된다.

**AC-1** When 버디인 A가 B에게 메시지를 보내면, the system shall A·B의 1:1 대화방에 메시지를 시간순으로
추가하고 B의 안읽음(unread) 카운트를 1 증가시킨다.

**AC-2** When 버디가 아닌 상대에게 메시지를 보내면, the system shall 전송을 거부하고 대화방·카운트를
변경하지 않는다(친구만 대화).

**AC-3** When 사용자가 상대와의 대화방을 열어 읽으면, the system shall 그 대화방의 안읽음 카운트를
0으로 만든다.

**AC-4** When 같은 두 사람의 대화 내역을 양쪽에서 조회하면, the system shall 동일한 단일 대화방의
동일한 순서(시간순)를 반환한다.

**AC-5** When 동일한 clientMessageId로 메시지를 재전송하면, the system shall 멱등성을 보장해
중복 저장하지 않는다(네트워크/탭 재시도 가드).

**AC-6** When 빈 문자열 또는 공백만 있는 메시지를 보내면, the system shall 전송을 거부한다.

## 검증 매핑

| AC | 테스트 |
| --- | --- |
| AC-1 | `chat.test.js::send_appends_and_increments_unread` |
| AC-2 | `chat.test.js::send_rejected_when_not_buddy` |
| AC-3 | `chat.test.js::read_clears_unread` |
| AC-4 | `chat.test.js::history_same_order_both_sides` |
| AC-5 | `chat.test.js::duplicate_message_id_idempotent` |
| AC-6 | `chat.test.js::blank_message_rejected` |
| 회귀 | 버디 상태 의존(AC-2) · 탭 복제 수렴(`transport.test.js`) |

## Residual Risk

- 그룹채팅·파일전송·이모티콘은 1차 범위 밖(1:1 텍스트만).
- 메시지 순서는 송신측 타임스탬프 가정. 동시 전송의 전역 전순서(total order)는 미보장(데모).
