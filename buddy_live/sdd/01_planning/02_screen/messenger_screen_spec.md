# 메신저 화면(버디목록 + 대화창) · Screen Spec

> 01_planning: 버디버디 메신저 단일 화면. 좌측 버디 목록, 우측 1:1 대화창.
> 로컬에서 두 브라우저 탭이 각각 한 유저가 되어 친구추가·대화한다(탭 간 `BroadcastChannel` 복제).

## 레이아웃 (SCR)

```
┌───────────────────────────────────────────────┐
│  buddy live · {나}                    ● 접속중  │
├──────────────────┬────────────────────────────┤
│ 버디 추가 [____][+]│  영희 와의 대화             │
│ ── 버디 ──        │  ┌──────────────────────┐   │
│ ● 영희      [2]   │  │ 영희: 안녕!           │   │
│ ○ 민수            │  │            나: ㅎㅇ │   │
│ ── 받은 요청 ──   │  └──────────────────────┘   │
│ 철수  [수락][거절]│  [메시지 입력________][전송] │
└──────────────────┴────────────────────────────┘
```

## 화면 요구사항

**SCR-1** 버디 목록은 각 버디의 접속상태(● online / ○ offline)와 안읽음 배지(>0일 때만)를 함께 렌더한다.
(← buddy AC-6, chat AC-1)

**SCR-2** 받은 버디 요청(pending)은 [수락]/[거절] 액션과 함께 노출된다. (← buddy AC-1·AC-2·AC-3)

**SCR-3** 대화창은 현재 선택된 버디와의 메시지를 시간순 버블로 렌더하고, 내 메시지는 우측 정렬한다.
버디가 아닌 상대는 선택·대화할 수 없다. (← chat AC-2·AC-4)

**SCR-4** 대화창을 연 버디의 안읽음 배지는 0으로 사라진다(읽음 처리). (← chat AC-3)

## 검증 매핑

| SCR | 테스트 |
| --- | --- |
| SCR-1 | `render.test.js::buddy_list_shows_presence_and_unread` |
| SCR-3 | `render.test.js::chat_window_orders_and_aligns_messages` |
| 라이브 복제 | `transport.test.js::two_tabs_converge`, `end_to_end_add_and_chat` |

## 렌더 결정성 (parity)

화면은 순수 함수 `renderMessenger(state, viewerId) -> HTML`로 만든다. 실제 `messenger.html`도 동일 함수를
쓴다 → 스냅샷 대조로 화면-도메인 일치를 검증한다(cyworld의 MinihompyRenderer parity와 동일 패턴).

## Residual Risk

- 브라우저 라이브 동기화(`BroadcastChannel`)는 same-origin 필요 → `file://` 직접 열기 대신
  `node tools/serve.js`(http://localhost)로 서빙해야 두 탭이 통신한다. 검증은 도메인/transport 단위로 대체.
