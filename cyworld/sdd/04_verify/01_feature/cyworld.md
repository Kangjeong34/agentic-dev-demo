# 싸이월드 미니홈피 · Verification Summary (retained)

> 04_verify: 보존 검증 상태와 잔여 위험. command-level 증거 기준.

## 검증 게이트 결과

- 게이트: JUnit 5.12.2 (JUnit Platform Launcher).
- 도메인 5종: **27/27 PASS**. (화면 parity 3종 포함 시 전체 30/30 → `04_verify/02_screen/minihompy.md`)
- `tmp/proof-results.json` `status: PASS`.

```
[ 27 tests found ] [ 27 tests successful ] [ 0 tests failed ]
PROOF: total=27 passed=27 failed=0 status=PASS
```

## AC ↔ 테스트 매핑 (전부 PASS)

| 도메인 | 테스트 클래스 | 케이스 수 | 커버 AC |
| --- | --- | --- | --- |
| 도토리 | `DotoriServiceTest` | 5 | AC-1 충전 / AC-2 차감 / AC-3 잔액부족 거부 / AC-4 멱등 / AC-5 음수불가 |
| 일촌 | `IlchonServiceTest` | 6 | AC-1 pending / AC-2 양방향 / AC-3 거절 / AC-4 멱등 / AC-5 해제 / AC-6 일촌명·평 |
| 다이어리 | `DiaryServiceTest` | 5 | AC-1 작성 / AC-2 공개범위 목록 / AC-3 일촌전용 차단 / AC-4 주인수정 / AC-5 타인거부 |
| 방명록 | `GuestbookServiceTest` | 5 | AC-1 작성 / AC-2 비밀글 분기 / AC-3 주인삭제 / AC-4 작성자삭제 / AC-5 제3자거부 |
| 미니룸 | `MiniroomServiceTest` | 6 | AC-1 구매·차감 / AC-2 잔액부족 미지급 / AC-3 배치 / AC-4 미보유배치거부 / AC-5 대표BGM / (회귀)더블클릭 멱등 |

## 재현 명령 (현재 환경)

설치 JDK가 26뿐이라 Gradle 8.5 데몬 비호환 → JUnit Launcher 직접 실행:

```bash
cd cyworld
CP=$(cat build-manual-cp.txt | sed 's#/c/#C:/#g')
javac --release 17 -d build-manual/classes $(find src/main/java -name '*.java')
javac --release 17 -cp "build-manual/classes;$CP" -d build-manual/test-classes $(find src/test/java -name '*.java')
javac -cp "$CP" -d build-manual/tools tools/TestRunner.java
java -cp "build-manual/tools;build-manual/classes;build-manual/test-classes;$CP" TestRunner
```

JDK 17~21 환경에서는 `./gradlew test` 가 동일한 검증을 수행한다(권장 경로).

## Regression Scope (선택·근거)

- direct: 5개 도메인 서비스 전 케이스.
- shared(교차 검증됨):
  - 공개범위 ↔ 일촌: `DiaryServiceTest::list_respects_visibility`가 일촌 수락 전/후 노출 건수 변화를 단언.
  - 미니룸 구매 ↔ 도토리 차감: `MiniroomServiceTest::buy_*` 및 `duplicate_buy_click_idempotent`가 도토리 멱등성 재사용을 단언.
- 근거 문서: `sdd/02_plan/10_test/regression_verification.md`.

## Residual Risk

- **빌드 게이트 우회**: Gradle proof 게이트(`./gradlew test`)는 JDK 26 비호환으로 현 환경에서 미실행.
  동등 검증을 JUnit Launcher 폴백으로 수행했으나, 표준 경로 재현은 JDK 17~21 설치 후 필요.
- 화면(`minihompy_main`) UI parity는 미구현(도메인 계층만 검증). 별도 화면 구현 태스크 필요.
- 동시성은 단일 노드 `synchronized` 가정 — 분산 환경 미검증.
- 인메모리 저장으로 영속/스키마 패리티 검증 대상 없음(해당 없음).
