# 01_planning: INDEX (1단 진입점)

> 버디버디(BuddyLive) 메신저 도메인. cyworld 데모와 동일한 SDD 기법(EARS Acceptance Criteria + 검증 매핑)을 따른다.
> **이 데모는 verify를 중점으로 한다**: 모든 AC는 `node --test` proof 게이트로 통과 증거가 남고,
> "두 유저가 로컬에서 친구추가·대화"는 도메인 리듀서의 수렴(convergence) 테스트로 증명된다.

## 기능 명세 (01_feature)

| 영역 | 파일 | 상태 |
| --- | --- | --- |
| feature · 버디(친구추가/관계/접속상태) | `01_feature/buddy_feature_spec.md` | 구현·검증 완료 |
| feature · 대화(1:1 메시지/읽음/멱등) | `01_feature/chat_feature_spec.md` | 구현·검증 완료 |

## 화면 명세 (02_screen)

| 영역 | 파일 | 상태 |
| --- | --- | --- |
| screen · 메신저(버디목록+대화창) | `02_screen/messenger_screen_spec.md` | 구현·검증 완료 |

## 실행 계획 (../02_plan)

| 영역 | 파일 | 상태 |
| --- | --- | --- |
| plan · 1차 구현 백로그 | `../02_plan/01_feature/buddy_live_todos.md` | 구현 완료 |
| plan · 회귀 검증 범위 | `../02_plan/10_test/regression_verification.md` | 검증 완료 |

## 구현 (../03_build)

| 영역 | 파일 | 상태 |
| --- | --- | --- |
| build · 기능 요약 | `../03_build/01_feature/buddy_live.md` | 구현 완료 |
| build · 화면 요약 | `../03_build/02_screen/messenger.md` | 구현 완료 |

## 검증 (../04_verify)

| 영역 | 파일 | 상태 |
| --- | --- | --- |
| verify · 기능 검증 요약 | `../04_verify/01_feature/buddy_live.md` | 17/17 PASS |
| verify · 화면 검증 요약 | `../04_verify/02_screen/messenger.md` | parity PASS |
| verify · proof 증빙(생성물) | `../04_verify/10_test/proof_evidence.md` | status=PASS |
| verify · UI parity 스냅샷 | `../04_verify/10_test/ui_parity/messenger.html` | 일치 |

## 자동화 게이트 (../99_toolchain)

| 게이트 | 명령 | 산출물 |
| --- | --- | --- |
| proof 테스트 | `node tools/run_proof.js` | `tmp/proof-results.json` (17/17 PASS) |
| 도메인 경계 | `node sdd/99_toolchain/01_automation/run_arch_check.js` | 위반 0 |
| proof 증빙 생성 | `node sdd/99_toolchain/01_automation/gen_proof_evidence.js` | `04_verify/10_test/proof_evidence.md` |

## 도메인 한눈에

버디버디 = 사람 관계망(버디) + 1:1 실시간 대화.
- **버디**: 신청→수락으로 성립하는 양방향 친구 관계. 대화 가능 여부의 기준.
- **대화**: 버디 사이에서만 오가는 1:1 메시지. 안읽음 카운트·읽음 처리·멱등(재전송 가드)이 핵심.
- **접속상태(presence)**: 접속/오프라인을 버디 목록에 노출.

## 설계 핵심 — 왜 "이벤트 리듀서"인가 (verify 중점)

상태를 직접 변이하는 서비스 대신 **순수 이벤트 리듀서**(`reduce(state, event) -> state`)로 도메인을 모델링한다.
- 단위 테스트가 이벤트 시퀀스를 흘려 셀렉터로 단언 → AC가 결정적으로 증명된다.
- 화면의 탭 간 동기화(`BroadcastChannel`)는 "같은 이벤트 로그를 각 탭이 리듀스"하는 복제다.
  → **두 탭이 동일 상태로 수렴**함을 테스트로 증명하면 "두 유저가 로컬에서 대화"가 검증된다.
