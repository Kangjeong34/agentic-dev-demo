# 회귀 검증 범위 (Regression Scope)

> 02_plan: 변경의 직접 대상과 상류/하류/공유 표면을 선택하고 근거를 남긴다. (verify 중점 데모)

## 직접 대상 (direct)

버디·대화 도메인의 전 AC 케이스 (`buddy.test.js`, `chat.test.js`).

## 공유/연계 표면 (shared)

| 연계 | 위험 | 검증 케이스 |
| --- | --- | --- |
| 대화 전송 ← 버디 상태 | 버디가 아니면 전송 거부, 삭제 시 대화 차단되어야 함 | `chat.test.js::send_rejected_when_not_buddy` |
| 화면 렌더 ← 도메인 상태 | 버디목록/대화창이 상태와 정확히 일치해야 함 | `render.test.js::*` |
| 탭 복제 수렴 ← 동일 리듀서 | 두 탭이 같은 이벤트 로그로 동일 상태에 수렴해야 함("두 유저 대화") | `transport.test.js::two_tabs_converge`, `end_to_end_add_and_chat` |

## 제외 (justified exclusions)

- 브라우저 DOM/`BroadcastChannel` 실연: same-origin 서버 필요 → 도메인/transport 단위 테스트로 등가 검증.
- 영속 DB/스키마: 이벤트 로그(localStorage/in-memory)라 해당 없음.
- 그룹채팅·파일전송: 범위 밖.

## 결과

전 범위 PASS는 `sdd/04_verify/01_feature/buddy_live.md`에 command-level 증거로 기록.
