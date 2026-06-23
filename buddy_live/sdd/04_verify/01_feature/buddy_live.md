# 버디버디 · Verification Summary (verify 중점)

> 04_verify: 보존 검증 상태와 잔여 위험. **command-level 증거** 기준.
> 이 데모의 주제는 verify다 — 모든 AC 는 proof 게이트로 통과 증거가 남고, "두 유저가 로컬에서
> 친구추가·대화"는 도메인/transport 수렴 테스트로 증명한다.

## 검증 게이트 결과

- 게이트: Node 24 내장 `node:test` (무의존).
- 도메인·화면·복제 통합 **17/17 PASS**. `tmp/proof-results.json` `status: PASS`.

```
[ 17 tests found ] [ 17 tests successful ] [ 0 tests failed ]
PROOF: total=17 passed=17 failed=0 status=PASS
```

| 테스트 파일 | 케이스 | 비고 |
| --- | --- | --- |
| `tests/buddy.test.js` | 6 | 버디 AC-1~6 |
| `tests/chat.test.js` | 6 | 대화 AC-1~6 |
| `tests/render.test.js` | 2 | SCR-1·SCR-3 + ui_parity 스냅샷 일치 |
| `tests/transport.test.js` | 2 | 두 탭 수렴 / 늦은 합류 replay 수렴 |
| `tests/e2e.test.js` | 1 | 친구추가→수락→양방향대화→읽음 전 흐름 |

## AC ↔ 테스트 매핑 (전부 PASS)

| 도메인 | 테스트 클래스 | 케이스 수 | 커버 AC |
| --- | --- | --- | --- |
| 버디 | `buddy.test.js` | 6 | AC-1 신청 pending / AC-2 양방향 수락 / AC-3 거절 / AC-4 멱등 / AC-5 양방향 해제 / AC-6 접속상태 노출 |
| 대화 | `chat.test.js` | 6 | AC-1 전송·안읽음+1 / AC-2 비버디 거부 / AC-3 읽음 0 / AC-4 양쪽 동일순서 / AC-5 멱등 / AC-6 빈문자 거부 |
| 화면 | `render.test.js` | 2 | SCR-1 접속·배지 / SCR-3 시간순·me·them + parity |
| 복제 | `transport.test.js` | 2 | 두 탭 동일 로그 → 동일 상태 / 늦은 합류 replay 수렴 |
| e2e | `e2e.test.js` | 1 | 두 유저 전 흐름 + 최종 상태 `deepEqual` 수렴 |

## "두 유저가 로컬에서 대화"의 증명 (핵심)

화면의 탭 간 동기화(BroadcastChannel)는 "같은 이벤트 로그를 각 탭이 reduce"하는 복제다. 따라서 이를
도메인 수준에서 등가로 증명한다:

- `transport.test.js::two_tabs_converge` — 두 클라이언트가 서로 다른 주체로 명령(alice 신청/bob 수락,
  양방향 메시지)한 뒤 **두 상태가 `deepEqual` 로 동일**함을 단언.
- `transport.test.js::replay_late_joiner_converges` — 늦게 합류한 탭이 영속 로그 replay 만으로 같은
  상태에 도달(localStorage 복원 등가).
- `e2e.test.js::end_to_end_add_and_chat` — 사용자 관점(받은요청·버디목록·대화·안읽음·읽음)으로 전 흐름을
  단언하고 최종 수렴 확인.

## 재현 명령 (현재 환경)

```bash
cd buddy_live
node tools/run_proof.js                                   # 17/17 PASS → tmp/proof-results.json
node sdd/99_toolchain/01_automation/run_arch_check.js     # ARCH CHECK: PASS
node sdd/99_toolchain/01_automation/gen_proof_evidence.js # → 04_verify/10_test/proof_evidence.md
# 화면 라이브 확인:
node tools/serve.js   # 두 탭: http://localhost:5173/?me=alice  /  ?me=bob
```

표준 경로는 `npm test`(= `node --test`)와 동일 결과. proof 러너는 파일별 집계까지 JSON 으로 남긴다.

## Regression Scope (선택·근거)

- direct: 버디·대화 도메인 전 AC(`buddy.test.js`, `chat.test.js`).
- shared(교차 검증됨):
  - 대화 전송 ↔ 버디 상태: `chat.test.js::send_rejected_when_not_buddy` 가 비버디 거부 + 버디 삭제 후
    재차단을 단언.
  - 화면 렌더 ↔ 도메인 상태: `render.test.js::*` 가 접속·배지·정렬을 상태와 대조(+parity 스냅샷).
  - 탭 복제 수렴 ↔ 동일 리듀서: `transport.test.js`, `e2e.test.js` 가 동일 로그→동일 상태를 단언.
- 근거 문서: `sdd/02_plan/10_test/regression_verification.md`.

## Residual Risk

- **브라우저 라이브 실연(DOM/BroadcastChannel)**: same-origin 서버가 필요해 자동 게이트에서 제외.
  도메인/transport 단위로 등가 검증함(수렴 단언). 수기 확인은 `node tools/serve.js` 2-탭.
- **메시지 전역 전순서(total order)**: 서버 권위 부재. 데모 버스는 단일 프로세스 순서를 가정하며,
  실제 다중 탭의 동시 전송 전순서는 미보장(설계상 범위 밖, 송신측 ts 정렬).
- **presence**: 탭 신호 기반(beforeunload). 비정상 종료 시 잔존 가능 — 서버 하트비트 없음.
- 영속은 localStorage 이벤트 로그 — 스키마/DB 패리티 대상 없음(해당 없음).
