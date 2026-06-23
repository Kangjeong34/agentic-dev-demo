// 엔드투엔드 — 두 유저가 로컬에서 "친구추가 → 수락 → 양방향 대화 → 읽음"까지 전 흐름을 한 시나리오로.
// 화면 셀렉터(buddyList/unread)와 대화 셀렉터로 사용자 관점 결과를 단언한다.

import { test } from "node:test";
import assert from "node:assert/strict";
import { LocalBus, Client } from "../src/web/transport.js";
import { buddyList, incomingRequests, roomMessages, unreadOf } from "../src/domain/index.js";

test("end_to_end_add_and_chat", () => {
  const bus = new LocalBus();
  const alice = new Client("alice", bus);
  const bob = new Client("bob", bus);
  alice.join("앨리스");
  bob.join("밥");
  alice.setPresence(true);
  bob.setPresence(true);

  // 1) alice 가 bob 에게 친구추가 → bob 화면에 받은 요청으로 노출
  alice.requestBuddy("bob");
  assert.deepEqual(
    incomingRequests(bob.state, "bob").map((r) => r.from),
    ["alice"]
  );

  // 2) bob 이 수락 → 양쪽 버디 목록에 서로 등장
  bob.acceptBuddy("alice");
  assert.equal(buddyList(alice.state, "alice")[0].id, "bob");
  assert.equal(buddyList(bob.state, "bob")[0].id, "alice");
  assert.equal(incomingRequests(bob.state, "bob").length, 0);

  // 3) 양방향 대화
  alice.sendMessage("bob", "밥아 안녕!", "a1", 1);
  bob.sendMessage("alice", "오 앨리스 ㅎㅇ", "b1", 2);
  alice.sendMessage("bob", "오늘 시간 돼?", "a2", 3);

  // 두 유저가 같은 대화를 같은 순서로 본다
  const expected = ["밥아 안녕!", "오 앨리스 ㅎㅇ", "오늘 시간 돼?"];
  assert.deepEqual(roomMessages(alice.state, "alice", "bob").map((m) => m.text), expected);
  assert.deepEqual(roomMessages(bob.state, "bob", "alice").map((m) => m.text), expected);

  // 4) 읽지 않은 동안 bob 의 안읽음은 2(alice 가 보낸 a1,a2)
  assert.equal(unreadOf(bob.state, "bob", "alice"), 2);
  // bob 이 대화방을 열어 읽음 → 0
  bob.readRoom("alice");
  assert.equal(unreadOf(bob.state, "bob", "alice"), 0);

  // 최종 상태 수렴
  assert.deepEqual(alice.state, bob.state);
});
