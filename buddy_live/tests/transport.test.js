// 라이브 복제 검증 — 두 클라이언트(=두 탭/두 유저)가 같은 이벤트 로그로 동일 상태에 수렴.
// "두 유저가 로컬에서 친구추가·대화"의 증명 핵심.

import { test } from "node:test";
import assert from "node:assert/strict";
import { LocalBus, Client } from "../src/web/transport.js";
import { buddyList, roomMessages, isBuddy } from "../src/domain/index.js";

test("two_tabs_converge", () => {
  // 두 클라이언트가 한 버스에 붙어 서로 다른 주체로 명령해도 최종 상태가 동일.
  const bus = new LocalBus();
  const a = new Client("alice", bus);
  const b = new Client("bob", bus);
  a.join("앨리스");
  b.join("밥");
  a.setPresence(true);
  b.setPresence(true);

  a.requestBuddy("bob"); // alice 가 신청
  b.acceptBuddy("alice"); // bob 이 수락
  a.sendMessage("bob", "안녕!", "m1", 1);
  b.sendMessage("alice", "ㅎㅇ", "m2", 2);

  // 두 클라이언트가 본 대화 내역이 동일(같은 방·같은 순서).
  const seenByA = roomMessages(a.state, "alice", "bob").map((m) => m.text);
  const seenByB = roomMessages(b.state, "alice", "bob").map((m) => m.text);
  assert.deepEqual(seenByA, ["안녕!", "ㅎㅇ"]);
  assert.deepEqual(seenByB, ["안녕!", "ㅎㅇ"]);
  assert.equal(isBuddy(a.state, "alice", "bob"), true);
  assert.equal(isBuddy(b.state, "alice", "bob"), true);
  // 전체 상태 수렴(직렬화 동일).
  assert.deepEqual(a.state, b.state);
});

test("replay_late_joiner_converges", () => {
  // 늦게 합류한 탭도 과거 로그 replay 로 같은 상태에 도달(localStorage 영속 복원 등가).
  const bus = new LocalBus();
  const a = new Client("alice", bus);
  a.join("앨리스");
  const b = new Client("bob", bus);
  b.join("밥");
  a.requestBuddy("bob");
  b.acceptBuddy("alice");
  a.sendMessage("bob", "먼저 와 있었음", "m1", 1);

  // 이제 세 번째 관찰자(다른 탭)가 합류 — connect 시 전체 로그를 받는다.
  const c = new Client("alice", bus);
  assert.deepEqual(
    roomMessages(c.state, "alice", "bob").map((m) => m.text),
    ["먼저 와 있었음"]
  );
  assert.deepEqual(c.state, a.state); // 합류 즉시 수렴
  assert.equal(buddyList(c.state, "alice")[0].id, "bob");
});
