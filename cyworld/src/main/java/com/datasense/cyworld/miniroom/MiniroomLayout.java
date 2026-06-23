package com.datasense.cyworld.miniroom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 한 사용자의 미니룸 배치 상태 + 대표 BGM. */
public class MiniroomLayout {
    private final List<String> placedItemIds = new ArrayList<>();
    private String representativeBgmId;

    public List<String> getPlacedItemIds() {
        return Collections.unmodifiableList(placedItemIds);
    }

    void place(String itemId) {
        if (!placedItemIds.contains(itemId)) {
            placedItemIds.add(itemId);
        }
    }

    public String getRepresentativeBgmId() {
        return representativeBgmId;
    }

    void setRepresentativeBgmId(String bgmId) {
        this.representativeBgmId = bgmId;
    }
}
