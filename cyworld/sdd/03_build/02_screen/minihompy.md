# 미니홈피 메인 화면 · Build Summary (current state)

> 03_build: 화면 구현 현재 상태. 화면 명세 `01_planning/02_screen/minihompy_main_screen_spec.md`.

## 구현 범위

미니홈피 메인 화면(`minihompy_main`)을 도메인 상태로부터 결정적 HTML 문서로 렌더한다.
브라우저 비가용 환경이라 렌더 출력 자체가 열람 가능한 화면이자 UI parity 대상이다.
레이아웃은 실제 싸이월드 참조 이미지 기반 **클래식 3단** 구성:
상단 TODAY/TOTAL 바 · 좌측 미니미 프로필 바인더 · 중앙 링바인더 페이지(Updated news·Mini Room·
What friends say) · 우측 CYWORLD HOME 게이지 메뉴(도토리·일촌·다이어리·방명록) + 핑크 하트 배경.

| 클래스 | 역할 |
| --- | --- |
| `screen/MinihompyRenderer` | 일촌·다이어리·도토리·미니룸 상태 → 결정적 HTML(스타일 내장) |
| `screen/MinihompyProfile` | 상단 프로필(이름·상태문구·TODAY/TOTAL) 입력값 |

## 화면 ↔ 도메인 매핑

- **미니룸/BGM**: `MiniroomService.layout()` 의 배치 아이템·대표 BGM 을 좌상단에 렌더(miniroom AC-3/AC-5).
- **도토리**: `DotoriService.balance()` 잔액 + 충전 링크(dotori AC-1).
- **다이어리 본문**: `DiaryService.list(owner, viewer)` → 방문자 일촌 여부에 따라 ALL/ILCHON 만 노출,
  PRIVATE 은 주인에게만(diary AC-2/AC-3 = SCR-1/SCR-2).
- **일촌 목록**: `IlchonService.ilchonsOf(owner)` + 일촌명(`profile`) → 좌측 일촌 드롭다운(ilchon AC-2/AC-6 = SCR-5).
- **방명록(What friends say)**: `GuestbookService.list(owner, viewer)` → 비밀글은 방문자 권한에 따라
  "비밀글입니다 🔒" 마스킹(guestbook AC-2 = SCR-3).

## 결정성 보장

- 다이어리: id 오름차순. 일촌: 이름 오름차순(`ilchonsOf` 가 TreeSet). 미니룸: 배치 삽입순.
- 스냅샷 생성/검증이 동일 고정 시나리오(`MinihompyFixture`)를 공유해 drift 차단.

## 변경된 도메인

- `IlchonService`: accepted 관계를 인접 집합으로 리팩터링하고 `ilchonsOf(user)` 추가
  (화면의 일촌 목록 렌더 지원). 기존 일촌 AC 테스트 6종 그대로 통과.
