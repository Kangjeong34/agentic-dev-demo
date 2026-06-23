# 메신저 화면 · Verification Summary

> 04_verify: 화면 검증 상태. 도메인 parity(결정적 스냅샷)로 화면-도메인 일치를 증명한다.

## 검증 방식

브라우저 비가용/비결정 요소(레이아웃 픽셀)를 피하고, **순수 렌더 함수 출력의 결정적 스냅샷 대조**로
화면을 검증한다(cyworld MinihompyRenderer parity 와 동일 패턴).

- `render.test.js::buddy_list_shows_presence_and_unread` — 접속 점(●/○)·안읽음 배지·받은요청 액션 단언(SCR-1·SCR-2).
- `render.test.js::chat_window_orders_and_aligns_messages` — 메시지 시간순 + `msg me`/`msg them` 정렬 단언(SCR-3),
  그리고 `renderMessenger` 출력이 커밋 스냅샷 `ui_parity/messenger.html` 과 **정확히 일치**함을 단언.

## SCR ↔ 테스트 매핑

| SCR | 테스트 | 결과 |
| --- | --- | --- |
| SCR-1 접속·안읽음 | `render.test.js::buddy_list_shows_presence_and_unread` | PASS |
| SCR-2 요청 액션 | 〃 (동일 케이스에서 accept/reject 단언) | PASS |
| SCR-3 정렬·me/them | `render.test.js::chat_window_orders_and_aligns_messages` | PASS |
| 라이브 복제 | `transport.test.js`, `e2e.test.js` | PASS |

## 결정성 핀

- 스냅샷 `sdd/04_verify/10_test/ui_parity/messenger.html` 은 `buddy_live/.gitattributes` 에서 `eol=lf` 로 고정.
  렌더러 출력과 OS 무관하게 일치하도록 보장(거짓 FAIL 방지).
- 스냅샷은 `tests/fixtures.js::canonicalState()` 한 상태에서 생성 — 재생성도 결정적.

## Residual Risk

- 실제 브라우저 DOM 렌더/CSS 시각 회귀는 자동 게이트 밖(스냅샷은 HTML 구조까지만 보장).
- 라이브 2-탭 통신은 `node tools/serve.js` 수기 확인 경로로 남김(same-origin 필요).
