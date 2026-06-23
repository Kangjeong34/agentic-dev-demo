# 01_planning: INDEX (1단 진입점)

> 싸이월드(미니홈피) 도메인. auth 데모와 동일한 SDD 기법(EARS Acceptance Criteria + 검증 매핑)을 따른다.
> 폴더를 열면 이 INDEX가 먼저 보인다: 어떤 명세가 있고 어디까지 됐는지 1단으로.

## 기능 명세 (01_feature)

| 영역 | 파일 | 상태 |
| --- | --- | --- |
| feature · 일촌(관계) | `01_feature/ilchon_feature_spec.md` | 구현·검증 완료 |
| feature · 일촌 파도타기 | `01_feature/wave_feature_spec.md` | 구현·검증 완료 |
| feature · 다이어리 | `01_feature/diary_feature_spec.md` | 구현·검증 완료 |
| feature · 방명록 | `01_feature/guestbook_feature_spec.md` | 구현·검증 완료 |
| feature · 도토리(결제) | `01_feature/dotori_feature_spec.md` | 구현·검증 완료 |
| feature · 미니룸/BGM | `01_feature/miniroom_feature_spec.md` | 구현·검증 완료 |

## 화면 명세 (02_screen)

| 영역 | 파일 | 상태 |
| --- | --- | --- |
| screen · 미니홈피 메인 | `02_screen/minihompy_main_screen_spec.md` | 구현·검증 완료 |

## 데이터 모델 (04_data)

| 영역 | 파일 | 상태 |
| --- | --- | --- |
| data · 도메인 모델 | `04_data/data_model.md` | 계획 |

## 실행 계획 (../02_plan)

| 영역 | 파일 | 상태 |
| --- | --- | --- |
| plan · 1차 구현 백로그 | `../02_plan/01_feature/cyworld_todos.md` | 계획 |

## 도메인 한눈에

싸이월드 = 개인 미니홈피 + 사람 관계망(일촌) + 가상 화폐(도토리) 경제.
- **일촌**: 신청→수락으로 성립하는 양방향 관계. 공개범위·파도타기의 기준.
- **파도타기**: 일촌(accepted) 간선을 타고 미니홈피를 순회. 방문 이력으로 재방문을 막는 그래프 탐색.
- **다이어리/방명록**: 콘텐츠. 공개범위(전체·일촌·비공개)가 일촌 관계에 의존.
- **도토리**: 충전·차감되는 화폐. auth의 OTP처럼 **멱등성**이 핵심 가드레일.
- **미니룸/BGM**: 도토리로 구매하는 아이템 경제의 소비처.
