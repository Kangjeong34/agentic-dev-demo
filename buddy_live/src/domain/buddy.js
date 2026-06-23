// 버디 도메인 — 친구추가/관계/접속상태.
// 설계: 순수 이벤트 리듀서. 커맨드 빌더가 "현재 상태에 비춰 유효한가"를 판정해 이벤트(또는 null)를
// 만들고, reduce 는 이벤트를 결정적으로 상태에 적용한다. reduce 는 방어적으로 동일 가드를 다시 적용해
// (재전송/복제 이벤트가 들어와도) 멱등·안전하게 수렴한다. → 검증(verify)을 결정적으로 만드는 핵심.

import { pairKey } from "./keys.js";

// --- selectors ---------------------------------------------------------------

export function isBuddy(state, a, b) {
  return state.buddies.includes(pairKey(a, b));
}

function hasRequest(state, from, to) {
  return state.requests.some((r) => r.from === from && r.to === to);
}

function ensureUser(users, id) {
  return users[id] ? users : { ...users, [id]: { id, name: id, online: false } };
}

// --- command builders (state -> event | null) --------------------------------

export function join(state, id, name) {
  return { type: "user_joined", id, name: name ?? id };
}

export function setPresence(state, id, online) {
  return { type: "presence", id, online: !!online };
}

export function requestBuddy(state, from, to) {
  if (from === to) return null; // 자기 자신에게 불가
  if (isBuddy(state, from, to)) return null; // 이미 버디 (AC-4 멱등)
  if (hasRequest(state, from, to)) return null; // 동일 방향 요청 진행 중 (AC-4 멱등)
  return { type: "buddy_requested", from, to };
}

export function acceptBuddy(state, from, to) {
  if (!hasRequest(state, from, to)) return null; // 수락할 요청이 없음
  return { type: "buddy_accepted", from, to };
}

export function rejectBuddy(state, from, to) {
  if (!hasRequest(state, from, to)) return null;
  return { type: "buddy_rejected", from, to };
}

export function removeBuddy(state, a, b) {
  if (!isBuddy(state, a, b)) return null;
  return { type: "buddy_removed", a, b };
}

// --- reducer (returns next state, or null if event not handled here) ---------

export function buddyReduce(state, ev) {
  switch (ev.type) {
    case "user_joined": {
      if (state.users[ev.id]) return state; // 이미 존재 → 접속상태 보존
      return { ...state, users: { ...state.users, [ev.id]: { id: ev.id, name: ev.name, online: false } } };
    }
    case "presence": {
      const users = ensureUser(state.users, ev.id);
      return { ...state, users: { ...users, [ev.id]: { ...users[ev.id], online: ev.online } } };
    }
    case "buddy_requested": {
      if (ev.from === ev.to) return state;
      if (isBuddy(state, ev.from, ev.to)) return state;
      if (hasRequest(state, ev.from, ev.to)) return state; // 멱등
      let users = ensureUser(state.users, ev.from);
      users = ensureUser(users, ev.to);
      return { ...state, users, requests: [...state.requests, { from: ev.from, to: ev.to }] };
    }
    case "buddy_accepted": {
      if (!hasRequest(state, ev.from, ev.to)) return state;
      const requests = state.requests.filter((r) => !(r.from === ev.from && r.to === ev.to));
      const key = pairKey(ev.from, ev.to);
      const buddies = state.buddies.includes(key) ? state.buddies : [...state.buddies, key];
      return { ...state, requests, buddies };
    }
    case "buddy_rejected": {
      const requests = state.requests.filter((r) => !(r.from === ev.from && r.to === ev.to));
      return { ...state, requests };
    }
    case "buddy_removed": {
      const key = pairKey(ev.a, ev.b);
      const buddies = state.buddies.filter((k) => k !== key);
      // 진행 중이던 양방향 요청도 함께 정리(관계 흔적 제거).
      const requests = state.requests.filter(
        (r) => !((r.from === ev.a && r.to === ev.b) || (r.from === ev.b && r.to === ev.a))
      );
      return { ...state, buddies, requests };
    }
    default:
      return null; // 버디 이벤트가 아님
  }
}
