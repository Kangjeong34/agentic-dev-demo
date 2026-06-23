import com.datasense.cyworld.screen.MinihompyFixture;

import java.nio.file.Files;
import java.nio.file.Path;

/** 미니홈피 메인 화면의 캐노니컬 스냅샷을 고정 시나리오로부터 1회 생성/갱신한다. */
public class SnapshotGen {
    public static void main(String[] args) throws Exception {
        String html = MinihompyFixture.render();
        Path out = Path.of("sdd/04_verify/10_test/ui_parity/minihompy_main.html");
        Files.createDirectories(out.getParent());
        Files.writeString(out, html);
        System.out.println("snapshot written: " + out + " (" + html.length() + " bytes)");
    }
}
