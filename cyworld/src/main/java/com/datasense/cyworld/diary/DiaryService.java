package com.datasense.cyworld.diary;

import com.datasense.cyworld.ilchon.IlchonService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 다이어리: 작성·공개범위 판정·권한.
 * 공개범위(ILCHON) 판정은 일촌 관계(IlchonService)에 의존한다.
 * (← diary AC-1 ~ AC-5)
 */
public class DiaryService {

    private final IlchonService ilchon;
    private final Map<Long, Diary> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    public DiaryService(IlchonService ilchon) {
        this.ilchon = ilchon;
    }

    /** 글 작성. 공개범위와 함께 1건 저장(AC-1). */
    public Diary write(String ownerId, String content, Visibility visibility) {
        long id = seq.incrementAndGet();
        Diary diary = new Diary(id, ownerId, content, visibility);
        store.put(id, diary);
        return diary;
    }

    /** 방문자(viewer)가 열람 가능한 owner 의 글만 반환(AC-2). */
    public List<Diary> list(String ownerId, String viewerId) {
        List<Diary> result = new ArrayList<>();
        for (Diary d : store.values()) {
            if (d.getOwnerId().equals(ownerId) && canView(d, viewerId)) {
                result.add(d);
            }
        }
        result.sort((a, b) -> Long.compare(a.getId(), b.getId()));
        return result;
    }

    /** 단건 조회. 열람 권한이 없으면 비어 있음(AC-3). */
    public Optional<Diary> get(long diaryId, String viewerId) {
        Diary d = store.get(diaryId);
        if (d == null || !canView(d, viewerId)) {
            return Optional.empty();
        }
        return Optional.of(d);
    }

    /** 주인만 수정 가능. 그 외 거부(AC-4·AC-5). */
    public boolean edit(long diaryId, String editorId, String content, Visibility visibility) {
        Diary d = store.get(diaryId);
        if (d == null || !d.getOwnerId().equals(editorId)) {
            return false;
        }
        d.update(content, visibility);
        return true;
    }

    /** 주인만 삭제 가능. 그 외 거부(AC-4·AC-5). */
    public boolean delete(long diaryId, String editorId) {
        Diary d = store.get(diaryId);
        if (d == null || !d.getOwnerId().equals(editorId)) {
            return false;
        }
        store.remove(diaryId);
        return true;
    }

    /** 공개범위 판정: 주인은 항상, ALL 은 모두, ILCHON 은 일촌만, PRIVATE 은 주인만. */
    public boolean canView(Diary diary, String viewerId) {
        if (diary.getOwnerId().equals(viewerId)) {
            return true;
        }
        return switch (diary.getVisibility()) {
            case ALL -> true;
            case ILCHON -> ilchon.isIlchon(diary.getOwnerId(), viewerId);
            case PRIVATE -> false;
        };
    }
}
