package com.datasense.cyworld.miniroom;

import com.datasense.cyworld.dotori.DotoriService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** 미니룸/BGM 구매·배치 AC-1 ~ AC-5 검증. */
class MiniroomServiceTest {

    private MiniroomService newService(DotoriService dotori) {
        MiniroomService svc = new MiniroomService(dotori);
        svc.register(new Item("sofa", "분홍 소파", ItemType.ROOM, 50));
        svc.register(new Item("bgm-spring", "봄날 BGM", ItemType.BGM, 30));
        return svc;
    }

    @Test
    void buy_charges_dotori_and_grants() { // AC-1
        DotoriService dotori = new DotoriService();
        dotori.charge("alice", 100, "pk-charge");
        MiniroomService svc = newService(dotori);
        BuyResult r = svc.buy("alice", "sofa", "pk-buy");
        assertTrue(r.granted());
        assertEquals(50, r.balanceAfter()); // 100 - 50
        assertTrue(svc.owns("alice", "sofa"));
    }

    @Test
    void buy_rejected_when_insufficient() { // AC-2
        DotoriService dotori = new DotoriService();
        dotori.charge("alice", 10, "pk-charge");
        MiniroomService svc = newService(dotori);
        BuyResult r = svc.buy("alice", "sofa", "pk-buy"); // 가격 50 > 잔액 10
        assertFalse(r.granted());
        assertFalse(svc.owns("alice", "sofa")); // 지급 안 됨
        assertEquals(10, dotori.balance("alice")); // 잔액 불변
    }

    @Test
    void place_and_save_layout() { // AC-3
        DotoriService dotori = new DotoriService();
        dotori.charge("alice", 100, "pk-charge");
        MiniroomService svc = newService(dotori);
        svc.buy("alice", "sofa", "pk-buy");
        assertTrue(svc.place("alice", "sofa"));
        assertTrue(svc.layout("alice").getPlacedItemIds().contains("sofa"));
    }

    @Test
    void cannot_place_unowned() { // AC-4
        DotoriService dotori = new DotoriService();
        MiniroomService svc = newService(dotori);
        assertFalse(svc.place("alice", "sofa")); // 미보유
        assertFalse(svc.layout("alice").getPlacedItemIds().contains("sofa"));
    }

    @Test
    void set_representative_bgm() { // AC-5
        DotoriService dotori = new DotoriService();
        dotori.charge("alice", 100, "pk-charge");
        MiniroomService svc = newService(dotori);
        svc.buy("alice", "bgm-spring", "pk-buy");
        assertTrue(svc.setRepresentativeBgm("alice", "bgm-spring"));
        assertEquals("bgm-spring", svc.layout("alice").getRepresentativeBgmId());
        // 보유하지 않은 BGM은 거부
        assertFalse(svc.setRepresentativeBgm("alice", "bgm-unknown"));
    }

    @Test
    void duplicate_buy_click_idempotent() { // 회귀: 도토리 멱등성 재사용
        DotoriService dotori = new DotoriService();
        dotori.charge("alice", 100, "pk-charge");
        MiniroomService svc = newService(dotori);
        svc.buy("alice", "sofa", "pk-buy");
        svc.buy("alice", "sofa", "pk-buy"); // 같은 결제키 더블클릭
        assertEquals(50, dotori.balance("alice")); // 한 번만 차감
    }
}
