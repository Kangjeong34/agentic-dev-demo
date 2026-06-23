package com.datasense.cyworld.ilchon;

import java.util.Random;

/**
 * 일촌 파도타기 진입점. 일촌(accepted) 그래프를 간선원으로 삼아 파도타기 세션을 연다.
 * 상태는 세션({@link WaveRide})이 들고, 이 서비스는 일촌 도메인 의존만 묶어 주는 팩토리다.
 * (← wave AC-1 ~ AC-6, ilchon 도메인 재사용)
 */
public class WaveService {

    private final IlchonService ilchon;

    public WaveService(IlchonService ilchon) {
        this.ilchon = ilchon;
    }

    /** 무작위 시드로 파도타기를 시작한다(실서비스 경로). */
    public WaveRide start(String start) {
        return new WaveRide(ilchon, start, new Random().nextLong());
    }

    /** 고정 시드로 파도타기를 시작한다 — 동일 시드·동일 그래프면 동일 경로(AC-5, 테스트·재현용). */
    public WaveRide start(String start, long seed) {
        return new WaveRide(ilchon, start, seed);
    }
}
