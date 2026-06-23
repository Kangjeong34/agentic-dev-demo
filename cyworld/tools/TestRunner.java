import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

/**
 * Gradle 데몬이 JDK 26과 호환되지 않아, JUnit Platform Launcher API로 직접 테스트를 실행하고
 * auth 데모와 동일한 tmp/proof-results.json 게이트 산출물을 생성하는 임시 러너.
 */
public class TestRunner {
    public static void main(String[] args) throws Exception {
        LauncherDiscoveryRequest req = request()
                .selectors(selectPackage("com.datasense.cyworld"))
                .build();
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(req);

        TestExecutionSummary summary = listener.getSummary();
        summary.printTo(new PrintWriter(System.out));
        summary.printFailuresTo(new PrintWriter(System.out));

        long passed = summary.getTestsSucceededCount();
        long failed = summary.getTestsFailedCount();
        long total = passed + failed;

        List<String> rows = new ArrayList<>();
        summary.getFailures().forEach(f ->
                rows.add(jsonRow(f.getTestIdentifier().getDisplayName(), "FAIL")));
        // 성공 케이스는 개수만 신뢰원으로 기록(런처 요약은 개별 성공 식별자를 보존하지 않음).

        String json = "{\n" +
                "  \"gate\": \"junit-platform launcher (JDK26 fallback)\",\n" +
                "  \"total\": " + total + ",\n" +
                "  \"passed\": " + passed + ",\n" +
                "  \"failed\": " + failed + ",\n" +
                "  \"status\": \"" + (failed == 0 ? "PASS" : "FAIL") + "\"\n" +
                "}\n";
        Path out = Path.of("tmp/proof-results.json");
        Files.createDirectories(out.getParent());
        Files.writeString(out, json);

        System.out.println("\nPROOF: total=" + total + " passed=" + passed + " failed=" + failed
                + " status=" + (failed == 0 ? "PASS" : "FAIL"));
        System.exit(failed == 0 ? 0 : 1);
    }

    private static String jsonRow(String name, String status) {
        return "{\"name\":\"" + name.replace("\"", "'") + "\",\"status\":\"" + status + "\"}";
    }
}
