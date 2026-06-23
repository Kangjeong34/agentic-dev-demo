package com.datasense.cyworld.dotori;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** 도토리 결제 AC-1 ~ AC-5 검증. */
class DotoriServiceTest {

    @Test
    void charge_increases_balance() { // AC-1
        DotoriService svc = new DotoriService();
        DotoriResult r = svc.charge("alice", 100, "pk-1");
        assertEquals(DotoriStatus.CHARGED, r.status());
        assertEquals(100, svc.balance("alice"));
    }

    @Test
    void spend_decreases_balance() { // AC-2
        DotoriService svc = new DotoriService();
        svc.charge("alice", 100, "pk-1");
        DotoriResult r = svc.spend("alice", 30, "pk-2");
        assertEquals(DotoriStatus.SPENT, r.status());
        assertEquals(70, svc.balance("alice"));
    }

    @Test
    void spend_rejected_when_insufficient() { // AC-3
        DotoriService svc = new DotoriService();
        svc.charge("alice", 20, "pk-1");
        DotoriResult r = svc.spend("alice", 50, "pk-2");
        assertEquals(DotoriStatus.REJECTED, r.status());
        assertEquals("insufficient_balance", r.reason());
        assertEquals(20, svc.balance("alice")); // 잔액 불변
    }

    @Test
    void duplicate_paymentKey_idempotent() { // AC-4
        DotoriService svc = new DotoriService();
        svc.charge("alice", 100, "pk-1");
        DotoriResult first = svc.spend("alice", 40, "pk-pay");
        DotoriResult again = svc.spend("alice", 40, "pk-pay"); // 같은 키 재요청
        assertEquals(DotoriStatus.SPENT, first.status());
        assertEquals(first, again);
        assertEquals(60, svc.balance("alice")); // 한 번만 차감
    }

    @Test
    void balance_never_negative() { // AC-5
        DotoriService svc = new DotoriService();
        svc.charge("alice", 10, "pk-1");
        svc.spend("alice", 999, "pk-2"); // 거부됨
        assertTrue(svc.balance("alice") >= 0);
        assertEquals(10, svc.balance("alice"));
    }
}
