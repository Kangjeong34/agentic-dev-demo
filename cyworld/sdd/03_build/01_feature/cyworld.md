# 싸이월드 미니홈피 · Build Summary (current state)

> 03_build: 현재 구현 상태 요약(사실·현재형). 계획은 `01_planning`, 검증은 `04_verify` 참조.

## 구현 범위

미니홈피 핵심 5도메인을 인메모리 도메인 서비스로 구현. 외부 인프라 없음(JDK만 필요).
패키지 루트: `com.datasense.cyworld`.

| 도메인 | 주요 클래스 | 매핑 AC |
| --- | --- | --- |
| 도토리(결제) | `dotori/DotoriService`, `DotoriResult`, `DotoriStatus` | dotori AC-1~5 |
| 일촌(관계) | `ilchon/IlchonService`, `IlchonProfile`, `IlchonStatus` | ilchon AC-1~6 |
| 다이어리 | `diary/DiaryService`, `Diary`, `Visibility` | diary AC-1~5 |
| 방명록 | `guestbook/GuestbookService`, `GuestbookEntry`, `GuestbookView` | guestbook AC-1~5 |
| 미니룸/BGM | `miniroom/MiniroomService`, `Item`, `ItemType`, `MiniroomLayout`, `BuyResult` | miniroom AC-1~5 |

## 핵심 설계 결정

- **멱등성 가드레일(도토리)**: `paymentKey` 단위로 충전·차감 결과를 고정해 중복 거래를 막는다.
  거부(잔액부족)도 동일 키로 고정해 재시도 우회 차감을 차단한다(dotori AC-3·AC-4·AC-5).
- **도토리 재사용(미니룸)**: `MiniroomService.buy()`가 `DotoriService.spend()`를 호출 →
  잔액 부족 시 아이템 미지급, 더블클릭(같은 결제키)은 1회만 차감(miniroom AC-1·AC-2).
- **공개범위 판정(다이어리)**: `DiaryService`가 `IlchonService.isIlchon()`에 의존해 ILCHON 글을
  일촌에게만 노출(diary AC-2·AC-3) — 일촌 상태 변화가 공개범위에 즉시 반영.
- **비밀글 분기(방명록)**: 목록 조회 시 비밀글은 주인·작성자에게만 content 노출, 그 외 null 마스킹.
- **일촌 양방향 표현**: accepted 관계는 정규화한 무순서 키 1건으로 저장 → 양쪽에서 `isIlchon` 일치.

## 빌드/실행 방법

- 표준 경로(JDK 17~21 환경): `./gradlew test` (proof 게이트) → `tmp/proof-results.json`.
- 현재 환경 폴백: 설치된 JDK가 26뿐이라 Gradle 8.5 데몬이 기동 불가(major version 70).
  → `tools/TestRunner.java`(JUnit Platform Launcher)로 직접 컴파일·실행해 동일 proof 산출물 생성.
  자세한 명령·결과는 `04_verify/01_feature/cyworld.md` 참조.

## 미구현(현재 범위 밖)

- 웹/HTTP 계층, 영속 DB(인메모리만).
- 미니홈피 메인 화면(`02_screen` 명세는 존재) 실제 렌더·UI parity 스냅샷.
- 사진첩, 미니미 커스터마이징, 알림 발송, 환불.
