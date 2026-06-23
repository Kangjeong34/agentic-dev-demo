package com.datasense.cyworld.miniroom;

/** 상점 아이템. price 는 도토리 가격. */
public record Item(String id, String name, ItemType type, long price) {
}
