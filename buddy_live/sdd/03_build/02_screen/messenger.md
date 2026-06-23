# 메신저 화면 · Build Summary (current state)

> 03_build: 메신저 화면 구현 상태. 명세는 `01_planning/02_screen/messenger_screen_spec.md`.

## 구현물

| 파일 | 역할 |
| --- | --- |
| `src/web/render.js` | 순수 `renderMessenger(state, viewer, selected) -> HTML` |
| `src/web/messenger.html` | 단일 화면(좌: 버디목록/요청, 우: 대화창) + CSS |
| `src/web/app.js` | 도메인/렌더/transport 연결, BroadcastChannel·localStorage, 이벤트 위임 |
| `tools/serve.js` | same-origin 정적 서버(두 탭 통신 전제) |

## 화면 요구 매핑

- **SCR-1** 버디 항목 = 접속 점(● online / ○ offline) + 이름 + 안읽음 배지(>0일 때만). → `renderBuddyItem`.
- **SCR-2** 받은 요청(pending)에 [수락]/[거절] 버튼(`data-action`). → `renderRequestItem`.
- **SCR-3** 대화 버블 시간순, 내 메시지 우측(`msg me`)·상대 좌측(`msg them`). → `renderMessages`.
- **SCR-4** 대화창 열기 = `open` 액션 → `client.readRoom(other)` 로 안읽음 0(읽음 처리).

## 라이브 동작 (두 유저, 로컬)

1. `node tools/serve.js` 로 http 서빙.
2. 탭1 `?me=alice`, 탭2 `?me=bob` 로 접속(각 탭이 한 유저, 접속상태 online).
3. alice 가 "버디 추가(id=bob)" → bob 탭에 받은 요청 노출 → bob [수락] → 양 탭 버디 목록에 서로 등장.
4. 서로 메시지 입력 → 같은 대화방에 시간순으로 양 탭 동시 갱신, 안읽음 배지/읽음 처리 반영.

동기화 원리: 각 액션은 이벤트가 되어 BroadcastChannel 로 다른 탭에 복제되고 localStorage 에 영속된다.
두 탭은 같은 이벤트 로그를 reduce 하므로 동일 상태로 수렴한다(= 같은 화면).

## 결정성 / parity

화면은 순수 함수라 입력 상태가 같으면 출력 HTML 이 같다. 커밋된 스냅샷
`sdd/04_verify/10_test/ui_parity/messenger.html` 과 `render.test.js` 가 정확히 대조한다.
`.gitattributes` 가 스냅샷 EOL 을 LF 로 고정해 OS 무관 결정성을 보장한다.

## 미구현

- 타임스탬프/날짜 구분선, 타이핑 표시, 이모티콘 — 범위 밖.
