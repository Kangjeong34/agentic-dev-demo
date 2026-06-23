package com.datasense.cyworld.screen;

import com.datasense.cyworld.diary.DiaryService;
import com.datasense.cyworld.diary.Visibility;
import com.datasense.cyworld.dotori.DotoriService;
import com.datasense.cyworld.guestbook.GuestbookService;
import com.datasense.cyworld.ilchon.IlchonService;
import com.datasense.cyworld.miniroom.Item;
import com.datasense.cyworld.miniroom.ItemType;
import com.datasense.cyworld.miniroom.MiniroomService;

/**
 * 미니홈피 메인 화면 UI parity 의 결정적 고정 시나리오.
 * 스냅샷 생성과 parity 테스트가 동일한 이 시나리오를 공유해 drift 를 막는다.
 */
public final class MinihompyFixture {

    public static final String OWNER = "minsu";
    public static final String VIEWER = "yuna"; // 일촌 방문자

    private MinihompyFixture() {
    }

    /** 고정 시나리오를 구성하고 렌더된 HTML 을 반환한다. */
    public static String render() {
        IlchonService ilchon = new IlchonService();
        DotoriService dotori = new DotoriService();
        MiniroomService miniroom = new MiniroomService(dotori);
        DiaryService diary = new DiaryService(ilchon);
        GuestbookService guestbook = new GuestbookService();

        // 도토리 충전 → 미니룸 아이템 구매(차감)
        dotori.charge(OWNER, 2000, "pk-charge");
        miniroom.register(new Item("sofa", "분홍 소파", ItemType.ROOM, 50));
        miniroom.register(new Item("bgm-spring", "봄날 BGM", ItemType.BGM, 30));
        miniroom.buy(OWNER, "sofa", "pk-buy-sofa");
        miniroom.buy(OWNER, "bgm-spring", "pk-buy-bgm");
        miniroom.place(OWNER, "sofa");
        miniroom.setRepresentativeBgm(OWNER, "bgm-spring");

        // 다이어리: 공개범위 3종
        diary.write(OWNER, "싸이 미니홈피 오픈!", Visibility.ALL);
        diary.write(OWNER, "일촌만 보는 비밀 이야기", Visibility.ILCHON);
        diary.write(OWNER, "혼자 적는 메모", Visibility.PRIVATE);

        // 일촌: 2명 + 일촌명
        ilchon.request(OWNER, "yuna");
        ilchon.accept(OWNER, "yuna");
        ilchon.setProfile(OWNER, "yuna", "단짝", "10년지기");
        ilchon.request(OWNER, "jihun");
        ilchon.accept(OWNER, "jihun");
        ilchon.setProfile(OWNER, "jihun", "동네친구", "옆집 친구");

        // 방명록: 공개 2 + 비밀 1 (방문자 yuna 는 비밀글 미열람)
        guestbook.leave(OWNER, "yuna", "민수야 놀러왔어~ 미니룸 예쁘다!", false);
        guestbook.leave(OWNER, "jihun", "오늘 즐거웠어 ㅎㅎ", false);
        guestbook.leave(OWNER, "somin", "우리끼리 비밀 얘기", true);

        MinihompyProfile profile = new MinihompyProfile(
                OWNER, "민수의 미니홈피", "오늘도 화이팅 :)", 12, 3456);

        return MinihompyRenderer.render(profile, VIEWER, ilchon, diary, guestbook, dotori, miniroom);
    }
}
