# 싸이월드 미니홈피 · todos + 실행 계획

## Scope
미니홈피 핵심 5도메인을 발급·검증까지 구현·검증한다:
일촌(관계) · 다이어리(공개범위) · 방명록(비밀글) · 도토리(멱등 결제) · 미니룸/BGM(구매·배치).
화면은 미니홈피 메인(`minihompy_main`) 1종을 parity 대상으로 한다.

## Acceptance Criteria
- 각 기능 명세의 AC 전부 테스트 통과:
  - 일촌 `01_planning/01_feature/ilchon_feature_spec.md` (AC-1~6)
  - 다이어리 `01_planning/01_feature/diary_feature_spec.md` (AC-1~5)
  - 방명록 `01_planning/01_feature/guestbook_feature_spec.md` (AC-1~5)
  - 도토리 `01_planning/01_feature/dotori_feature_spec.md` (AC-1~5)
  - 미니룸 `01_planning/01_feature/miniroom_feature_spec.md` (AC-1~5)
- 회귀(공개범위↔일촌, 구매↔도토리 차감) green.
- 화면 parity(`minihompy_main`) PASS = 완료.

## Execution Checklist (비중첩)
- [x] T1 @backend-dev  일촌 신청·수락·거절·해제·멱등 (`IlchonService`)
- [x] T2 @backend-dev  도토리 충전·차감·멱등·잔액불변식 (`DotoriService`) — 멱등 키 핵심
- [x] T3 @backend-dev  다이어리 작성·공개범위 판정(일촌 의존) (`DiaryService`)
- [x] T4 @backend-dev  방명록 작성·비밀글·삭제권한 (`GuestbookService`)
- [x] T5 @backend-dev  미니룸 구매(도토리 재사용)·배치·대표 BGM (`MiniroomService`)
- [x] T6 @frontend-dev 미니홈피 메인 화면 렌더 + UI parity (`04_verify/10_test/ui_parity/minihompy_main.html`)
- [x] T7 @test-dev     도메인 27 + 화면 parity 3 = 30케이스 PASS

## Dependencies
- T3·T4 화면 분기는 T1(일촌)에 의존.
- T5는 T2(도토리)에 의존.
- T6는 T1~T5 산출물에 의존.

## Regression Scope
- direct: 각 도메인 서비스 흐름.
- shared: 공개범위 판정(일촌 상태 변경 시 재판정), 도토리 차감(미니룸 구매가 재사용).
- 근거: `sdd/02_plan/10_test/regression_verification.md` (구현 시 생성).

## Validation
- 도메인 27 + 화면 parity 3 = **30/30 PASS** (`tmp/proof-results.json` status=PASS).
- 표준 경로 `./gradlew test`(proof)·`./gradlew uiParity`(회귀)는 현 환경 JDK 26 비호환
  → JUnit Launcher 폴백으로 동등 검증. (재현 명령: `sdd/04_verify/02_screen/minihompy.md`)

## 상태
도메인 5종 + 미니홈피 메인 화면(UI parity) 구현·검증 완료.
build: `03_build/01_feature/cyworld.md`, `03_build/02_screen/minihompy.md`
verify: `04_verify/01_feature/cyworld.md`, `04_verify/02_screen/minihompy.md`
