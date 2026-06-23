package com.datasense.cyworld.dotori;

/**
 * 도토리 거래 결과. balance 는 거래 후 잔액(거부 시 변경 없는 현재 잔액).
 * (← dotori AC-1·AC-2·AC-3)
 */
public record DotoriResult(DotoriStatus status, String reason, long balance) {

    public static DotoriResult charged(long balance) {
        return new DotoriResult(DotoriStatus.CHARGED, null, balance);
    }

    public static DotoriResult spent(long balance) {
        return new DotoriResult(DotoriStatus.SPENT, null, balance);
    }

    public static DotoriResult rejected(String reason, long balance) {
        return new DotoriResult(DotoriStatus.REJECTED, reason, balance);
    }

    public boolean isSuccess() {
        return status != DotoriStatus.REJECTED;
    }
}
