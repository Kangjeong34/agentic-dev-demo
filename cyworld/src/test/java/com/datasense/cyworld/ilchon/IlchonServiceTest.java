package com.datasense.cyworld.ilchon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** 일촌 관계 AC-1 ~ AC-6 검증. */
class IlchonServiceTest {

    @Test
    void request_creates_pending() { // AC-1
        IlchonService svc = new IlchonService();
        assertEquals(IlchonStatus.PENDING, svc.request("alice", "bob"));
        assertEquals(IlchonStatus.PENDING, svc.status("alice", "bob"));
        assertFalse(svc.isIlchon("alice", "bob"));
    }

    @Test
    void accept_makes_bidirectional() { // AC-2
        IlchonService svc = new IlchonService();
        svc.request("alice", "bob");
        assertTrue(svc.accept("alice", "bob"));
        assertTrue(svc.isIlchon("alice", "bob"));
        assertTrue(svc.isIlchon("bob", "alice")); // 양방향
    }

    @Test
    void reject_removes_request() { // AC-3
        IlchonService svc = new IlchonService();
        svc.request("alice", "bob");
        assertTrue(svc.reject("alice", "bob"));
        assertEquals(IlchonStatus.NONE, svc.status("alice", "bob"));
        assertFalse(svc.isIlchon("alice", "bob"));
    }

    @Test
    void duplicate_request_idempotent() { // AC-4
        IlchonService svc = new IlchonService();
        svc.request("alice", "bob");
        svc.request("alice", "bob"); // 중복 신청
        svc.accept("alice", "bob");
        assertTrue(svc.isIlchon("alice", "bob"));
        // 이미 일촌인데 또 신청 → ACCEPTED 반환, 중복 관계 없음
        assertEquals(IlchonStatus.ACCEPTED, svc.request("alice", "bob"));
    }

    @Test
    void break_removes_both_sides() { // AC-5
        IlchonService svc = new IlchonService();
        svc.request("alice", "bob");
        svc.accept("alice", "bob");
        assertTrue(svc.breakUp("alice", "bob"));
        assertFalse(svc.isIlchon("alice", "bob"));
        assertFalse(svc.isIlchon("bob", "alice"));
    }

    @Test
    void nickname_and_review_stored() { // AC-6
        IlchonService svc = new IlchonService();
        svc.request("alice", "bob");
        svc.accept("alice", "bob");
        assertTrue(svc.setProfile("alice", "bob", "베프", "둘도 없는 친구"));
        IlchonProfile p = svc.profile("alice", "bob").orElseThrow();
        assertEquals("베프", p.nickname());
        assertEquals("둘도 없는 친구", p.review());
        // 일촌이 아니면 프로필 설정 거부
        assertFalse(svc.setProfile("alice", "carol", "x", "y"));
    }
}
