import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

/**
 * Gradle 데몬이 JDK 26과 호환되지 않아, JUnit Platform Launcher API로 직접 테스트를 실행하고
 * auth 데모와 동일한 tmp/proof-results.json 게이트 산출물을 생성하는 임시 러너.
 *
 * 산출 JSON 은 집계(total/passed/failed/status)에 더해 클래스별 통계(classes[])를 포함한다.
 * 이 classes[] 가 sdd/99_toolchain 의 gen_proof_evidence.py 입력이 된다.
 */
public class TestRunner {
    public static void main(String[] args) throws Exception {
        LauncherDiscoveryRequest req = request()
                .selectors(selectPackage("com.datasense.cyworld"))
                .build();
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
        ClassTally tally = new ClassTally();
        launcher.registerTestExecutionListeners(summaryListener, tally);
        launcher.execute(req);

        TestExecutionSummary summary = summaryListener.getSummary();
        summary.printTo(new PrintWriter(System.out));
        summary.printFailuresTo(new PrintWriter(System.out));

        long passed = summary.getTestsSucceededCount();
        long failed = summary.getTestsFailedCount();
        long total = passed + failed;

        String json = "{\n" +
                "  \"gate\": \"junit-platform launcher (JDK26 fallback)\",\n" +
                "  \"total\": " + total + ",\n" +
                "  \"passed\": " + passed + ",\n" +
                "  \"failed\": " + failed + ",\n" +
                "  \"status\": \"" + (failed == 0 ? "PASS" : "FAIL") + "\",\n" +
                "  \"classes\": [\n" + tally.toJsonRows() + "  ]\n" +
                "}\n";
        Path out = Path.of("tmp/proof-results.json");
        Files.createDirectories(out.getParent());
        Files.writeString(out, json);

        System.out.println("\nPROOF: total=" + total + " passed=" + passed + " failed=" + failed
                + " status=" + (failed == 0 ? "PASS" : "FAIL"));
        System.exit(failed == 0 ? 0 : 1);
    }

    /** 테스트 클래스별 통과/실패 집계를 모은다(런처 요약이 보존하지 않는 per-class 통계). */
    static final class ClassTally implements TestExecutionListener {
        // className(short) -> [passed, failed]
        private final Map<String, long[]> byClass = new TreeMap<>();

        @Override
        public void executionFinished(TestIdentifier id, TestExecutionResult result) {
            if (!id.isTest()) return;
            String cls = id.getSource()
                    .filter(s -> s instanceof MethodSource)
                    .map(s -> ((MethodSource) s).getClassName())
                    .map(n -> n.substring(n.lastIndexOf('.') + 1))
                    .orElse("unknown");
            long[] t = byClass.computeIfAbsent(cls, k -> new long[2]);
            if (result.getStatus() == TestExecutionResult.Status.SUCCESSFUL) t[0]++;
            else t[1]++;
        }

        String toJsonRows() {
            StringBuilder sb = new StringBuilder();
            int i = 0, n = byClass.size();
            for (Map.Entry<String, long[]> e : byClass.entrySet()) {
                long p = e.getValue()[0], f = e.getValue()[1];
                sb.append("    {\"name\":\"").append(e.getKey()).append("\",")
                        .append("\"tests\":").append(p + f).append(",")
                        .append("\"passed\":").append(p).append(",")
                        .append("\"failed\":").append(f).append("}")
                        .append(++i < n ? ",\n" : "\n");
            }
            return sb.toString();
        }
    }
}
