// 결정적 화면 fixture — render parity 스냅샷과 테스트가 같은 상태를 공유한다.
// 이 상태로 renderMessenger 를 호출한 결과가 ui_parity/messenger.html 과 정확히 일치해야 한다.

import {
  initialState,
  reduce,
  join,
  setPresence,
  requestBuddy,
  acceptBuddy,
  sendMessage,
} from "../src/domain/index.js";

export const VIEWER = "alice";
export const SELECTED = "bob";

export function canonicalState() {
  let s = initialState();
  s = reduce(s, join(s, "alice", "앨리스"));
  s = reduce(s, join(s, "bob", "밥"));
  s = reduce(s, join(s, "minsu", "민수"));
  s = reduce(s, join(s, "chulsoo", "철수"));
  s = reduce(s, setPresence(s, "alice", true));
  // alice ↔ bob 버디(접속중), alice ↔ minsu 버디(오프라인)
  s = reduce(s, requestBuddy(s, "alice", "bob"));
  s = reduce(s, acceptBuddy(s, "alice", "bob"));
  s = reduce(s, setPresence(s, "bob", true));
  s = reduce(s, requestBuddy(s, "alice", "minsu"));
  s = reduce(s, acceptBuddy(s, "alice", "minsu"));
  // chulsoo → alice 로 받은 버디 요청(pending)
  s = reduce(s, requestBuddy(s, "chulsoo", "alice"));
  // bob → alice 안읽음 2건 + alice → bob 1건 (대화창 정렬/정렬 확인용)
  s = reduce(s, sendMessage(s, "bob", "alice", "안녕!", "f1", 10));
  s = reduce(s, sendMessage(s, "alice", "bob", "ㅎㅇ", "f2", 20));
  s = reduce(s, sendMessage(s, "bob", "alice", "잘 지내?", "f3", 30));
  return s;
}
