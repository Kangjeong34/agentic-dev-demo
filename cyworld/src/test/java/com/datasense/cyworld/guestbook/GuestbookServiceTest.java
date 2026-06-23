package com.datasense.cyworld.guestbook;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 방명록 작성·비밀글·삭제 권한 AC-1 ~ AC-5 검증. */
class GuestbookServiceTest {

    @Test
    void leave_entry() { // AC-1
        GuestbookService svc = new GuestbookService();
        GuestbookEntry e = svc.leave("alice", "bob", "다녀가요~", false);
        assertEquals("alice", e.ownerId());
        assertEquals("bob", e.authorId());
        assertEquals(1, svc.list("alice", "alice").size());
    }

    @Test
    void secret_visible_to_owner_and_author() { // AC-2
        GuestbookService svc = new GuestbookService();
        svc.leave("alice", "bob", "비밀 인사", true);

        // 주인 alice: 내용 보임
        assertEquals("비밀 인사", svc.list("alice", "alice").get(0).content());
        // 작성자 bob: 내용 보임
        assertEquals("비밀 인사", svc.list("alice", "bob").get(0).content());
        // 제3자: 숨김
        GuestbookView hidden = svc.list("alice", "carol").get(0);
        assertNull(hidden.content());
        assertTrue(hidden.isHidden());
    }

    @Test
    void owner_can_delete_any() { // AC-3
        GuestbookService svc = new GuestbookService();
        GuestbookEntry e = svc.leave("alice", "bob", "글", false);
        assertTrue(svc.delete(e.id(), "alice")); // 주인이 타인 글 삭제
        assertTrue(svc.list("alice", "alice").isEmpty());
    }

    @Test
    void author_can_delete_own() { // AC-4
        GuestbookService svc = new GuestbookService();
        GuestbookEntry e = svc.leave("alice", "bob", "글", false);
        assertTrue(svc.delete(e.id(), "bob")); // 작성자가 자기 글 삭제
        assertTrue(svc.list("alice", "alice").isEmpty());
    }

    @Test
    void stranger_cannot_delete() { // AC-5
        GuestbookService svc = new GuestbookService();
        GuestbookEntry e = svc.leave("alice", "bob", "글", false);
        assertFalse(svc.delete(e.id(), "mallory"));
        List<GuestbookView> remaining = svc.list("alice", "alice");
        assertEquals(1, remaining.size());
    }
}
