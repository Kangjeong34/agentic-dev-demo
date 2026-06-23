# 미니홈피 메인 화면 명세서 (minihompy_main)

> 01_planning: 미니홈피 메인 화면(`minihompy_main`)의 설계 기준을 정의한다.
> 출처는 기능 명세(일촌·다이어리·방명록·도토리·미니룸)이며, 화면 동작은 각 기능 AC와 매핑된다.
> 본 문서는 계획 단계 설계 명세이며, 사실의 원천(렌더 결과)은 구현 시 캐노니컬 스냅샷으로 확정한다.

## 화면 개요

| 항목 | 내용 |
| --- | --- |
| 화면 ID | `minihompy_main` |
| 목적 | 한 사용자의 미니홈피(프로필·미니룸·콘텐츠·일촌)를 한 화면에 노출한다. |
| 진입 조건 | 미니홈피 URL/일촌 목록/파도타기로 특정 사용자 홈피에 진입 |
| 정상 이탈 | 다이어리·사진첩·방명록 탭 이동, 일촌 미니홈피로 이동 |
| 공개 분기 | 방문자의 일촌 여부에 따라 일촌공개 콘텐츠 노출 분기 (← diary AC-2/AC-3) |

## 레이아웃 구조

좌/우 2단 구성.

| 영역 | 위치 | 구성 요소 | 매핑 |
| --- | --- | --- | --- |
| 미니룸 | 좌상단 | 미니미(아바타) + 배치된 아이템 | miniroom AC-3 |
| 프로필 | 좌측 | 이름, 상태문구, 투데이/투데이방문자 카운트 | — |
| BGM | 좌측 | 대표곡 자동재생 표시 | miniroom AC-5 |
| 도토리 | 좌측 하단 | 보유 도토리 잔액 + 충전 진입 | dotori AC-1 |
| 메뉴 탭 | 우상단 | 다이어리 · 사진첩 · 방명록 | diary / guestbook |
| 콘텐츠 | 우측 본문 | 선택된 탭의 목록(기본: 다이어리 최신글) | diary AC-2 |
| 일촌 | 우측 하단 | 일촌 목록 + 일촌명, 파도타기 | ilchon AC-2/AC-6 |

## 상호작용·상태 명세 (EARS)

**SCR-1** When 방문자가 미니홈피에 진입하면, the system shall 방문자의 일촌 여부를 판정해
열람 가능한 다이어리/사진첩만 본문에 노출한다. (← diary AC-2)

**SCR-2** When 비일촌 방문자가 일촌공개 콘텐츠를 열려 하면, the system shall 잠금/접근 거부를
표시한다. (← diary AC-3)

**SCR-3** When 방문자가 방명록 탭을 열면, the system shall 비밀글은 주인·작성자에게만 내용을
노출한다. (← guestbook AC-2)

**SCR-4** When 주인이 도토리 충전/미니룸 구매를 수행하면, the system shall 잔액·미니룸 배치를
갱신해 메인에 반영한다. (← dotori AC-1, miniroom AC-1/AC-3)

**SCR-5** When 방문자가 일촌 목록의 항목을 선택하면, the system shall 해당 일촌의 미니홈피로
이동(파도타기)한다. (← ilchon AC-2)

## 접근성

| 항목 | 계획 |
| --- | --- |
| BGM 자동재생 | 음소거 토글 제공(자동재생 정책 준수) |
| 미니룸 이미지 | 대체텍스트(`alt`) 제공 |
| 탭 키보드 이동 | 메뉴 탭 `tabindex`/`aria-selected` 명시 |

## 디자인 일치 기준 (UI parity) — 실현됨

- 캐노니컬 스냅샷: `sdd/04_verify/10_test/ui_parity/minihompy_main.html` (CSS 내장 단독 문서, 열람 가능).
- 렌더러: `src/main/java/com/datasense/cyworld/screen/MinihompyRenderer.java`.
- 검증 게이트: `MinihompyScreenParityTest`(결정적 HTML parity). JDK 17~21 에선 `./gradlew uiParity`.
- 결과: parity 1/1 PASS (`sdd/04_verify/02_screen/minihompy.md`).

## 검증 매핑

| 화면 동작 | 기능 AC | 테스트 |
| --- | --- | --- |
| SCR-1·SCR-2 (공개범위 분기) | diary AC-2/AC-3 | `DiaryServiceTest` + 화면 parity |
| SCR-3 (비밀 방명록) | guestbook AC-2 | `GuestbookServiceTest` |
| SCR-4 (도토리/미니룸 반영) | dotori AC-1, miniroom AC-1/3 | `DotoriServiceTest`, `MiniroomServiceTest` |
| SCR-5 (파도타기) | ilchon AC-2 | `IlchonServiceTest` |
| 화면 일치 | — | `minihompy_main.html` parity |

## Residual Risk

- 반응형·픽셀 레이아웃은 구현 시 스냅샷으로 확정(현 계획 단계 미검증).
- 미니미 커스터마이징·꾸미기 상점 UI는 별도 화면으로 분리 예정.
