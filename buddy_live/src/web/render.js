// 순수 렌더러 — renderMessenger(state, viewerId, selectedId) -> HTML 문자열.
// 도메인 상태만 입력으로 받아 결정적 HTML 을 만든다(부수효과·DOM 접근 없음).
// 실제 브라우저(app.js)와 테스트(render.test.js)가 같은 함수를 써서 화면-도메인 일치를 parity 로 검증한다.

import {
  buddyList,
  incomingRequests,
  roomMessages,
  viewerName,
  viewerOnline,
} from "../domain/index.js";

export function escapeHtml(s) {
  return String(s)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function renderBuddyItem(b, selectedId) {
  const dot = b.online ? '<span class="dot on">●</span>' : '<span class="dot off">○</span>';
  const badge = b.unread > 0 ? `<span class="badge">${b.unread}</span>` : "";
  const sel = b.id === selectedId ? " selected" : "";
  return (
    `<li class="buddy${sel}" data-action="open" data-id="${escapeHtml(b.id)}">` +
    `${dot}<span class="name">${escapeHtml(b.name)}</span>${badge}</li>`
  );
}

function renderRequestItem(r) {
  return (
    `<li class="request" data-id="${escapeHtml(r.from)}">` +
    `<span class="name">${escapeHtml(r.name)}</span>` +
    `<button data-action="accept" data-id="${escapeHtml(r.from)}">수락</button>` +
    `<button data-action="reject" data-id="${escapeHtml(r.from)}">거절</button>` +
    `</li>`
  );
}

function renderMessages(state, viewerId, selectedId) {
  const msgs = roomMessages(state, viewerId, selectedId);
  return msgs
    .map((m) => {
      const side = m.from === viewerId ? "me" : "them";
      return `<div class="msg ${side}"><span class="bubble">${escapeHtml(m.text)}</span></div>`;
    })
    .join("");
}

function renderConversation(state, viewerId, selectedId) {
  if (!selectedId) {
    return '<div class="conversation empty"><p class="hint">버디를 선택해 대화를 시작하세요.</p></div>';
  }
  const peerName = state.users[selectedId]?.name ?? selectedId;
  return (
    '<div class="conversation">' +
    `<header class="conv-head">${escapeHtml(peerName)} 와의 대화</header>` +
    `<div class="messages" data-role="messages">${renderMessages(state, viewerId, selectedId)}</div>` +
    '<form class="composer" data-action="send">' +
    `<input type="text" name="text" data-role="input" placeholder="메시지 입력" autocomplete="off" />` +
    '<button type="submit">전송</button>' +
    "</form>" +
    "</div>"
  );
}

export function renderMessenger(state, viewerId, selectedId = null) {
  const buddies = buddyList(state, viewerId);
  const requests = incomingRequests(state, viewerId);
  const meName = viewerName(state, viewerId);
  const meDot = viewerOnline(state, viewerId)
    ? '<span class="dot on">● 접속중</span>'
    : '<span class="dot off">○ 오프라인</span>';

  const buddiesHtml = buddies.length
    ? buddies.map((b) => renderBuddyItem(b, selectedId)).join("")
    : '<li class="empty">아직 버디가 없습니다</li>';

  const requestsHtml = requests.length
    ? '<div class="section-title">받은 요청</div><ul class="requests">' +
      requests.map(renderRequestItem).join("") +
      "</ul>"
    : "";

  return (
    '<div class="messenger">' +
    `<header class="app-head"><span class="title">buddy live · ${escapeHtml(meName)}</span>${meDot}</header>` +
    '<div class="body">' +
    '<aside class="sidebar">' +
    '<form class="add" data-action="add">' +
    '<input type="text" name="buddyId" data-role="add-input" placeholder="버디 추가(id)" autocomplete="off" />' +
    '<button type="submit">+</button>' +
    "</form>" +
    '<div class="section-title">버디</div>' +
    `<ul class="buddies">${buddiesHtml}</ul>` +
    requestsHtml +
    "</aside>" +
    renderConversation(state, viewerId, selectedId) +
    "</div>" +
    "</div>"
  );
}
