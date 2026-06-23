package com.datasense.cyworld.ilchon;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 일촌 관계: 신청·수락·거절·해제 + 일촌명/일촌평.
 * 양방향(accepted) 관계는 인접 집합으로 표현해 양쪽 일촌 목록 조회를 지원한다.
 * (← ilchon AC-1 ~ AC-6)
 */
public class IlchonService {

    /** "from->to" pending 요청. */
    private final Map<String, Boolean> pending = new ConcurrentHashMap<>();
    /** user -> 일촌(accepted) 집합. 양방향으로 함께 갱신한다. */
    private final Map<String, Set<String>> adjacency = new ConcurrentHashMap<>();
    /** "owner->other" 별 일촌명/일촌평. */
    private final Map<String, IlchonProfile> profiles = new ConcurrentHashMap<>();

    /**
     * 일촌 신청(requester -> target). pending 요청 1건을 만든다(AC-1).
     * 이미 일촌이거나 같은 방향 요청이 진행 중이면 중복을 만들지 않는다(AC-4).
     */
    public synchronized IlchonStatus request(String requester, String target) {
        if (requester.equals(target)) {
            return IlchonStatus.NONE;
        }
        if (isIlchon(requester, target)) {
            return IlchonStatus.ACCEPTED; // 이미 일촌 → 멱등
        }
        // 같은 방향 pending 은 put 이 자연 멱등(중복 키 → 단일 엔트리)이라 별도 검사 불필요(AC-4).
        pending.put(directed(requester, target), Boolean.TRUE);
        return IlchonStatus.PENDING;
    }

    /** requester->target pending 요청을 target 이 수락 → 양방향 일촌 성립(AC-2). */
    public synchronized boolean accept(String requester, String target) {
        if (pending.remove(directed(requester, target)) == null) {
            return false;
        }
        adjacency.computeIfAbsent(requester, k -> ConcurrentHashMap.newKeySet()).add(target);
        adjacency.computeIfAbsent(target, k -> ConcurrentHashMap.newKeySet()).add(requester);
        return true;
    }

    /** requester->target pending 요청을 거절 → 요청 제거, 관계 미성립(AC-3). */
    public synchronized boolean reject(String requester, String target) {
        return pending.remove(directed(requester, target)) != null;
    }

    /** 일촌 해제 → 양방향 관계와 양쪽 프로필을 동시에 제거한다(AC-5). */
    public synchronized boolean breakUp(String userA, String userB) {
        boolean removed = adjacency.getOrDefault(userA, Set.of()).remove(userB);
        adjacency.getOrDefault(userB, Set.of()).remove(userA);
        profiles.remove(directed(userA, userB));
        profiles.remove(directed(userB, userA));
        return removed;
    }

    /** 일촌명/일촌평 등록(AC-6). 일촌 상태에서만 허용한다. */
    public synchronized boolean setProfile(String owner, String other, String nickname, String review) {
        if (!isIlchon(owner, other)) {
            return false;
        }
        profiles.put(directed(owner, other), new IlchonProfile(nickname, review));
        return true;
    }

    public Optional<IlchonProfile> profile(String owner, String other) {
        return Optional.ofNullable(profiles.get(directed(owner, other)));
    }

    /** 양방향 일촌 여부. 공개범위 판정(다이어리)이 이 값을 쓴다. */
    public boolean isIlchon(String a, String b) {
        return adjacency.getOrDefault(a, Set.of()).contains(b);
    }

    /** 한 사용자의 일촌 목록(이름 오름차순). 미니홈피 화면 렌더가 쓴다. */
    public List<String> ilchonsOf(String user) {
        return new ArrayList<>(new TreeSet<>(adjacency.getOrDefault(user, Set.of())));
    }

    public IlchonStatus status(String a, String b) {
        if (isIlchon(a, b)) {
            return IlchonStatus.ACCEPTED;
        }
        if (pending.containsKey(directed(a, b)) || pending.containsKey(directed(b, a))) {
            return IlchonStatus.PENDING;
        }
        return IlchonStatus.NONE;
    }

    private String directed(String from, String to) {
        return from + " " + to;
    }
}
