// 대화 도메인 — 버디 사이 1:1 메시지/읽음/멱등.
// 대화는 버디(accepted) 관계에 의존한다(친구만 대화). 버디 상태 변화가 전송 가능 여부에 즉시 반영된다.

import { roomKey } from "./keys.js";
import { isBuddy } from "./buddy.js";

// --- selectors ---------------------------------------------------------------

// 두 사람의 단일 대화방 메시지를 시간순(ts→id)으로. 양쪽에서 동일 결과(AC-4).
export function roomMessages(state, a, b) {
  const msgs = state.rooms[roomKey(a, b)] ?? [];
  return [...msgs].sort((x, y) => x.ts - y.ts || (x.id < y.id ? -1 : x.id > y.id ? 1 : 0));
}

// viewer 가 other 로부터 받은 안읽음 수.
export function unreadOf(state, viewer, other) {
  return state.unread[viewer]?.[other] ?? 0;
}

function hasMessageId(state, a, b, id) {
  return (state.rooms[roomKey(a, b)] ?? []).some((m) => m.id === id);
}

export function isBlank(text) {
  return typeof text !== "string" || text.trim() === "";
}

// --- command builders (state -> event | null) --------------------------------

// id = clientMessageId(재시도 가드), ts = 송신측 타임스탬프.
export function sendMessage(state, from, to, text, id, ts) {
  if (!isBuddy(state, from, to)) return null; // 친구만 대화 (AC-2)
  if (isBlank(text)) return null; // 빈/공백 거부 (AC-6)
  if (hasMessageId(state, from, to, id)) return null; // 중복 id 멱등 (AC-5)
  return { type: "message_sent", id, from, to, text, ts };
}

export function readRoom(state, reader, other) {
  return { type: "room_read", reader, other };
}

// --- reducer (returns next state, or null if event not handled here) ---------

export function chatReduce(state, ev) {
  switch (ev.type) {
    case "message_sent": {
      // 방어적 재가드: 복제/재전송 이벤트에도 멱등·불변식 유지(verify 결정성).
      if (!isBuddy(state, ev.from, ev.to)) return state;
      if (isBlank(ev.text)) return state;
      if (hasMessageId(state, ev.from, ev.to, ev.id)) return state;
      const key = roomKey(ev.from, ev.to);
      const room = state.rooms[key] ?? [];
      const rooms = {
        ...state.rooms,
        [key]: [...room, { id: ev.id, from: ev.from, to: ev.to, text: ev.text, ts: ev.ts }],
      };
      // 수신자(to)의 발신자(from)에 대한 안읽음 +1.
      const forTo = state.unread[ev.to] ?? {};
      const unread = { ...state.unread, [ev.to]: { ...forTo, [ev.from]: (forTo[ev.from] ?? 0) + 1 } };
      return { ...state, rooms, unread };
    }
    case "room_read": {
      const forReader = state.unread[ev.reader] ?? {};
      const unread = { ...state.unread, [ev.reader]: { ...forReader, [ev.other]: 0 } };
      return { ...state, unread };
    }
    default:
      return null; // 대화 이벤트가 아님
  }
}
