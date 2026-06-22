# 테트리스 명세 (EARS, 검증 가능)

원문(00_sources/tetris.md)을 참·거짓을 판정할 수 있는 조건문으로 구조화합니다.
좌표 약속: 보드는 위에서 아래로 쌓인 행 목록이며 index 0 = 맨 위, 각 행은 칸(빈칸/채움)입니다.

## AC-1 블록은 더 내려갈 수 없으면 고정된다
When 조각이 바닥 또는 다른 블록에 닿아 더 내려갈 수 없으면,
the system shall 조각을 현재 칸에 고정한다.

## AC-2 새 조각은 상단 중앙에 생성된다
When 새 조각이 필요하면,
the system shall 7종(I·O·T·S·Z·J·L) 중 하나를 보드 상단 중앙에 생성한다.

## AC-3 가득 찬 줄은 지워지고 윗줄이 내려온다
When 한 줄의 모든 칸이 채워지면,
the system shall 그 줄을 지우고, 그 위의 모든 줄을 지운 줄 수만큼 아래로 내린다.

## AC-4 여러 줄이 동시에 가득 차면 함께 지워진다
When 둘 이상의 줄이 동시에 가득 차면,
the system shall 그 줄들을 모두 지우고 지운 줄 수만큼 윗줄을 내린다.

## AC-5 좌우 이동은 충돌하지 않을 때만 적용된다
When 좌 또는 우 이동 요청 시 이동 결과가 벽·다른 블록과 충돌하지 않으면,
the system shall 조각을 한 칸 이동하고, 충돌하면 제자리를 유지한다.

## AC-6 회전은 충돌하지 않을 때만 적용된다
When 회전 요청 시 회전 결과가 벽·다른 블록과 충돌하지 않으면,
the system shall 조각을 회전하고, 충돌하면 회전을 거부한다(원래 상태 유지).

## AC-7 조각은 한 칸씩 낙하하고, 하드드롭은 즉시 바닥까지 내린다
When 한 틱이 진행되면 the system shall 조각을 한 칸 아래로 내린다.
When 하드드롭이 요청되면 the system shall 조각을 충돌 직전까지 즉시 내려 고정한다.

## AC-8 줄을 지우면 점수와 레벨이 오른다
When N개의 줄이 동시에 지워지면,
the system shall 점수를 (1·2·3·4줄 = 100·300·500·800) × 현재 레벨만큼 더하고,
누적 삭제 줄이 10줄 늘 때마다 레벨을 1 올린다.

## AC-9 새 조각이 생성 위치에서 막히면 게임오버다
When 새 조각이 생성 위치에서 즉시 다른 블록과 충돌하면,
the system shall 게임오버 상태로 전환한다.

## 검증 매핑 (AC ↔ 테스트)
| AC | 테스트 |
|----|--------|
| AC-1 | BoardTest::locksPieceCells, GameEngineTest::tickLocksAtBottom |
| AC-2 | PieceTest::spawnShapes, GameEngineTest::spawnsAtTopCenter |
| AC-3 | BoardTest::clearsSingleFullLine |
| AC-4 | BoardTest::clearsMultipleFullLines |
| AC-5 | GameEngineTest::moveBlockedByWall, moveIntoEmptySpace |
| AC-6 | PieceTest::rotationCells, GameEngineTest::rotateRejectedOnCollision |
| AC-7 | GameEngineTest::tickMovesDown, hardDropLocksAtFloor |
| AC-8 | GameEngineTest::scoreAndLevelOnLineClear |
| AC-9 | GameEngineTest::gameOverWhenSpawnBlocked |
