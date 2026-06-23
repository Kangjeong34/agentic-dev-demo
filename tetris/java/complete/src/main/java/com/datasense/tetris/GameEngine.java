package com.datasense.tetris;

/**
 * 테트리스 게임 루프: 스폰(상단 중앙)·틱 하강·하드드롭·고정 후 줄삭제·좌우 이동·회전·
 * 점수/레벨·게임오버를 하나로 연결합니다(AC-1·AC-2·AC-5~AC-9).
 * 렌더링/입력은 범위 밖이며, 모든 동작은 결정적이라 JUnit 으로 검증합니다.
 */
public final class GameEngine {
    /** 동시에 지운 줄 수별 기본 점수: 1·2·3·4줄 = 100·300·500·800 (AC-8). */
    private static final int[] LINE_SCORE = {0, 100, 300, 500, 800};

    private final Board board;
    private final PieceGenerator generator;
    private Piece current;
    private int score;
    private int level = 1;
    private int linesCleared;
    private boolean gameOver;

    public GameEngine(int width, int height, long seed) {
        this(Board.empty(width, height), new PieceGenerator(seed));
    }

    /** 미리 구성한 보드/생성기로 시작합니다(결정적 테스트 시나리오용). */
    public GameEngine(Board board, PieceGenerator generator) {
        this.board = board;
        this.generator = generator;
        spawn();
    }

    /** AC-2·AC-9: 새 조각을 상단 중앙에 생성하고, 그 자리가 막혀 있으면 게임오버로 전환합니다. */
    private void spawn() {
        Tetromino type = generator.next();
        int spawnCol = (board.width() - 4) / 2;
        Piece piece = new Piece(type, 0, 0, spawnCol);
        if (board.collides(piece)) {
            gameOver = true;
        }
        current = piece;
    }

    /** AC-1·AC-7: 한 칸 내리고, 더 못 내리면 현재 자리에 고정·줄삭제 후 새 조각을 띄웁니다. */
    public void tick() {
        if (gameOver) {
            return;
        }
        Piece down = current.moved(1, 0);
        if (board.collides(down)) {
            lockAndSpawn();
        } else {
            current = down;
        }
    }

    /** AC-7: 충돌 직전까지 즉시 내려 고정합니다. */
    public void hardDrop() {
        if (gameOver) {
            return;
        }
        while (!board.collides(current.moved(1, 0))) {
            current = current.moved(1, 0);
        }
        lockAndSpawn();
    }

    /** AC-5: 충돌하지 않을 때만 한 칸 왼쪽으로 이동합니다. */
    public void moveLeft() {
        tryMove(0, -1);
    }

    /** AC-5: 충돌하지 않을 때만 한 칸 오른쪽으로 이동합니다. */
    public void moveRight() {
        tryMove(0, 1);
    }

    private void tryMove(int dRow, int dCol) {
        if (gameOver) {
            return;
        }
        Piece moved = current.moved(dRow, dCol);
        if (!board.collides(moved)) {
            current = moved;
        }
    }

    /** AC-6: 회전 결과가 충돌하지 않을 때만 회전을 적용하고, 충돌하면 거부합니다(원래 상태 유지). */
    public void rotate() {
        if (gameOver) {
            return;
        }
        Piece rotated = current.rotated();
        if (!board.collides(rotated)) {
            current = rotated;
        }
    }

    private void lockAndSpawn() {
        board.lock(current);
        int cleared = board.clearFullLines();
        if (cleared > 0) {
            score += LINE_SCORE[cleared] * level;   // AC-8: 가산은 지우던 시점의 레벨 기준
            linesCleared += cleared;
            level = 1 + linesCleared / 10;          // 누적 10줄마다 레벨 1 상승
        }
        spawn();
    }

    public Board board() {
        return board;
    }

    public Piece current() {
        return current;
    }

    public int score() {
        return score;
    }

    public int level() {
        return level;
    }

    public int linesCleared() {
        return linesCleared;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    /** 테스트 전용 시드: 결정적 시나리오를 위해 현재 조각을 직접 배치합니다. */
    void placeCurrent(Piece piece) {
        this.current = piece;
    }
}
