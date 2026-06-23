package com.datasense.cyworld.ilchon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/**
 * 한 번의 파도타기 세션 상태. 시작점에서 일촌(accepted) 간선을 타고 미니홈피를 순회한다.
 * 방문 이력으로 재방문(순환)을 막고, 시드 기반 선택으로 경로를 재현 가능하게 한다.
 * (← wave AC-1 ~ AC-6)
 */
public final class WaveRide {

    private final IlchonService ilchon;
    private final Random rng;
    /** 방문 순서(시작점 포함). 미니홈피 "지나온 길" 표시에 쓴다. */
    private final List<String> trail = new ArrayList<>();
    /** 재방문 차단용 방문 집합(시작점 포함). */
    private final Set<String> visited = new HashSet<>();
    private String current;

    WaveRide(IlchonService ilchon, String start, long seed) {
        this.ilchon = ilchon;
        this.rng = new Random(seed);
        this.current = start;
        trail.add(start);   // 시작점을 현재 위치이자 첫 방문으로 기록(AC-1)
        visited.add(start);
    }

    /** 현재 위치한 미니홈피 주인. */
    public String current() {
        return current;
    }

    /** 시작점부터 현재까지 지나온 경로(불변 사본). */
    public List<String> trail() {
        return List.copyOf(trail);
    }

    /**
     * 다음 파도: 현재 위치 사용자의 미방문 일촌 중 한 명으로 이동한다(AC-2).
     * 후보는 일촌 목록(이름 오름차순) 중 미방문만 추리고, 시드 기반으로 한 명을 고른다(AC-5).
     * 갈 곳이 없으면 현재 위치를 유지하고 빈 결과를 돌려준다(AC-3).
     * 일촌(accepted)만 간선으로 쓰므로 일촌이 아닌/해제된 관계로는 이동하지 않는다(AC-6).
     */
    public Optional<String> hop() {
        List<String> candidates = new ArrayList<>();
        for (String neighbor : ilchon.ilchonsOf(current)) { // ilchonsOf = 오름차순 정렬 → 결정성
            if (!visited.contains(neighbor)) {               // 방문 이력 기반 재방문 차단(AC-4)
                candidates.add(neighbor);
            }
        }
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        String next = candidates.get(rng.nextInt(candidates.size()));
        current = next;
        trail.add(next);
        visited.add(next);
        return Optional.of(next);
    }
}
