package com.datasense.cyworld.ilchon;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** 일촌 파도타기 AC-1 ~ AC-6 검증. 일촌(accepted) 그래프를 간선원으로 재사용한다. */
class WaveServiceTest {

    /** a-b 양방향 일촌을 성립시킨다(신청→수락). */
    private static void befriend(IlchonService il, String a, String b) {
        il.request(a, b);
        il.accept(a, b);
    }

    @Test
    void start_records_origin() { // AC-1
        WaveService wave = new WaveService(new IlchonService());
        WaveRide ride = wave.start("alice", 1L);
        assertEquals("alice", ride.current());
        assertEquals(List.of("alice"), ride.trail());
    }

    @Test
    void hop_moves_to_unvisited_ilchon() { // AC-2
        IlchonService il = new IlchonService();
        befriend(il, "alice", "bob"); // 후보가 bob 하나뿐 → 시드와 무관하게 결정적
        WaveRide ride = new WaveService(il).start("alice", 1L);

        Optional<String> next = ride.hop();
        assertEquals(Optional.of("bob"), next);
        assertEquals("bob", ride.current());
        assertEquals(List.of("alice", "bob"), ride.trail());
    }

    @Test
    void hop_empty_when_no_unvisited() { // AC-3
        IlchonService il = new IlchonService();
        befriend(il, "alice", "bob"); // alice↔bob 외 간선 없음
        WaveRide ride = new WaveService(il).start("alice", 1L);

        assertEquals(Optional.of("bob"), ride.hop());
        // bob 의 유일한 일촌(alice)은 이미 방문 → 더 갈 곳 없음, 현재 위치 유지
        assertEquals(Optional.empty(), ride.hop());
        assertEquals("bob", ride.current());
        assertEquals(List.of("alice", "bob"), ride.trail());
    }

    @Test
    void trail_has_no_revisits() { // AC-4
        IlchonService il = new IlchonService();
        // 삼각형: 어느 경로로 가도 시작점·기방문 노드는 재방문하지 않는다.
        befriend(il, "alice", "bob");
        befriend(il, "bob", "carol");
        befriend(il, "carol", "alice");
        WaveRide ride = new WaveService(il).start("alice", 7L);

        while (ride.hop().isPresent()) {
            // 갈 곳이 없을 때까지 순회
        }
        List<String> trail = ride.trail();
        assertEquals(new HashSet<>(trail).size(), trail.size(), "방문 이력에 중복이 없어야 한다");
        assertEquals("alice", trail.get(0));
        assertTrue(trail.size() <= 3, "노드 3개를 초과해 방문할 수 없다");
    }

    @Test
    void same_seed_same_path() { // AC-5
        IlchonService il = new IlchonService();
        // 분기가 있는 그래프(후보 2+개) → rng 선택이 실제로 갈린다.
        befriend(il, "alice", "bob");
        befriend(il, "alice", "carol");
        befriend(il, "alice", "dave");
        befriend(il, "bob", "erin");
        befriend(il, "carol", "frank");
        WaveService wave = new WaveService(il);

        assertEquals(walk(wave.start("alice", 42L)), walk(wave.start("alice", 42L)),
                "동일 시드·동일 그래프는 동일 경로를 재현해야 한다");
    }

    @Test
    void hop_only_through_accepted_ilchon() { // AC-6
        IlchonService il = new IlchonService();
        befriend(il, "alice", "bob");
        il.request("alice", "dave"); // pending(미수락) — 간선이 아니어야 한다

        // 여러 번 시도해도 pending 대상(dave)으로는 이동하지 않는다.
        Set<String> reachable = new HashSet<>();
        for (long seed = 0; seed < 20; seed++) {
            reachable.addAll(walk(new WaveService(il).start("alice", seed)));
        }
        assertTrue(reachable.contains("bob"));
        assertFalse(reachable.contains("dave"), "pending 관계는 파도 간선이 아니다");

        // 일촌 해제 시 그 간선이 사라져 더 이상 이동할 수 없다.
        il.breakUp("alice", "bob");
        assertEquals(Optional.empty(), new WaveService(il).start("alice", 0L).hop());
    }

    /** 갈 곳이 없을 때까지 파도를 타고 지나온 경로를 돌려준다. */
    private static List<String> walk(WaveRide ride) {
        List<String> path = new ArrayList<>();
        path.add(ride.current());
        Optional<String> next;
        while ((next = ride.hop()).isPresent()) {
            path.add(next.get());
        }
        return path;
    }
}
