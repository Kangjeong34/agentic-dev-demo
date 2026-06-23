package com.datasense.tetris;

import java.util.ArrayList;
import java.util.List;

/**
 * 테트리스 보드의 줄 삭제 규칙(AC-3·AC-4)을 담은 핵심 도메인입니다.
 * 보드는 위에서 아래로 쌓인 행 목록이며(index 0 = 맨 위), 각 행은 칸 배열입니다(false=빈칸, true=채움).
 */
public class Board {
    private final int width;
    private final List<boolean[]> rows;

    public Board(int width, List<boolean[]> rows) {
        this.width = width;
        this.rows = new ArrayList<>(rows);
    }

    /** 모든 칸이 빈 width×height 보드를 만듭니다(AC-1). */
    public static Board empty(int width, int height) {
        List<boolean[]> rows = new ArrayList<>();
        for (int i = 0; i < height; i++) {
            rows.add(new boolean[width]);
        }
        return new Board(width, rows);
    }

    public List<boolean[]> rows() {
        return rows;
    }

    public int width() {
        return width;
    }

    public int height() {
        return rows.size();
    }

    /** 해당 칸이 채워져 있으면 true. */
    public boolean cell(int row, int col) {
        return rows.get(row)[col];
    }

    /**
     * AC-1·AC-5·AC-6: 조각이 벽·바닥을 벗어나거나 이미 쌓인 블록과 겹치면 true 입니다.
     */
    public boolean collides(Piece piece) {
        for (int[] cell : piece.cells()) {
            int r = cell[0];
            int c = cell[1];
            if (r < 0 || r >= rows.size() || c < 0 || c >= width) {
                return true;
            }
            if (rows.get(r)[c]) {
                return true;
            }
        }
        return false;
    }

    /** AC-1: 조각의 칸을 보드에 고정(채움)합니다. */
    public void lock(Piece piece) {
        for (int[] cell : piece.cells()) {
            rows.get(cell[0])[cell[1]] = true;
        }
    }

    /**
     * AC-3·AC-4: 가로로 가득 찬 줄을 모두 지우고, 그 위의 줄을 지운 줄 수만큼 아래로 내립니다.
     * @return 지워진 줄 수
     */
    public int clearFullLines() {
        int before = rows.size();
        rows.removeIf(this::isFull);
        int cleared = before - rows.size();
        for (int i = 0; i < cleared; i++) {
            rows.add(0, new boolean[width]); // 위에 빈 줄을 보충해 윗줄이 내려온 효과를 만듭니다
        }
        return cleared;
    }

    private boolean isFull(boolean[] row) {
        for (boolean cell : row) {
            if (!cell) {
                return false;
            }
        }
        return true;
    }
}
