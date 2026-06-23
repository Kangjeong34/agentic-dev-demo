package com.datasense.cyworld.screen;

import com.datasense.cyworld.diary.DiaryService;
import com.datasense.cyworld.diary.Visibility;
import com.datasense.cyworld.dotori.DotoriService;
import com.datasense.cyworld.guestbook.GuestbookService;
import com.datasense.cyworld.ilchon.IlchonService;
import com.datasense.cyworld.miniroom.MiniroomService;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 미니홈피 메인 화면 UI parity 게이트.
 * 강의 데모의 Playwright exactness 를 브라우저 비가용 환경에서 결정적 HTML 스냅샷 대조로 대체한다.
 * (← minihompy_main 화면 명세 SCR-1 ~ SCR-5, 기능 AC: diary/ilchon/dotori/miniroom)
 */
class MinihompyScreenParityTest {

    private static final Path SNAPSHOT =
            Path.of("sdd/04_verify/10_test/ui_parity/minihompy_main.html");

    @Test
    void render_matches_canonical_snapshot() throws Exception {
        String want = Files.readString(SNAPSHOT).strip();
        String got = MinihompyFixture.render().strip();
        assertEquals(want, got, "미니홈피 메인 렌더가 캐노니컬 스냅샷과 불일치 (UI parity FAIL)");
    }

    @Test
    void ilchon_viewer_sees_all_and_ilchon_but_not_private() { // SCR-1·SCR-2 (← diary AC-2/AC-3)
        String html = MinihompyFixture.render();
        assertTrue(html.contains("싸이 미니홈피 오픈!"), "ALL 글은 일촌에게 보여야 한다");
        assertTrue(html.contains("일촌만 보는 비밀 이야기"), "ILCHON 글은 일촌에게 보여야 한다");
        assertFalse(html.contains("혼자 적는 메모"), "PRIVATE 글은 일촌에게도 숨겨야 한다");
    }

    @Test
    void stranger_viewer_sees_only_public() { // SCR-2 (← diary AC-2): 비일촌 분기
        IlchonService ilchon = new IlchonService();
        DotoriService dotori = new DotoriService();
        MiniroomService miniroom = new MiniroomService(dotori);
        DiaryService diary = new DiaryService(ilchon);
        GuestbookService guestbook = new GuestbookService();
        diary.write("minsu", "전체공개 글", Visibility.ALL);
        diary.write("minsu", "일촌공개 글", Visibility.ILCHON);

        MinihompyProfile profile =
                new MinihompyProfile("minsu", "민수의 미니홈피", "hi", 0, 0);
        String html = MinihompyRenderer.render(
                profile, "stranger", ilchon, diary, guestbook, dotori, miniroom);

        assertTrue(html.contains("전체공개 글"));
        assertFalse(html.contains("일촌공개 글"), "비일촌에게 ILCHON 글은 숨겨야 한다");
    }
}
