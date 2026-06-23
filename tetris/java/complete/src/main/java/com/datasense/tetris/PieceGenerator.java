package com.datasense.tetris;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * 결정적 조각 생성기(AC-2): 7종을 한 묶음(7-bag)으로 섞어 모두 소진한 뒤 다시 채웁니다.
 * 같은 시드(seed)면 항상 같은 순서가 나오므로 테스트로 못 박을 수 있습니다.
 */
public final class PieceGenerator {
    private final Random random;
    private final Deque<Tetromino> bag = new ArrayDeque<>();

    public PieceGenerator(long seed) {
        this.random = new Random(seed);
    }

    /** 다음 조각 종류를 반환합니다. 묶음이 비면 7종을 다시 섞어 채웁니다. */
    public Tetromino next() {
        if (bag.isEmpty()) {
            refill();
        }
        return bag.poll();
    }

    private void refill() {
        List<Tetromino> shuffled = new ArrayList<>(List.of(Tetromino.values()));
        Collections.shuffle(shuffled, random);
        bag.addAll(shuffled);
    }
}
