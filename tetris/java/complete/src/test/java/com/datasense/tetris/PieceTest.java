package com.datasense.tetris;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** 명세(sdd/01_planning/tetris_spec.md)의 AC-2·AC-6 을 못 박는 테스트입니다. */
class PieceTest {

    @Test // AC-2: 7종 테트로미노가 각각 4개 회전 상태·4칸으로 정의되어 있다
    void spawnShapes() {
        assertEquals(7, Tetromino.values().length);
        for (Tetromino type : Tetromino.values()) {
            assertEquals(4, type.rotationCount(), type + " 는 4개 회전 상태여야 한다");
            for (int rotation = 0; rotation < type.rotationCount(); rotation++) {
                assertEquals(4, type.cells(rotation).length, type + " 의 각 상태는 4칸이어야 한다");
            }
        }
    }

    @Test // AC-6: 회전하면 새 좌표가 나오고, 원본 조각은 변하지 않는다(불변)
    void rotationCells() {
        Piece piece = new Piece(Tetromino.T, 0, 0, 0);
        int[][] before = piece.cells();

        Piece rotated = piece.rotated();

        // 회전 결과: T 의 두 번째 상태 절대 좌표
        assertEquals(1, rotated.rotation());
        assertArrayEquals(new int[][]{{0, 1}, {1, 1}, {1, 2}, {2, 1}}, rotated.cells());

        // 원본은 그대로(불변): 회전 상태도 좌표도 변하지 않았다
        assertEquals(0, piece.rotation());
        assertArrayEquals(before, piece.cells());
    }
}
