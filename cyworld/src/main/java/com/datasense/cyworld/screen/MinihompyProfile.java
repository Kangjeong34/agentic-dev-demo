package com.datasense.cyworld.screen;

/**
 * 미니홈피 메인 화면 상단 프로필 데이터.
 * today/total 은 방문자 카운트(별도 도메인 없이 화면 입력값으로 취급).
 */
public record MinihompyProfile(
        String ownerId,
        String displayName,
        String statusMessage,
        long today,
        long total) {
}
