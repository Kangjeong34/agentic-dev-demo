# 다이어리 · Acceptance Criteria (EARS)

> 01_planning: 다이어리 글 작성·공개범위·권한 요구사항을 EARS로 정제.
> 공개범위 판정은 일촌(`ilchon_feature_spec.md`) 관계 상태에 의존한다.

**AC-1** When 주인이 다이어리 글을 작성하면, the system shall 공개범위(전체공개·일촌공개·비공개)와
함께 글 1건을 저장한다.

**AC-2** When 방문자가 글 목록을 조회하면, the system shall 방문자의 일촌 여부에 따라
열람 가능한 글만 노출한다. (전체공개=모두, 일촌공개=일촌만, 비공개=주인만)

**AC-3** When 비일촌 방문자가 일촌공개 글에 직접 접근하면, the system shall 접근을 거부한다.

**AC-4** When 주인이 자신의 글을 수정·삭제하면, the system shall 변경을 반영한다.

**AC-5** When 주인이 아닌 사용자가 글을 수정·삭제하려 하면, the system shall 권한 없음으로 거부한다.

## 검증 매핑

| AC | 테스트 |
| --- | --- |
| AC-1 | `DiaryServiceTest::write_with_visibility` |
| AC-2 | `DiaryServiceTest::list_respects_visibility` |
| AC-3 | `DiaryServiceTest::ilchon_only_blocks_stranger` |
| AC-4 | `DiaryServiceTest::owner_can_edit_delete` |
| AC-5 | `DiaryServiceTest::non_owner_cannot_modify` |
| 회귀 | 일촌 상태 변경 후 공개범위 재판정 |

## Residual Risk

- 댓글·이모티콘·기분/날씨 태그는 1차 범위 밖.
- 사진첩(포토)은 다이어리와 같은 공개범위 모델을 공유하나 별도 명세로 분리 예정.
