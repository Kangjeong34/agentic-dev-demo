// 화면 렌더 검증 (messenger_screen_spec.md SCR) + 화면-도메인 parity 스냅샷 대조.

import { test } from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { renderMessenger } from "../src/web/render.js";
import { canonicalState, VIEWER, SELECTED } from "./fixtures.js";

const SNAPSHOT = fileURLToPath(
  new URL("../sdd/04_verify/10_test/ui_parity/messenger.html", import.meta.url)
);

test("buddy_list_shows_presence_and_unread", () => {
  // SCR-1: 버디 목록이 접속상태(●/○)와 안읽음 배지(>0)를 함께 렌더.
  const html = renderMessenger(canonicalState(), VIEWER, SELECTED);
  // bob 접속중(●) + 안읽음 2(bob→alice 메시지 2건)
  assert.match(html, /class="dot on">●<\/span><span class="name">밥<\/span><span class="badge">2<\/span>/);
  // minsu 오프라인(○) + 배지 없음
  assert.match(html, /class="dot off">○<\/span><span class="name">민수<\/span><\/li>/);
  // SCR-2: 받은 요청(철수)에 수락/거절 액션 노출
  assert.match(html, /data-action="accept" data-id="chulsoo"/);
  assert.match(html, /data-action="reject" data-id="chulsoo"/);
});

test("chat_window_orders_and_aligns_messages", () => {
  // SCR-3: 대화창은 시간순 버블 + 내 메시지 우측(me)/상대 좌측(them) 정렬.
  const html = renderMessenger(canonicalState(), VIEWER, SELECTED);
  const them1 = html.indexOf('<div class="msg them"><span class="bubble">안녕!');
  const me1 = html.indexOf('<div class="msg me"><span class="bubble">ㅎㅇ');
  const them2 = html.indexOf('<div class="msg them"><span class="bubble">잘 지내?');
  assert.ok(them1 >= 0 && me1 >= 0 && them2 >= 0, "세 메시지가 모두 렌더되어야 한다");
  assert.ok(them1 < me1 && me1 < them2, "메시지는 ts 시간순(안녕!→ㅎㅇ→잘 지내?)으로 정렬");

  // parity: 실제 화면 함수 출력이 커밋된 스냅샷과 정확히 일치(화면-도메인 결정성).
  const snapshot = readFileSync(SNAPSHOT, "utf8");
  assert.equal(html, snapshot, "renderMessenger 출력이 ui_parity 스냅샷과 일치해야 한다");
});
