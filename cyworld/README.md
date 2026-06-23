# cyworld — 싸이월드 미니홈피 데모 (SDD)

auth 데모와 동일한 **SDD 기법**(EARS Acceptance Criteria → plan → build → verify)으로 만든
싸이월드 미니홈피 도메인 구현. 외부 인프라 없이 인메모리로 동작한다.

## 도메인 (bounded context)

| 도메인 | 설명 | 핵심 가드레일 |
| --- | --- | --- |
| 일촌(ilchon) | 신청·수락·거절·해제, 일촌명/일촌평 | 양방향 정규화, 중복 신청 멱등 |
| 다이어리(diary) | 글 작성, 공개범위(전체/일촌/비공개) | 공개범위 ← 일촌 상태 의존 |
| 방명록(guestbook) | 글 작성, 비밀글, 삭제 권한 | 비밀글은 주인·작성자에게만 노출 |
| 도토리(dotori) | 충전·차감·잔액 | **paymentKey 멱등 결제**, 음수 잔액 불가 |
| 미니룸(miniroom) | 아이템/BGM 구매·배치·대표곡 | 구매는 도토리 차감 재사용 |

## 빌드 · 테스트

표준 경로(JDK 17~21):

```bash
./gradlew test      # proof 게이트 → tmp/proof-results.json
./gradlew uiParity  # 회귀 게이트
```

> 현재 개발 환경은 JDK 26만 설치되어 Gradle 8.5 데몬이 호환되지 않는다.
> 그 경우 `tools/TestRunner.java`(JUnit Platform Launcher) 폴백으로 동등 검증한다.
> 명령은 `sdd/04_verify/01_feature/cyworld.md` 참조. 현재 결과: **27/27 PASS**.

## SDD 산출물

```
sdd/
├── 01_planning/   INDEX · 기능 명세(EARS AC) · 화면 명세 · 데이터 모델
├── 02_plan/       실행 백로그 · 회귀 범위
├── 03_build/      구현 요약
└── 04_verify/     검증 요약(27/27 PASS) · 잔여 위험
```
