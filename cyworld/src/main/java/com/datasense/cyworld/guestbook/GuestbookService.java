package com.datasense.cyworld.guestbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 방명록: 작성·비밀글 노출 분기·삭제 권한.
 * (← guestbook AC-1 ~ AC-5)
 */
public class GuestbookService {

    private final Map<Long, GuestbookEntry> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    /** 방명록 작성(AC-1·AC-2). */
    public GuestbookEntry leave(String ownerId, String authorId, String content, boolean secret) {
        long id = seq.incrementAndGet();
        GuestbookEntry entry = new GuestbookEntry(id, ownerId, authorId, content, secret);
        store.put(id, entry);
        return entry;
    }

    /**
     * 방문자(viewer) 관점의 목록. 비밀글은 주인·작성자에게만 내용을 노출하고
     * 그 외에게는 content=null 로 숨긴다(AC-2).
     */
    public List<GuestbookView> list(String ownerId, String viewerId) {
        List<GuestbookView> result = new ArrayList<>();
        for (GuestbookEntry e : store.values()) {
            if (!e.ownerId().equals(ownerId)) {
                continue;
            }
            boolean canSee = !e.secret()
                    || viewerId.equals(e.ownerId())
                    || viewerId.equals(e.authorId());
            String content = canSee ? e.content() : null;
            result.add(new GuestbookView(e.id(), e.authorId(), content, e.secret()));
        }
        result.sort((a, b) -> Long.compare(a.id(), b.id()));
        return result;
    }

    /**
     * 삭제. 미니홈피 주인은 임의의 글을, 작성자는 자신의 글을 지울 수 있다(AC-3·AC-4).
     * 주인도 작성자도 아니면 거부(AC-5).
     */
    public boolean delete(long entryId, String actorId) {
        GuestbookEntry e = store.get(entryId);
        if (e == null) {
            return false;
        }
        if (!actorId.equals(e.ownerId()) && !actorId.equals(e.authorId())) {
            return false;
        }
        store.remove(entryId);
        return true;
    }
}
