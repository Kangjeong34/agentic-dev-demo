package dev.agentic.demo;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * spec.md(AC-1~4)를 만족하는 OTP 구현.
 *
 * <ul>
 *   <li>AC-1 정상 발급·검증 : 유효한 OTP로 가입하면 성공한다.</li>
 *   <li>AC-2 만료 OTP 거부  : 발급 후 {@code t - issuedAt > TTL_SECONDS} 면 거부한다.</li>
 *   <li>AC-3 5회 오류 잠금  : 5회 연속 오답이면 이후 정답도 거부한다(무차별 대입 차단).</li>
 *   <li>AC-4 재요청 멱등    : 같은 사용자가 두 번 가입해도 계정은 1개만 만든다.</li>
 * </ul>
 *
 * 시간은 실제 시계 대신 정수 t(초)로 주입하므로, 같은 입력이면 항상 같은 결과가 나온다.
 */
public class MyOtp implements Otp {

    /** OTP 유효 시간(초). 발급 후 이 시간을 넘기면 만료된다. */
    private static final int TTL_SECONDS = 300;
    /** 연속 오답 허용 횟수. 이 횟수에 도달하면 잠긴다. */
    private static final int MAX_ATTEMPTS = 5;

    /** 이메일별 발급 코드·발급 시각·연속 오답 횟수를 함께 보관한다. */
    private static final class Record {
        final String code;
        final int issuedAt;
        int failedAttempts;

        Record(String code, int issuedAt) {
            this.code = code;
            this.issuedAt = issuedAt;
        }
    }

    private final Map<String, Record> records = new HashMap<>();
    private final Set<String> created = new LinkedHashSet<>();

    @Override
    public String issue(String email, int t) {
        String code = "123456";
        // 재발급하면 발급 시각·오답 횟수를 새로 시작한다.
        records.put(email, new Record(code, t));
        return code;
    }

    @Override
    public boolean verify(String email, String code, int t) {
        Record rec = records.get(email);
        if (rec == null) {
            return false; // 발급된 적 없음
        }
        if (rec.failedAttempts >= MAX_ATTEMPTS) {
            return false; // AC-3: 잠김 — 정답이라도 거부
        }
        if (t - rec.issuedAt > TTL_SECONDS) {
            return false; // AC-2: 만료
        }
        if (!rec.code.equals(code)) {
            rec.failedAttempts++; // AC-3: 오답 누적
            return false;
        }
        return true; // AC-1: 정상
    }

    @Override
    public boolean signup(String email, String code, int t) {
        if (!verify(email, code, t)) {
            return false;
        }
        created.add(email); // AC-4: Set 이라 두 번 가입해도 1건
        return true;
    }

    @Override
    public Collection<String> created() {
        return created;
    }
}
