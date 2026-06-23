package com.datasense.cyworld.diary;

import com.datasense.cyworld.ilchon.IlchonService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 다이어리 공개범위·권한 AC-1 ~ AC-5 검증. */
class DiaryServiceTest {

    private DiaryService newService(IlchonService ilchon) {
        return new DiaryService(ilchon);
    }

    @Test
    void write_with_visibility() { // AC-1
        DiaryService svc = newService(new IlchonService());
        Diary d = svc.write("alice", "오늘의 일기", Visibility.ALL);
        assertTrue(d.getId() > 0);
        assertEquals(Visibility.ALL, d.getVisibility());
    }

    @Test
    void list_respects_visibility() { // AC-2
        IlchonService ilchon = new IlchonService();
        DiaryService svc = newService(ilchon);
        svc.write("alice", "전체공개", Visibility.ALL);
        svc.write("alice", "일촌공개", Visibility.ILCHON);
        svc.write("alice", "비공개", Visibility.PRIVATE);

        // 비일촌 stranger: ALL 1건만
        assertEquals(1, svc.list("alice", "stranger").size());

        // 주인 본인: 3건 전부
        assertEquals(3, svc.list("alice", "alice").size());

        // 일촌 bob: ALL + ILCHON = 2건
        ilchon.request("alice", "bob");
        ilchon.accept("alice", "bob");
        List<Diary> bobView = svc.list("alice", "bob");
        assertEquals(2, bobView.size());
    }

    @Test
    void ilchon_only_blocks_stranger() { // AC-3
        IlchonService ilchon = new IlchonService();
        DiaryService svc = newService(ilchon);
        Diary d = svc.write("alice", "일촌만", Visibility.ILCHON);
        assertTrue(svc.get(d.getId(), "stranger").isEmpty());
        ilchon.request("alice", "bob");
        ilchon.accept("alice", "bob");
        assertTrue(svc.get(d.getId(), "bob").isPresent());
    }

    @Test
    void owner_can_edit_delete() { // AC-4
        DiaryService svc = newService(new IlchonService());
        Diary d = svc.write("alice", "원본", Visibility.ALL);
        assertTrue(svc.edit(d.getId(), "alice", "수정본", Visibility.PRIVATE));
        assertEquals("수정본", svc.get(d.getId(), "alice").orElseThrow().getContent());
        assertTrue(svc.delete(d.getId(), "alice"));
        assertTrue(svc.get(d.getId(), "alice").isEmpty());
    }

    @Test
    void non_owner_cannot_modify() { // AC-5
        DiaryService svc = newService(new IlchonService());
        Diary d = svc.write("alice", "원본", Visibility.ALL);
        assertFalse(svc.edit(d.getId(), "mallory", "변조", Visibility.ALL));
        assertFalse(svc.delete(d.getId(), "mallory"));
        assertEquals("원본", svc.get(d.getId(), "alice").orElseThrow().getContent());
    }
}
