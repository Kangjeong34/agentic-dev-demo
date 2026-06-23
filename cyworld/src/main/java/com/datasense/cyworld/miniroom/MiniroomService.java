package com.datasense.cyworld.miniroom;

import com.datasense.cyworld.dotori.DotoriResult;
import com.datasense.cyworld.dotori.DotoriService;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 미니룸/BGM: 도토리 차감으로 아이템을 구매하고, 보유 아이템을 배치·대표곡 지정한다.
 * 구매는 도토리(DotoriService) 차감 흐름을 재사용한다.
 * (← miniroom AC-1 ~ AC-5)
 */
public class MiniroomService {

    private final DotoriService dotori;
    private final Map<String, Item> catalog = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> owned = new ConcurrentHashMap<>();
    private final Map<String, MiniroomLayout> layouts = new ConcurrentHashMap<>();

    public MiniroomService(DotoriService dotori) {
        this.dotori = dotori;
    }

    /** 상점 아이템 등록(시드 데이터). */
    public void register(Item item) {
        catalog.put(item.id(), item);
    }

    public Optional<Item> item(String itemId) {
        return Optional.ofNullable(catalog.get(itemId));
    }

    /**
     * 아이템 구매: 도토리 차감 성공 시 보유 목록에 추가(AC-1).
     * 잔액 부족이면 거부하고 지급하지 않는다(AC-2, ← dotori AC-3).
     * paymentKey 로 중복 차감을 막는다(← dotori AC-4).
     */
    public BuyResult buy(String userId, String itemId, String paymentKey) {
        Item item = catalog.get(itemId);
        if (item == null) {
            return BuyResult.rejected("no_such_item", dotori.balance(userId));
        }
        DotoriResult pay = dotori.spend(userId, item.price(), paymentKey);
        if (!pay.isSuccess()) {
            return BuyResult.rejected(pay.reason(), pay.balance());
        }
        owned.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(itemId);
        return BuyResult.granted(pay.balance());
    }

    public boolean owns(String userId, String itemId) {
        return owned.getOrDefault(userId, Set.of()).contains(itemId);
    }

    public Set<String> ownedItems(String userId) {
        return new HashSet<>(owned.getOrDefault(userId, Set.of()));
    }

    /** 보유 아이템만 배치 가능(AC-3·AC-4). */
    public boolean place(String userId, String itemId) {
        if (!owns(userId, itemId)) {
            return false;
        }
        layouts.computeIfAbsent(userId, k -> new MiniroomLayout()).place(itemId);
        return true;
    }

    /** 보유한 BGM 1곡을 대표곡으로 지정(AC-5). */
    public boolean setRepresentativeBgm(String userId, String bgmItemId) {
        Item item = catalog.get(bgmItemId);
        if (item == null || item.type() != ItemType.BGM || !owns(userId, bgmItemId)) {
            return false;
        }
        layouts.computeIfAbsent(userId, k -> new MiniroomLayout()).setRepresentativeBgmId(bgmItemId);
        return true;
    }

    public MiniroomLayout layout(String userId) {
        return layouts.computeIfAbsent(userId, k -> new MiniroomLayout());
    }
}
