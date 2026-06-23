package com.datasense.cyworld.miniroom;

/** 미니룸 아이템 구매 결과. */
public record BuyResult(boolean granted, String reason, long balanceAfter) {

    public static BuyResult granted(long balanceAfter) {
        return new BuyResult(true, null, balanceAfter);
    }

    public static BuyResult rejected(String reason, long balanceAfter) {
        return new BuyResult(false, reason, balanceAfter);
    }
}
