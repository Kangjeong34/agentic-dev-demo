# 버디버디(BuddyLive) · todos + 실행 계획

## Scope
버디버디 메신저를 도메인부터 화면까지, **verify 중점**으로 구현·검증한다:
버디(친구추가·접속상태) · 대화(1:1 메시지·읽음·멱등) + 메신저 화면(라이브 2-탭).

## Assumptions
- 무의존 스택: 도메인/테스트는 Node 24 내장 `node:test`, 화면은 브라우저 ESM + `BroadcastChannel`.
- 서버 없음(이벤트 복제). 정적 서빙만 `tools/serve.js`(Node 내장 http).
- "두 유저가 로컬에서 대화" = 두 탭이 동일 이벤트 로그를 리듀스해 동일 상태로 수렴.

## Acceptance Criteria
- 버디 `01_planning/01_feature/buddy_feature_spec.md` AC-1~6 전부 테스트 통과.
- 대화 `01_planning/01_feature/chat_feature_spec.md` AC-1~6 전부 테스트 통과.
- 화면 `01_planning/02_screen/messenger_screen_spec.md` SCR render parity 통과.
- 회귀(대화↔버디 상태, 탭 복제 수렴) green.
- proof 게이트 `tmp/proof-results.json` status=PASS = 완료.

## Execution Checklist (비중첩)
- [x] T1 @backend-dev  도메인 리듀서·셀렉터: 버디 신청·수락·거절·삭제·멱등·접속상태 (`src/domain`)
- [x] T2 @backend-dev  도메인 리듀서·셀렉터: 대화 송신·읽음·멱등·빈문자 거부 (`src/domain`)
- [x] T3 @frontend-dev 순수 렌더 `renderMessenger(state, viewer)` + 메신저 HTML/CSS (`src/web`)
- [x] T4 @frontend-dev 탭 간 라이브 동기화: `BroadcastChannel` 이벤트 복제 + localStorage 영속 (`src/web/app.js`)
- [x] T5 @backend-dev  정적 서버 `tools/serve.js` (Node 내장 http, 무의존)
- [x] T6 @test-dev     proof 게이트: 버디6 + 대화6 + 렌더2 + transport2 + e2e1 = 17 케이스 PASS
- [x] T7 @test-dev     toolchain: proof 생성기 + arch check(도메인→웹 의존 금지)

## Dependencies
- T2(대화)는 T1(버디)에 의존 — 친구만 대화(AC-2).
- T3·T4(화면)는 T1·T2 도메인에 의존(동일 리듀서/렌더 재사용).
- T6는 T1~T5 산출물에 의존.

## Regression Scope
- direct: 버디·대화 도메인 전 AC.
- shared: 대화 전송 가능 여부(버디 상태 변경 시 재판정), 탭 복제 수렴(두 클라이언트 동일 로그→동일 상태).
- 근거: `sdd/02_plan/10_test/regression_verification.md`.

## Validation
- 17/17 PASS 목표(`tmp/proof-results.json` status=PASS).
- 명령: `node tools/run_proof.js` → `node sdd/99_toolchain/01_automation/gen_proof_evidence.js`.
- arch: `node sdd/99_toolchain/01_automation/run_arch_check.js`.

## 상태
구현·검증 완료. T1~T7 전부 done. proof 17/17 PASS(`tmp/proof-results.json` status=PASS),
arch check PASS. 증거: `sdd/04_verify/01_feature/buddy_live.md`.
