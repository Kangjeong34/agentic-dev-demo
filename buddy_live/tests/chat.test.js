// 대화 도메인 AC 검증 (chat_feature_spec.md AC-1~6).

import { test } from "node:test";
import assert from "node:assert/strict";
import {
  initialState,
  reduce,
  join,
  requestBuddy,
  acceptBuddy,
  removeBuddy,
  sendMessage,
  readRoom,
  roomMessages,
  unreadOf,
} from "../src/domain/index.js";

function apply(state, ev) {
  return ev ? reduce(state, ev) : state;
}

// alice 와 bob 이 버디인 상태를 만든다.
function buddies() {
  let s = initialState();
  s = reduce(s, join(s, "alice", "앨리스"));
  s = reduce(s, join(s, "bob", "밥"));
  s = apply(s, requestBuddy(s, "alice", "bob"));
  s = apply(s, acceptBuddy(s, "alice", "bob"));
  return s;
}

test("send_appends_and_increments_unread", () => {
  // AC-1: 버디에게 전송 → 대화방에 추가 + 수신자 안읽음 +1.
  let s = buddies();
  s = apply(s, sendMessage(s, "alice", "bob", "안녕!", "m1", 1));
  const msgs = roomMessages(s, "alice", "bob");
  assert.equal(msgs.length, 1);
  assert.equal(msgs[0].text, "안녕!");
  assert.equal(unreadOf(s, "bob", "alice"), 1); // 수신자만 증가
  assert.equal(unreadOf(s, "alice", "bob"), 0); // 발신자는 그대로
});

test("send_rejected_when_not_buddy", () => {
  // AC-2 (+회귀): 버디가 아니면 전송 거부, 대화방/카운트 무변화.
  let s = initialState();
  s = reduce(s, join(s, "alice", "앨리스"));
  s = reduce(s, join(s, "carol", "캐럴"));
  assert.equal(sendMessage(s, "alice", "carol", "hi", "x1", 1), null);
  // 강제 이벤트 주입도 reduce 가 방어적으로 무시.
  s = reduce(s, { type: "message_sent", id: "x1", from: "alice", to: "carol", text: "hi", ts: 1 });
  assert.equal(roomMessages(s, "alice", "carol").length, 0);
  assert.equal(unreadOf(s, "carol", "alice"), 0);

  // 회귀: 버디였다가 삭제되면 다시 전송 불가.
  let s2 = buddies();
  s2 = apply(s2, removeBuddy(s2, "alice", "bob"));
  assert.equal(sendMessage(s2, "alice", "bob", "still?", "y1", 2), null);
});

test("read_clears_unread", () => {
  // AC-3: 대화방을 열어 읽으면 안읽음 0.
  let s = buddies();
  s = apply(s, sendMessage(s, "alice", "bob", "1", "m1", 1));
  s = apply(s, sendMessage(s, "alice", "bob", "2", "m2", 2));
  assert.equal(unreadOf(s, "bob", "alice"), 2);
  s = apply(s, readRoom(s, "bob", "alice"));
  assert.equal(unreadOf(s, "bob", "alice"), 0);
});

test("history_same_order_both_sides", () => {
  // AC-4: 양쪽 조회가 동일 단일 방의 동일 시간순.
  let s = buddies();
  s = apply(s, sendMessage(s, "alice", "bob", "a", "m1", 10));
  s = apply(s, sendMessage(s, "bob", "alice", "b", "m2", 20));
  s = apply(s, sendMessage(s, "alice", "bob", "c", "m3", 30));
  const fromAlice = roomMessages(s, "alice", "bob").map((m) => m.text);
  const fromBob = roomMessages(s, "bob", "alice").map((m) => m.text);
  assert.deepEqual(fromAlice, ["a", "b", "c"]);
  assert.deepEqual(fromBob, ["a", "b", "c"]); // 동일 순서·동일 방
});

test("duplicate_message_id_idempotent", () => {
  // AC-5: 동일 clientMessageId 재전송은 중복 저장 안 함.
  let s = buddies();
  s = apply(s, sendMessage(s, "alice", "bob", "once", "dup", 1));
  assert.equal(sendMessage(s, "alice", "bob", "again", "dup", 2), null);
  s = reduce(s, { type: "message_sent", id: "dup", from: "alice", to: "bob", text: "again", ts: 2 });
  const msgs = roomMessages(s, "alice", "bob");
  assert.equal(msgs.length, 1);
  assert.equal(msgs[0].text, "once");
  assert.equal(unreadOf(s, "bob", "alice"), 1); // 재전송이 카운트도 안 올림
});

test("blank_message_rejected", () => {
  // AC-6: 빈 문자열/공백만 있는 메시지는 거부.
  let s = buddies();
  assert.equal(sendMessage(s, "alice", "bob", "", "b1", 1), null);
  assert.equal(sendMessage(s, "alice", "bob", "   ", "b2", 2), null);
  s = reduce(s, { type: "message_sent", id: "b1", from: "alice", to: "bob", text: "  ", ts: 1 });
  assert.equal(roomMessages(s, "alice", "bob").length, 0);
});
