package com.datasense.cyworld.guestbook;

/** 방명록 글 1건. ownerId=미니홈피 주인, authorId=작성자. */
public record GuestbookEntry(long id, String ownerId, String authorId, String content, boolean secret) {
}
