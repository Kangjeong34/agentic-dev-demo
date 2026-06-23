// 도메인 키 헬퍼. 두 사용자 사이의 관계/대화방은 "순서 무관" 단일 키로 정규화한다.
// → 양쪽(A→B, B→A)에서 항상 같은 키가 나와 단일 관계/단일 대화방을 보장한다(chat AC-4).

const SEP = "~"; // 사용자 id 는 영숫자만 허용(아래 isValidUserId) → 구분자 충돌 없음

export function pairKey(a, b) {
  return [a, b].sort().join(SEP);
}

// 대화방 키 = 버디 쌍 키. 두 사람당 정확히 하나의 방.
export const roomKey = pairKey;

export function pairMembers(key) {
  return key.split(SEP);
}

// id 는 영숫자/밑줄/하이픈만 — 구분자(~)와 충돌하지 않게 강제.
export function isValidUserId(id) {
  return typeof id === "string" && /^[A-Za-z0-9_-]+$/.test(id);
}
