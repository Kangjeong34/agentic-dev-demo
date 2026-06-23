package com.datasense.tetris;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 명세(sdd/01_planning/tetris_spec.md)의 AC-1·AC-2·AC-5~AC-9 를 못 박는 테스트입니다. */
class GameEngineTest {

    private static final long SEED = 42L;

    private static int countFilled(Board board) {
        int count = 0;
        for (int r = 0; r < board.height(); r++) {
            for (int c = 0; c < board.width(); c++) {
                if (board.cell(r, c)) {
                    count++;
                }
            }
        }
        return count;
    }

    @Test // AC-2: 새 조각은 상단(행 0) 중앙에서 생성된다
    void spawnsAtTopCenter() {
        GameEngine engine = new GameEngine(10, 20, SEED);

        assertEquals(0, engine.current().row());
        assertEquals((10 - 4) / 2, engine.current().col());
        assertFalse(engine.isGameOver());
    }

    @Test // AC-7: 한 틱이 진행되면 조각이 한 칸 아래로 내려간다
    void tickMovesDown() {
        GameEngine engine = new GameEngine(10, 20, SEED);
        int before = engine.current().row();

        engine.tick();

        assertEquals(before + 1, engine.current().row());
    }

    @Test // AC-1: 틱을 반복하면 바닥에 닿은 조각이 고정된다
    void tickLocksAtBottom() {
        GameEngine engine = new GameEngine(10, 5, SEED);

        for (int i = 0; i < 10; i++) {
            engine.tick();
        }

        assertTrue(countFilled(engine.board()) > 0, "바닥에 닿은 조각이 보드에 고정되어야 한다");
    }

    @Test // AC-7: 하드드롭은 즉시 바닥까지 내려 고정하고 새 조각을 띄운다
    void hardDropLocksAtFloor() {
        GameEngine engine = new GameEngine(10, 20, SEED);

        engine.hardDrop();

        assertEquals(4, countFilled(engine.board())); // 조각 4칸이 고정됨(줄삭제 없음)
        assertEquals(0, engine.current().row());      // 새 조각이 상단에 생성됨
        assertFalse(engine.isGameOver());
    }

    @Test // AC-5: 빈 공간으로의 이동은 적용된다
    void moveIntoEmptySpace() {
        GameEngine engine = new GameEngine(10, 20, SEED);
        int before = engine.current().col();

        engine.moveRight();

        assertEquals(before + 1, engine.current().col());
    }

    @Test // AC-5: 벽에 막히면 이동이 거부되어 제자리를 유지한다
    void moveBlockedByWall() {
        GameEngine engine = new GameEngine(10, 20, SEED);

        for (int i = 0; i < 20; i++) {
            engine.moveLeft(); // 왼쪽 벽까지 붙인다
        }
        int atWall = engine.current().col();
        for (int[] cell : engine.current().cells()) {
            assertTrue(cell[1] >= 0, "어떤 칸도 왼쪽 벽을 넘지 못한다");
        }

        engine.moveLeft(); // 벽 너머로는 더 못 간다

        assertEquals(atWall, engine.current().col());
    }

    @Test // AC-6: 회전 결과가 쌓인 블록과 충돌하면 회전이 거부된다
    void rotateRejectedOnCollision() {
        List<boolean[]> rows = new ArrayList<>();
        for (int r = 0; r < 4; r++) {
            rows.add(new boolean[4]);
        }
        rows.get(2)[1] = true; // (2,1) 한 칸만 막아 둔다
        Board board = new Board(4, rows);
        GameEngine engine = new GameEngine(board, new PieceGenerator(SEED));
        engine.placeCurrent(new Piece(Tetromino.T, 0, 0, 0)); // (0,1)(1,0)(1,1)(1,2): 충돌 없음

        // 회전하면 (2,1) 칸이 필요한데 이미 막혀 있어 거부되어야 한다
        engine.rotate();

        assertEquals(0, engine.current().rotation());
    }

    @Test // AC-8: 줄을 지우면 점수와 레벨이 오른다
    void scoreAndLevelOnLineClear() {
        GameEngine engine = new GameEngine(4, 6, SEED); // 폭 4: 가로 I 조각이 한 줄을 가득 채움

        for (int i = 0; i < 10; i++) {
            engine.placeCurrent(new Piece(Tetromino.I, 0, 0, 0)); // 가로 막대(칸: 행 1, 열 0~3)
            engine.hardDrop();                                    // 바닥 줄을 채워 1줄 삭제
        }

        assertEquals(10, engine.linesCleared());
        assertEquals(2, engine.level());     // 누적 10줄 → 레벨 2
        assertEquals(1000, engine.score());  // 1줄(100) × 레벨 1 × 10회
    }

    @Test // AC-9: 새 조각 생성 위치가 막혀 있으면 게임오버다
    void gameOverWhenSpawnBlocked() {
        List<boolean[]> rows = new ArrayList<>();
        for (int r = 0; r < 20; r++) {
            boolean[] cells = new boolean[10];
            if (r < 2) {
                Arrays.fill(cells, true); // 상단 두 줄을 가득 채워 생성 위치를 막는다
            }
            rows.add(cells);
        }
        Board board = new Board(10, rows);

        GameEngine engine = new GameEngine(board, new PieceGenerator(SEED));

        assertTrue(engine.isGameOver());
    }
}
