# 방명록 · Acceptance Criteria (EARS)

> 01_planning: 방명록 작성·비밀글·삭제 권한 요구사항을 EARS로 정제.

**AC-1** When 방문자가 방명록에 글을 남기면, the system shall 작성자·내용·작성시각을 묶어
주인의 방명록에 1건 저장한다.

**AC-2** When 방문자가 비밀글로 작성하면, the system shall 주인과 작성자에게만 내용을 노출하고
그 외에게는 비밀글로 숨긴다.

**AC-3** When 미니홈피 주인이 임의의 방명록 글을 삭제하면, the system shall 해당 글을 제거한다.

**AC-4** When 작성자가 자신이 남긴 글을 삭제하면, the system shall 해당 글을 제거한다.

**AC-5** When 주인도 작성자도 아닌 사용자가 글을 삭제하려 하면, the system shall 권한 없음으로 거부한다.

## 검증 매핑

| AC | 테스트 |
| --- | --- |
| AC-1 | `GuestbookServiceTest::leave_entry` |
| AC-2 | `GuestbookServiceTest::secret_visible_to_owner_and_author` |
| AC-3 | `GuestbookServiceTest::owner_can_delete_any` |
| AC-4 | `GuestbookServiceTest::author_can_delete_own` |
| AC-5 | `GuestbookServiceTest::stranger_cannot_delete` |

## Residual Risk

- 도배/스팸 방지(rate limit)는 비기능 요구로 분리 — 1차 범위 밖.
- 방명록 글에 대한 답글(주인 코멘트)은 추후 보강.
