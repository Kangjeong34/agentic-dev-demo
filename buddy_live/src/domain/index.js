// 버디버디 도메인 진입점. 순수 이벤트 리듀서 + 셀렉터 + 커맨드 빌더를 한곳에서 재노출한다.
// 화면(src/web)과 테스트(tests)는 오직 이 모듈을 통해 도메인을 사용한다.
//
// 핵심 계약:
//   initialState() -> state
//   reduce(state, event) -> state           (순수·결정적: 같은 로그 → 같은 상태 = 두 탭 수렴)
//   <command>(state, ...) -> event | null   (현재 상태 기준 유효성 판정)

import { buddyReduce, isBuddy } from "./buddy.js";
import { chatReduce, roomMessages, unreadOf } from "./chat.js";
import { pairMembers } from "./keys.js";

export function initialState() {
  return {
    users: {}, // id -> { id, name, online }
    requests: [], // { from, to }  (pending 단방향 요청)
    buddies: [], // 정규화한 쌍 키 목록 (accepted 양방향 관계)
    rooms: {}, // roomKey -> [ { id, from, to, text, ts } ]
    unread: {}, // owner -> { other -> count }
  };
}

// 도메인 전체 리듀서. 각 도메인 핸들러를 순서대로 시도하고, 아무도 처리 안 하면 상태 불변.
export function reduce(state, ev) {
  const next = buddyReduce(state, ev);
  if (next !== null) return next;
  const next2 = chatReduce(state, ev);
  if (next2 !== null) return next2;
  return state; // 알 수 없는 이벤트는 무시(전방 호환)
}

// 이벤트 로그 전체를 접어 상태를 만든다(replay). 탭 수렴/영속 복원의 기반.
export function replay(events) {
  return events.reduce((s, ev) => reduce(s, ev), initialState());
}

// --- 화면용 합성 셀렉터 -------------------------------------------------------

// viewer 의 버디 목록: 접속상태 + 안읽음 수 포함, (이름, id) 순 정렬로 결정적.
export function buddyList(state, viewer) {
  const out = [];
  for (const key of state.buddies) {
    const members = pairMembers(key);
    if (!members.includes(viewer)) continue;
    const other = members[0] === viewer ? members[1] : members[0];
    const u = state.users[other] ?? { id: other, name: other, online: false };
    out.push({ id: other, name: u.name, online: !!u.online, unread: unreadOf(state, viewer, other) });
  }
  out.sort((a, b) => (a.name < b.name ? -1 : a.name > b.name ? 1 : a.id < b.id ? -1 : a.id > b.id ? 1 : 0));
  return out;
}

// viewer 가 받은(pending) 버디 요청 목록.
export function incomingRequests(state, viewer) {
  return state.requests
    .filter((r) => r.to === viewer)
    .map((r) => ({ from: r.from, name: state.users[r.from]?.name ?? r.from }))
    .sort((a, b) => (a.name < b.name ? -1 : a.name > b.name ? 1 : 0));
}

export function viewerName(state, viewer) {
  return state.users[viewer]?.name ?? viewer;
}

export function viewerOnline(state, viewer) {
  return !!state.users[viewer]?.online;
}

// 셀렉터/커맨드 재노출
export { isBuddy } from "./buddy.js";
export { roomMessages, unreadOf, isBlank } from "./chat.js";
export { join, setPresence, requestBuddy, acceptBuddy, rejectBuddy, removeBuddy } from "./buddy.js";
export { sendMessage, readRoom } from "./chat.js";
export { pairKey, roomKey, isValidUserId } from "./keys.js";
