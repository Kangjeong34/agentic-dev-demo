package com.datasense.cyworld.screen;

import com.datasense.cyworld.diary.Diary;
import com.datasense.cyworld.diary.DiaryService;
import com.datasense.cyworld.dotori.DotoriService;
import com.datasense.cyworld.guestbook.GuestbookService;
import com.datasense.cyworld.guestbook.GuestbookView;
import com.datasense.cyworld.ilchon.IlchonService;
import com.datasense.cyworld.miniroom.MiniroomLayout;
import com.datasense.cyworld.miniroom.MiniroomService;

import java.util.List;

/**
 * 미니홈피 메인 화면(minihompy_main) 렌더러.
 * 도메인 상태(일촌·다이어리·방명록·도토리·미니룸)로부터 결정적 HTML 문서를 만든다.
 * 클래식 싸이월드 3단 레이아웃: 상단 TODAY/TOTAL · 좌(미니미 프로필) · 중앙(링바인더 페이지) · 우(CYWORLD HOME 메뉴).
 * 출력은 브라우저로 열람 가능하며, 동시에 UI parity 게이트의 대조 대상이다.
 * 화면 명세: sdd/01_planning/02_screen/minihompy_main_screen_spec.md (SCR-1 ~ SCR-5)
 */
public final class MinihompyRenderer {

    private MinihompyRenderer() {
    }

    public static String render(
            MinihompyProfile profile,
            String viewerId,
            IlchonService ilchon,
            DiaryService diary,
            GuestbookService guestbook,
            DotoriService dotori,
            MiniroomService miniroom) {

        String owner = profile.ownerId();
        List<Diary> diaries = diary.list(owner, viewerId);
        List<String> ilchons = ilchon.ilchonsOf(owner);
        List<GuestbookView> guestbookViews = guestbook.list(owner, viewerId);
        long balance = dotori.balance(owner);
        MiniroomLayout layout = miniroom.layout(owner);

        StringBuilder b = new StringBuilder();
        b.append("<!DOCTYPE html>\n");
        b.append("<html lang=\"ko\">\n");
        b.append("<head>\n");
        b.append("<meta charset=\"utf-8\"/>\n");
        b.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>\n");
        b.append("<title>").append(esc(profile.displayName())).append("</title>\n");
        b.append("<style>").append(STYLE).append("</style>\n");
        b.append("</head>\n");
        b.append("<body>\n");
        b.append("<div class=\"cyworld\" data-owner=\"").append(esc(owner)).append("\">\n");

        // ── 상단바: TODAY/TOTAL · 타이틀 · URL ──
        b.append("  <header class=\"topbar\">\n");
        b.append("    <div class=\"counts\">TODAY <b>").append(profile.today())
                .append("</b> / TOTAL <b>").append(profile.total()).append("</b></div>\n");
        b.append("    <div class=\"title\">사이좋은 사람들</div>\n");
        b.append("    <div class=\"url\">http://www.cyworld.com/").append(esc(owner)).append("</div>\n");
        b.append("  </header>\n");

        b.append("  <div class=\"columns\">\n");

        // ── 좌측: 미니미 프로필 바인더 ──
        b.append("    <aside class=\"left\">\n");
        b.append("      <div class=\"profile-card\">\n");
        b.append("        <div class=\"avatar\">🧑</div>\n");
        b.append("        <div class=\"today-is\">TODAY IS · 즐거움</div>\n");
        b.append("        <h1 class=\"dname\">").append(esc(profile.displayName())).append("</h1>\n");
        b.append("        <p class=\"status\">").append(esc(profile.statusMessage())).append("</p>\n");
        b.append("        <div class=\"history\"><h2>HISTORY</h2><p>")
                .append(esc(owner)).append("@cyworld.com</p></div>\n");
        b.append("        <div class=\"ilchon-pick\">\n");
        b.append("          <label>★ 나의 일촌 (").append(ilchons.size()).append(")</label>\n");
        b.append("          <select>\n");
        for (String other : ilchons) {
            String nickname = ilchon.profile(owner, other).map(p -> p.nickname()).orElse(other);
            b.append("            <option value=\"").append(esc(other)).append("\">")
                    .append(esc(nickname)).append("</option>\n");
        }
        b.append("          </select>\n");
        b.append("        </div>\n");
        b.append("      </div>\n");
        b.append("    </aside>\n");

        // ── 중앙: 링바인더 페이지 ──
        b.append("    <main class=\"center\">\n");
        b.append("      <div class=\"rings\"><i></i><i></i><i></i><i></i><i></i><i></i><i></i><i></i></div>\n");
        b.append("      <div class=\"pages\">\n");

        // Updated news (다이어리)
        b.append("        <section class=\"news\">\n");
        b.append("          <h2>Updated news <span>today story</span></h2>\n");
        b.append("          <ul class=\"diary-list\">\n");
        for (Diary d : diaries) {
            b.append("            <li data-id=\"").append(d.getId())
                    .append("\" data-visibility=\"").append(d.getVisibility().name())
                    .append("\">").append(esc(d.getContent())).append("</li>\n");
        }
        b.append("          </ul>\n");
        b.append("        </section>\n");

        // 카테고리 카운트
        b.append("        <ul class=\"cats\">\n");
        b.append("          <li>다이어리 <b>").append(diaries.size()).append("</b></li>\n");
        b.append("          <li>사진첩 <b>0</b></li>\n");
        b.append("          <li>방명록 <b>").append(guestbookViews.size()).append("</b></li>\n");
        b.append("        </ul>\n");

        // Mini Room
        b.append("        <section class=\"miniroom\">\n");
        b.append("          <h2>Mini Room <span>express yourself</span></h2>\n");
        b.append("          <div class=\"room\">\n");
        b.append("            <div class=\"mini-char\">🧑</div>\n");
        b.append("            <ul class=\"items\">\n");
        for (String itemId : layout.getPlacedItemIds()) {
            String name = miniroom.item(itemId).map(i -> i.name()).orElse(itemId);
            b.append("              <li data-item=\"").append(esc(itemId)).append("\">")
                    .append(esc(name)).append("</li>\n");
        }
        b.append("            </ul>\n");
        String bgmId = layout.getRepresentativeBgmId();
        if (bgmId != null) {
            String bgmName = miniroom.item(bgmId).map(i -> i.name()).orElse(bgmId);
            b.append("            <p class=\"bgm\" data-bgm=\"").append(esc(bgmId)).append("\">♪ ")
                    .append(esc(bgmName)).append("</p>\n");
        }
        b.append("          </div>\n");
        b.append("        </section>\n");

        // What friends say (방명록)
        b.append("        <section class=\"guestbook\">\n");
        b.append("          <h2>What friends say <span>방명록</span></h2>\n");
        b.append("          <ul class=\"gb-list\">\n");
        for (GuestbookView g : guestbookViews) {
            String content = g.isHidden() ? "비밀글입니다 🔒" : g.content();
            b.append("            <li").append(g.secret() ? " class=\"secret\"" : "")
                    .append("><b>").append(esc(g.authorId())).append("</b> ")
                    .append(esc(content)).append("</li>\n");
        }
        b.append("          </ul>\n");
        b.append("        </section>\n");

        b.append("      </div>\n");
        b.append("    </main>\n");

        // ── 우측: CYWORLD HOME 메뉴 ──
        b.append("    <nav class=\"right\">\n");
        b.append("      <div class=\"home-head\">CYWORLD HOME ▶▶</div>\n");
        b.append("      <ul class=\"gauges\">\n");
        b.append(gauge("도토리", balance, pct(balance, 2000)));
        b.append(gauge("일촌", ilchons.size(), pct(ilchons.size(), 10)));
        b.append(gauge("다이어리", diaries.size(), pct(diaries.size(), 10)));
        b.append(gauge("방명록", guestbookViews.size(), pct(guestbookViews.size(), 10)));
        b.append("      </ul>\n");
        b.append("      <div class=\"scrap\">스크랩 510 · 즐겨찾기 28</div>\n");
        b.append("      <ul class=\"tabs\">\n");
        for (String t : new String[]{"홈", "프로필", "다이어리", "사진첩", "방명록", "일촌평", "관리"}) {
            String cls = "홈".equals(t) ? " class=\"active\"" : "";
            b.append("        <li").append(cls).append(">").append(t).append("</li>\n");
        }
        b.append("      </ul>\n");
        b.append("      <div class=\"actions\"><a href=\"/minihompy/").append(esc(owner))
                .append("\">미니홈피 가기</a><a href=\"/logout\">로그아웃</a></div>\n");
        b.append("    </nav>\n");

        b.append("  </div>\n");
        b.append("</div>\n");
        b.append("</body>\n");
        b.append("</html>\n");
        return b.toString();
    }

    private static String gauge(String label, long value, int percent) {
        return "        <li><span class=\"lbl\">" + esc(label) + "</span>"
                + "<span class=\"bar\"><i style=\"width:" + percent + "%\"></i></span>"
                + "<span class=\"val\">" + value + "</span></li>\n";
    }

    private static int pct(long value, long max) {
        if (max <= 0) {
            return 0;
        }
        long p = value * 100 / max;
        return (int) Math.max(0, Math.min(100, p));
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    /** 클래식 싸이월드 감성의 고정 스타일(결정적). 핑크 하트 배경 + 3단 레이아웃. */
    private static final String STYLE =
            "*{box-sizing:border-box}"
            + "body{margin:0;font-family:'Malgun Gothic',Dotum,sans-serif;color:#444;"
            + "background:#ffd9e6 url(\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='44' height='44' viewBox='0 0 24 24'%3E%3Cpath d='M12 21s-7-4.7-9.3-9.2C1.3 8.6 2.7 5 6 5c2 0 3.4 1.3 4.2 2.6C11 6.3 12.4 5 14.4 5c3.3 0 4.7 3.6 3.3 6.8C19 16.3 12 21 12 21z' fill='%23f4a9c4'/%3E%3C/svg%3E\")}"
            + ".cyworld{max-width:880px;margin:20px auto;background:#fff;border:1px solid #e58bb0;"
            + "border-radius:6px;box-shadow:0 4px 16px rgba(180,80,120,.35);overflow:hidden}"
            + ".topbar{display:flex;align-items:center;justify-content:space-between;"
            + "padding:8px 14px;background:linear-gradient(#fff,#ffeef4);border-bottom:1px solid #f3bcd2}"
            + ".topbar .counts{font-size:11px;color:#c25b8a}.topbar .counts b{color:#e23b78}"
            + ".topbar .title{font-size:17px;color:#a23a66;font-weight:bold}"
            + ".topbar .url{font-size:10px;color:#aaa}"
            + ".columns{display:flex;gap:0;align-items:stretch}"
            + ".left{width:190px;flex:none;background:#fff;border-right:1px solid #f3d3e0;padding:12px}"
            + ".profile-card{text-align:center}"
            + ".avatar{width:120px;height:120px;margin:0 auto;border:1px solid #e58bb0;border-radius:6px;"
            + "background:linear-gradient(#fde6ef,#fff);font-size:64px;line-height:120px}"
            + ".today-is{margin-top:8px;font-size:10px;color:#fff;background:#e58bb0;border-radius:10px;"
            + "display:inline-block;padding:2px 10px}"
            + ".dname{font-size:14px;color:#a23a66;margin:10px 0 4px}"
            + ".status{font-size:11px;color:#888;margin:0 0 12px;min-height:14px}"
            + ".history{text-align:left;border-top:1px dashed #e7b6cc;padding-top:8px;margin-top:8px}"
            + ".history h2{font-size:11px;color:#c25b8a;margin:0 0 4px}"
            + ".history p{font-size:10px;color:#999;margin:0}"
            + ".ilchon-pick{text-align:left;margin-top:12px}"
            + ".ilchon-pick label{display:block;font-size:11px;color:#e23b78;margin-bottom:4px}"
            + ".ilchon-pick select{width:100%;font-size:11px;padding:2px;border:1px solid #e7b6cc;border-radius:3px}"
            + ".center{flex:1;display:flex;background:#fff}"
            + ".rings{width:20px;flex:none;background:#fff5f9;border-right:1px solid #f3d3e0;padding-top:12px}"
            + ".rings i{display:block;width:9px;height:9px;margin:13px auto;border-radius:50%;"
            + "background:#fff;border:1px solid #d99ab8}"
            + ".pages{flex:1;padding:14px 16px}"
            + ".pages section{margin-bottom:16px}"
            + ".pages h2{font-size:13px;color:#3a6ea5;border-bottom:2px solid #cfe0f2;"
            + "padding-bottom:4px;margin:0 0 8px}"
            + ".pages h2 span{font-size:10px;color:#aaa;font-weight:normal;margin-left:6px}"
            + ".diary-list,.gb-list{list-style:none;margin:0;padding:0}"
            + ".diary-list li{font-size:12px;padding:4px 6px 4px 14px;position:relative;color:#555}"
            + ".diary-list li:before{content:'▪';position:absolute;left:2px;color:#e58bb0}"
            + ".cats{list-style:none;display:flex;gap:8px;margin:0 0 16px;padding:0}"
            + ".cats li{flex:1;text-align:center;font-size:11px;color:#3a6ea5;background:#eef5fc;"
            + "border:1px solid #cfe0f2;border-radius:4px;padding:6px 0}"
            + ".cats li b{display:block;font-size:14px;color:#e23b78}"
            + ".room{position:relative;height:180px;border:1px solid #cfe0f2;border-radius:6px;overflow:hidden;"
            + "background:linear-gradient(#dfeefc 55%,#f5e6cf 55%)}"
            + ".mini-char{position:absolute;left:50%;top:96px;transform:translateX(-50%);font-size:42px}"
            + ".room .items{list-style:none;margin:0;padding:8px;display:flex;flex-wrap:wrap;gap:6px;"
            + "align-content:flex-start;position:relative;z-index:1}"
            + ".room .items li{font-size:11px;background:#fff;border:1px solid #cdb38c;border-radius:4px;"
            + "padding:3px 8px;color:#8a6d3b}"
            + ".room .bgm{position:absolute;right:8px;bottom:6px;font-size:11px;color:#c25b8a;margin:0}"
            + ".gb-list li{font-size:12px;padding:5px 6px;border-bottom:1px dotted #e7b6cc;color:#555}"
            + ".gb-list li b{color:#3a6ea5;margin-right:4px}"
            + ".gb-list li.secret{color:#aa8}"
            + ".right{width:170px;flex:none;background:#fbf4f8;border-left:1px solid #f3d3e0;padding:10px}"
            + ".home-head{font-size:11px;color:#fff;background:#a23a66;text-align:center;"
            + "border-radius:3px;padding:4px;margin-bottom:10px}"
            + ".gauges{list-style:none;margin:0 0 10px;padding:0}"
            + ".gauges li{display:flex;align-items:center;gap:4px;font-size:10px;margin-bottom:5px}"
            + ".gauges .lbl{width:42px;color:#a23a66}"
            + ".gauges .bar{flex:1;height:8px;background:#f0dbe6;border-radius:4px;overflow:hidden}"
            + ".gauges .bar i{display:block;height:100%;background:linear-gradient(#f49ac0,#e23b78)}"
            + ".gauges .val{width:34px;text-align:right;color:#777}"
            + ".scrap{font-size:10px;color:#999;text-align:center;margin-bottom:10px}"
            + ".tabs{list-style:none;margin:0 0 10px;padding:0;border-top:1px solid #f3d3e0}"
            + ".tabs li{font-size:11px;color:#a23a66;padding:5px 8px;border-bottom:1px solid #f3d3e0}"
            + ".tabs li.active{background:#a23a66;color:#fff;font-weight:bold}"
            + ".actions{display:flex;flex-direction:column;gap:5px}"
            + ".actions a{font-size:10px;text-align:center;text-decoration:none;color:#fff;"
            + "background:#e58bb0;border-radius:3px;padding:5px}";
}
