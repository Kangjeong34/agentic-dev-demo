package com.datasense.cyworld.dotori;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 도토리 지갑: 충전·차감·잔액·멱등 결제.
 * auth 데모의 OTP 멱등성과 동일한 가드레일: paymentKey 로 중복 거래를 막는다.
 * (← dotori AC-1 ~ AC-5)
 */
public class DotoriService {

    /** userId -> 잔액. 음수 잔액은 발생하지 않는다(AC-5). */
    private final Map<String, Long> balances = new ConcurrentHashMap<>();
    /** paymentKey -> 최초 거래 결과. 멱등성 보장(AC-4). */
    private final Map<String, DotoriResult> processed = new ConcurrentHashMap<>();

    /** 현재 잔액. */
    public long balance(String userId) {
        return balances.getOrDefault(userId, 0L);
    }

    /** 도토리 충전. 동일 paymentKey 재요청은 최초 결과를 그대로 반환한다(AC-1·AC-4). */
    public synchronized DotoriResult charge(String userId, long amount, String paymentKey) {
        DotoriResult prior = processed.get(paymentKey);
        if (prior != null) {
            return prior;
        }
        if (amount <= 0) {
            return DotoriResult.rejected("invalid_amount", balance(userId));
        }
        long after = balance(userId) + amount;
        balances.put(userId, after);
        DotoriResult result = DotoriResult.charged(after);
        processed.put(paymentKey, result);
        return result;
    }

    /**
     * 도토리 차감. 잔액 부족이면 거부하고 잔액을 바꾸지 않는다(AC-3).
     * 동일 paymentKey 재요청은 최초 결과를 그대로 반환한다(AC-4). 음수 잔액 불가(AC-5).
     */
    public synchronized DotoriResult spend(String userId, long amount, String paymentKey) {
        DotoriResult prior = processed.get(paymentKey);
        if (prior != null) {
            return prior;
        }
        long current = balance(userId);
        if (amount <= 0) {
            return DotoriResult.rejected("invalid_amount", current);
        }
        if (current < amount) {
            // 거부도 멱등 처리: 같은 키의 재시도가 우회 차감되지 않도록 결과를 고정한다.
            DotoriResult rejected = DotoriResult.rejected("insufficient_balance", current);
            processed.put(paymentKey, rejected);
            return rejected;
        }
        long after = current - amount;
        balances.put(userId, after);
        DotoriResult result = DotoriResult.spent(after);
        processed.put(paymentKey, result);
        return result;
    }
}
