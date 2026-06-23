# 미니홈피 메인 화면 · Verification Summary (retained)

> 04_verify: 화면 검증 상태와 잔여 위험. command-level 증거 기준.

## 검증 게이트 결과

전체 **30/30 PASS** (도메인 27 + 화면 3) → `tmp/proof-results.json` status=PASS.

| 테스트 | 검증 | 매핑 |
| --- | --- | --- |
| `MinihompyScreenParityTest::render_matches_canonical_snapshot` | 렌더 == 캐노니컬 스냅샷(글자 단위) | UI parity |
| `MinihompyScreenParityTest::ilchon_viewer_sees_all_and_ilchon_but_not_private` | 일촌 방문자: ALL+ILCHON 노출, PRIVATE 숨김 | SCR-1·SCR-2 ← diary AC-2/AC-3 |
| `MinihompyScreenParityTest::stranger_viewer_sees_only_public` | 비일촌 방문자: ALL 만 노출 | SCR-2 ← diary AC-2 |

## 캐노니컬 스냅샷

- 경로: `sdd/04_verify/10_test/ui_parity/minihompy_main.html` (CSS 내장 단독 문서).
- 레이아웃: 실제 싸이월드 참조 이미지 기반 **클래식 3단**(상단 TODAY/TOTAL · 좌 미니미 프로필 ·
  중앙 링바인더 페이지 · 우 CYWORLD HOME 게이지 메뉴) + 핑크 하트 배경.
- 고정 시나리오(`MinihompyFixture`, 방문자=일촌 yuna): 미니룸(분홍 소파·봄날 BGM),
  도토리 1920(2000−50−30), 다이어리 ALL+ILCHON 2건(PRIVATE 제외), 일촌 2명(jihun·yuna),
  방명록 3건(공개 2 + 비밀 1 → yuna 에겐 비밀글 마스킹).

## 재현 명령 (현재 환경, JDK 26 폴백)

```bash
cd cyworld
CP=$(cat build-manual-cp.txt | sed 's#/c/#C:/#g')
javac --release 17 -d build-manual/classes $(find src/main/java -name '*.java')
javac --release 17 -cp "build-manual/classes;$CP" -d build-manual/test-classes $(find src/test/java -name '*.java')
javac -cp "$CP" -d build-manual/tools tools/TestRunner.java
java -cp "build-manual/tools;build-manual/classes;build-manual/test-classes;$CP" TestRunner
```

JDK 17~21 환경에서는 `./gradlew uiParity` 가 동일 검증을 수행한다.

## Residual Risk

- 실제 렌더 픽셀·반응형 레이아웃은 미검증(브라우저 비가용) — 결정적 HTML parity 로만 보증.
- BGM 자동재생/음소거, 탭 전환(사진첩·방명록 본문)은 정적 구조만 — 동적 동작 미구현.
- 사진첩 콘텐츠는 탭만 존재하고 도메인 미구현(별도 명세 예정).
