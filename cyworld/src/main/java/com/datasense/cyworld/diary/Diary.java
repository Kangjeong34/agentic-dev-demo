package com.datasense.cyworld.diary;

/** 다이어리 글 1건. */
public class Diary {
    private final long id;
    private final String ownerId;
    private String content;
    private Visibility visibility;

    public Diary(long id, String ownerId, String content, Visibility visibility) {
        this.id = id;
        this.ownerId = ownerId;
        this.content = content;
        this.visibility = visibility;
    }

    public long getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getContent() {
        return content;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    void update(String content, Visibility visibility) {
        this.content = content;
        this.visibility = visibility;
    }
}
