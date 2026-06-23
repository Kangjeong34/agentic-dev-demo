package com.datasense.tetris;

/**
 * 보드 위의 조각: 종류·회전·위치(행·열)로 이루어진 불변 표현입니다(AC-5·AC-6).
 * 이동(moved)·회전(rotated)은 상태를 바꾸지 않고 새 인스턴스를 반환하며,
 * 보드 절대 좌표는 cells() 로 계산합니다.
 */
public final class Piece {
    private final Tetromino type;
    private final int rotation;
    private final int row;
    private final int col;

    public Piece(Tetromino type, int rotation, int row, int col) {
        this.type = type;
        this.rotation = Math.floorMod(rotation, type.rotationCount());
        this.row = row;
        this.col = col;
    }

    public Tetromino type() {
        return type;
    }

    public int rotation() {
        return rotation;
    }

    public int row() {
        return row;
    }

    public int col() {
        return col;
    }

    /** 보드 절대 좌표 4칸 {행, 열} 을 반환합니다(상대 좌표 + 현재 위치). */
    public int[][] cells() {
        int[][] base = type.cells(rotation);
        int[][] abs = new int[base.length][2];
        for (int i = 0; i < base.length; i++) {
            abs[i][0] = row + base[i][0];
            abs[i][1] = col + base[i][1];
        }
        return abs;
    }

    /** 이동한 새 조각을 반환합니다(원본 불변). */
    public Piece moved(int dRow, int dCol) {
        return new Piece(type, rotation, row + dRow, col + dCol);
    }

    /** 다음 회전 상태의 새 조각을 반환합니다(원본 불변). */
    public Piece rotated() {
        return new Piece(type, rotation + 1, row, col);
    }
}
