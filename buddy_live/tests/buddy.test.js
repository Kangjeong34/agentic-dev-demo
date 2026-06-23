// 버디 도메인 AC 검증 (buddy_feature_spec.md AC-1~6).
// 패턴: 커맨드로 이벤트를 만들고 reduce 로 접은 뒤 셀렉터로 단언 → AC 를 결정적으로 증명.

import { test } from "node:test";
import assert from "node:assert/strict";
import {
  initialState,
  reduce,
  isBuddy,
  buddyList,
  incomingRequests,
  join,
  setPresence,
  requestBuddy,
  acceptBuddy,
  rejectBuddy,
  removeBuddy,
} from "../src/domain/index.js";

// 작은 헬퍼: 상태에 커맨드 이벤트를 적용(이벤트가 null 이면 무시).
function apply(state, ev) {
  return ev ? reduce(state, ev) : state;
}

function seedUsers(s) {
  s = reduce(s, join(s, "alice", "앨리스"));
  s = reduce(s, join(s, "bob", "밥"));
  return s;
}

test("request_creates_pending", () => {
  // AC-1: A→B 신청 시 pending 요청 1건 생성, B에게 노출.
  let s = seedUsers(initialState());
  s = apply(s, requestBuddy(s, "alice", "bob"));
  assert.equal(isBuddy(s, "alice", "bob"), false);
  assert.deepEqual(
    incomingRequests(s, "bob").map((r) => r.from),
    ["alice"]
  );
});

test("accept_makes_bidirectional", () => {
  // AC-2: pending 을 B가 수락하면 양방향 accepted 성립.
  let s = seedUsers(initialState());
  s = apply(s, requestBuddy(s, "alice", "bob"));
  s = apply(s, acceptBuddy(s, "alice", "bob"));
  assert.equal(isBuddy(s, "alice", "bob"), true);
  assert.equal(isBuddy(s, "bob", "alice"), true); // 양방향
  assert.equal(incomingRequests(s, "bob").length, 0); // 요청 소거
});

test("reject_removes_request", () => {
  // AC-3: 거절 시 요청 제거, 관계 미성립.
  let s = seedUsers(initialState());
  s = apply(s, requestBuddy(s, "alice", "bob"));
  s = apply(s, rejectBuddy(s, "alice", "bob"));
  assert.equal(isBuddy(s, "alice", "bob"), false);
  assert.equal(incomingRequests(s, "bob").length, 0);
});

test("duplicate_request_idempotent", () => {
  // AC-4: 이미 버디거나 동일 방향 요청 진행 중이면 중복 생성 안 함.
  let s = seedUsers(initialState());
  s = apply(s, requestBuddy(s, "alice", "bob"));
  // 같은 신청 재시도 → 커맨드가 null, 이벤트를 강제 reduce 해도 무변화.
  assert.equal(requestBuddy(s, "alice", "bob"), null);
  s = reduce(s, { type: "buddy_requested", from: "alice", to: "bob" });
  assert.equal(incomingRequests(s, "bob").length, 1);
  // 수락해 버디가 된 뒤 다시 신청해도 중복 관계 없음.
  s = apply(s, acceptBuddy(s, "alice", "bob"));
  assert.equal(requestBuddy(s, "alice", "bob"), null);
  assert.equal(s.buddies.length, 1);
});

test("remove_breaks_both_sides", () => {
  // AC-5: 한쪽이 삭제하면 양방향 동시 해제.
  let s = seedUsers(initialState());
  s = apply(s, requestBuddy(s, "alice", "bob"));
  s = apply(s, acceptBuddy(s, "alice", "bob"));
  s = apply(s, removeBuddy(s, "bob", "alice")); // 반대쪽이 삭제해도
  assert.equal(isBuddy(s, "alice", "bob"), false);
  assert.equal(isBuddy(s, "bob", "alice"), false);
});

test("presence_reflected_in_buddy_list", () => {
  // AC-6: 접속/종료가 버디 목록 조회에 상태로 노출.
  let s = seedUsers(initialState());
  s = apply(s, requestBuddy(s, "alice", "bob"));
  s = apply(s, acceptBuddy(s, "alice", "bob"));
  s = apply(s, setPresence(s, "bob", true));
  let list = buddyList(s, "alice");
  assert.equal(list.length, 1);
  assert.equal(list[0].id, "bob");
  assert.equal(list[0].online, true);
  s = apply(s, setPresence(s, "bob", false));
  assert.equal(buddyList(s, "alice")[0].online, false);
});
