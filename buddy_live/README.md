# buddy live — 버디버디 (SDD · verify 중점)

순수 이벤트 리듀서로 만든 1:1 메신저 데모. 로컬 두 탭이 각각 한 유저가 되어 **친구추가·대화**한다.
무의존(Node 24 + 브라우저 내장 API만).

## 빠른 시작 — 두 유저로 대화

```bash
node tools/serve.js
```

브라우저 두 탭을 연다:

- 탭1: <http://localhost:5173/?me=alice&name=앨리스>
- 탭2: <http://localhost:5173/?me=bob&name=밥>

alice 탭에서 "버디 추가"에 `bob` 입력 → bob 탭의 **받은 요청**에서 [수락] → 서로 버디.
이제 양쪽에서 메시지를 주고받으면 두 탭이 실시간으로 같이 갱신된다(BroadcastChannel + localStorage).
초기화는 브라우저 콘솔에서 `localStorage.clear()`.

## 검증 (verify 중점)

```bash
node tools/run_proof.js                                    # 17/17 PASS → tmp/proof-results.json
node sdd/99_toolchain/01_automation/run_arch_check.js      # 도메인→화면 경계 PASS
node sdd/99_toolchain/01_automation/gen_proof_evidence.js  # 증거 문서 생성
```

`npm test` 는 `node --test` 와 동일.

## 구조

```
src/domain/   순수 이벤트 리듀서 + 셀렉터 + 커맨드 (buddy, chat, keys, index)
src/web/      renderMessenger(순수) · transport(복제) · app.js(브라우저) · messenger.html
tools/        serve.js(정적 서버) · run_proof.js(proof 게이트)
tests/        buddy/chat/render/transport/e2e (17 케이스)
sdd/          01_planning → 02_plan → 03_build → 04_verify → 99_toolchain
```

## 설계 한 줄

`reduce(state, event) -> state` 가 순수·결정적이므로 "같은 이벤트 로그 → 같은 상태"가 성립한다.
탭 간 동기화는 이 로그의 복제일 뿐이라, **두 탭이 동일 상태로 수렴**함을 테스트로 증명하면
"두 유저가 로컬에서 대화"가 검증된다. 자세한 근거는 `sdd/04_verify/01_feature/buddy_live.md`.
