# 일촌 파도타기 · Acceptance Criteria (EARS)

> 01_planning: 일촌 관계망을 타고 미니홈피를 순회하는 "파도타기" 요구사항을 검증 가능한 EARS로 정제.
> 파도타기는 일촌(accepted) 관계를 간선으로 삼는 그래프 순회다. 일촌 도메인에 의존하며 자체 상태(방문 이력)를 가진다.
> 기준 관계 명세: `ilchon_feature_spec.md` (AC-2 양방향 성립 · AC-5 해제).

**AC-1** When 사용자가 특정 미니홈피(start)에서 파도타기를 시작하면, the system shall 시작 사용자를
현재 위치로 두고 방문 이력에 기록한다.

**AC-2** When 사용자가 다음 파도(hop)를 요청하면, the system shall 현재 위치 사용자의 일촌(accepted) 중
아직 방문하지 않은 한 명으로 현재 위치를 이동시키고 방문 이력에 추가한다.

**AC-3** When 현재 위치 사용자의 일촌이 없거나 모두 이미 방문되었으면, the system shall 더 이동할 수 없음
(빈 결과)을 반환하고 현재 위치와 방문 이력을 그대로 유지한다.

**AC-4** While 한 파도타기 세션이 진행되는 동안, the system shall 시작점을 포함해 이미 방문한 사용자를
재방문하지 않는다(방문 이력 기반 순환 방지).

**AC-5** When 동일한 시드(seed)와 동일한 일촌 그래프로 파도타기를 수행하면, the system shall 동일한 순회
경로를 재현한다(결정성 — 테스트·재현 가능성).

**AC-6** When 두 사용자가 일촌이 아니면(또는 일촌이 해제되면), the system shall 그 사용자로의 파도(hop)를
간선으로 사용하지 않는다(일촌 상태 의존 — 회귀 가드).

## 검증 매핑

| AC | 테스트 |
| --- | --- |
| AC-1 | `WaveServiceTest::start_records_origin` |
| AC-2 | `WaveServiceTest::hop_moves_to_unvisited_ilchon` |
| AC-3 | `WaveServiceTest::hop_empty_when_no_unvisited` |
| AC-4 | `WaveServiceTest::trail_has_no_revisits` |
| AC-5 | `WaveServiceTest::same_seed_same_path` |
| AC-6 | `WaveServiceTest::hop_only_through_accepted_ilchon` |
| 회귀 | 일촌 상태(`IlchonService.isIlchon`/`ilchonsOf`) 변경이 파도 간선에 즉시 반영 |

## Residual Risk

- 파도타기 알림·방문자 카운트(투데이/투탈) 연동은 1차 범위 밖.
- 무작위 선택은 단일 노드 `java.util.Random`(시드 기반) 가정 — 분산 난수 동기화 미검증.
- 깊이 제한·타임아웃 등 순회 정책은 호출자 책임(데모는 "갈 곳 없으면 종료"만 보장).
