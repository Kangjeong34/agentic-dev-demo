# 버디버디(BuddyLive) · Build Summary (current state)

> 03_build: 현재 구현 상태 요약(사실·현재형). 계획은 `01_planning`, 검증은 `04_verify` 참조.

## 구현 범위

버디버디 메신저를 **순수 이벤트 리듀서** 도메인부터 라이브 2-탭 화면까지 구현. 외부 의존 없음(Node 24 + 브라우저 내장 API만).

| 영역 | 주요 모듈 | 매핑 AC |
| --- | --- | --- |
| 버디(관계/접속) | `src/domain/buddy.js` | buddy AC-1~6 |
| 대화(1:1/읽음/멱등) | `src/domain/chat.js` | chat AC-1~6 |
| 도메인 진입점/셀렉터 | `src/domain/index.js` | 합성 셀렉터(buddyList·incomingRequests…) |
| 순수 렌더 | `src/web/render.js` | SCR-1·SCR-3 |
| 라이브 복제 | `src/web/transport.js` | 탭 수렴(두 유저 대화) |
| 브라우저 글루 | `src/web/app.js` | BroadcastChannel + localStorage |
| 정적 서버 | `tools/serve.js` | same-origin 2-탭 |

## 핵심 설계 결정 (verify 중점)

- **이벤트 리듀서(`reduce(state, event) -> state`)**: 상태를 직접 변이하지 않고 이벤트를 접는다.
  같은 이벤트 로그 → 같은 상태가 보장되므로 "두 탭이 동일 상태로 수렴"을 단위 테스트로 증명할 수 있다.
- **커맨드/리듀서 2단 가드**: 커맨드 빌더(`requestBuddy`, `sendMessage`…)가 현재 상태 기준 유효성을 판정해
  이벤트(또는 null)를 만들고, `reduce` 가 **동일 가드를 방어적으로 재적용**한다. 복제·재전송 이벤트가
  들어와도 멱등·불변식이 유지된다(buddy AC-4 / chat AC-2·AC-5·AC-6).
- **단일 대화방 정규화(`keys.js`)**: 두 사람의 방·관계를 순서무관 단일 키로 정규화 → 양쪽 조회가 동일
  단일 방·동일 순서(chat AC-4).
- **친구만 대화(도메인 의존)**: `chat.js` 가 `buddy.js::isBuddy` 에 의존 → 버디 해제 시 전송 즉시 차단(chat AC-2).
- **순수 렌더 parity**: `renderMessenger(state, viewer, selected)` 는 부수효과 없는 순수 함수. 브라우저와
  테스트가 같은 함수를 써서 스냅샷(`ui_parity/messenger.html`)과 대조 → 화면-도메인 일치 검증.
- **탭 복제 = 이벤트 로그 복제**: `transport.js` 의 버스가 모든 클라이언트에 같은 순서로 이벤트를 전달하고
  각자 reduce. 브라우저 버스(`app.js::BroadcastBus`)는 BroadcastChannel 로 탭에 복제 + localStorage 영속.

## 빌드/실행 방법

- proof 게이트: `node tools/run_proof.js` → `tmp/proof-results.json` (17/17 PASS).
- 구조 게이트: `node sdd/99_toolchain/01_automation/run_arch_check.js` (도메인→화면 의존 0).
- 증빙 생성: `node sdd/99_toolchain/01_automation/gen_proof_evidence.js`.
- 화면 라이브 실행: `node tools/serve.js` → 두 탭을 `?me=alice` / `?me=bob` 로 열어 친구추가·대화.

## 자동화 게이트 (toolchain)

| 게이트 | 명령 | 산출물 |
| --- | --- | --- |
| proof 테스트 | `node tools/run_proof.js` | `tmp/proof-results.json` (17/17 PASS) |
| 도메인 경계 | `run_arch_check.js` | 도메인→화면 위반 0 · 외부 의존 0 |
| proof 증빙 생성 | `gen_proof_evidence.js` | `04_verify/10_test/proof_evidence.md` |

## 미구현 (현재 범위 밖)

- 그룹채팅·파일전송·이모티콘(1:1 텍스트만), 차단/신고, 서버 권위(메시지 전역 전순서).
- 영속 DB(이벤트 로그는 localStorage/in-memory).
