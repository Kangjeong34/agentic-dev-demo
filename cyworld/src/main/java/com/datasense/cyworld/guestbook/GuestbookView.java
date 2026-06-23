package com.datasense.cyworld.guestbook;

/**
 * 방문자 관점의 방명록 항목. 비밀글이고 볼 권한이 없으면 content=null 로 숨긴다.
 * (← guestbook AC-2)
 */
public record GuestbookView(long id, String authorId, String content, boolean secret) {

    public boolean isHidden() {
        return secret && content == null;
    }
}
