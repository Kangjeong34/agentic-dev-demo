# 99_toolchain · 자동화 게이트

미니홈피 데모의 완료 기준은 "사람이 봤다"가 아니라 "게이트가 통과시켰다"입니다.
이커머스 데모와 동일하게 두 개의 게이트 + 하나의 생성기로 구성됩니다.

## 게이트 1 · proof 테스트 게이트

```
# 표준 경로 (JDK 17~21)
./gradlew test

# 현재 환경 (설치 JDK가 26뿐 → Gradle 8.5 데몬 비호환) 폴백
CP=$(cat build-manual-cp.txt | sed 's#/c/#C:/#g')
javac -encoding UTF-8 --release 17 -d build-manual/classes $(find src/main/java -name '*.java')
javac -encoding UTF-8 --release 17 -cp "build-manual/classes;$CP" -d build-manual/test-classes $(find src/test/java -name '*.java')
javac -encoding UTF-8 -cp "$CP" -d build-manual/tools tools/TestRunner.java
java -Dfile.encoding=UTF-8 -cp "build-manual/tools;build-manual/classes;build-manual/test-classes;$CP" TestRunner
```

도메인 단위 33 + 화면 parity 3 = 36개를 결정적으로 실행합니다. 브라우저 비가용
환경이라 화면 parity 는 Playwright exactness 대신 결정적 HTML 스냅샷 대조
(`MinihompyScreenParityTest`)로 대체합니다. exit 0 = 통과이며, 결과는
`tmp/proof-results.json`(집계 + 클래스별 통계)에 기록됩니다.

> **UI parity 결정성**: 스냅샷(`sdd/04_verify/10_test/ui_parity/minihompy_main.html`)은
> `cyworld/.gitattributes` 에서 `eol=lf` 로 고정합니다. 렌더러가 LF 를 출력하므로,
> Windows 체크아웃에서 CRLF 로 변환되면 parity 가 거짓 FAIL 을 냅니다. 이 핀이 게이트를
> OS 와 무관하게 만듭니다.

## 게이트 2 · 도메인 경계 게이트 (구조)

```
python3 sdd/99_toolchain/01_automation/run_arch_check.py
```

미니홈피 패키지는 `com.datasense.cyworld.<context>` 한 단계로 평면 구성입니다.
두 가지 구조 규칙을 코드로 판정합니다.

- 규칙 1: 도메인 컨텍스트(dotori·ilchon·diary·guestbook·miniroom)는 화면(screen,
  표현 계층)을 import 하지 않습니다. 도메인은 화면을 먹여 살릴 뿐 화면에 의존하지
  않는다 — 단방향을 강제합니다.
- 규칙 2: 컨텍스트 의존 그래프에 순환이 없습니다.

현재 의존 엣지는 `diary→ilchon`(공개범위 판정), `miniroom→dotori`(도토리 차감 재사용),
`screen→{dotori,ilchon,diary,guestbook,miniroom}`(화면 렌더) 로, 전부 단방향입니다.
이 게이트는 예컨대 도토리 서비스가 화면을 역참조하거나 miniroom↔dotori 가 서로를
참조해 순환이 생기는 순간 FAIL 로 잡아냅니다.

## 생성기 · proof 증빙 문서

```
python3 sdd/99_toolchain/01_automation/gen_proof_evidence.py
python3 sdd/99_toolchain/01_automation/gen_proof_evidence.py --dry-run
```

`tmp/proof-results.json` 을 파싱해 `sdd/04_verify/10_test/proof_evidence.md` 를
생성합니다. 입력(테스트 결과) → 출력(검증 증거)의 단방향 흐름이며, 이 파일이
SDD 에서 '코드가 통과시켰다'의 공식 증거가 됩니다. 손으로 수정하지 않습니다.
