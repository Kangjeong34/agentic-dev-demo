// 브라우저 글루 — 도메인/렌더/transport 를 실제 화면에 연결한다.
// 탭 간 라이브 동기화: BroadcastChannel 로 이벤트를 복제하고 localStorage 에 로그를 영속한다.
// 두 브라우저 탭을 ?me=alice / ?me=bob 로 열면 서로 친구추가·대화할 수 있다(같은 origin 필요 → serve.js).

import { Client } from "./transport.js";
import { renderMessenger } from "./render.js";

const LOG_KEY = "buddy_live_log_v1";
const CHANNEL = "buddy_live";

// 탭 간 이벤트 버스 + 영속. LocalBus 와 동일한 connect/publish 계약을 만족한다.
class BroadcastBus {
  constructor() {
    this.client = null;
    this.log = readLog();
    this.channel = new BroadcastChannel(CHANNEL);
    this.channel.onmessage = (e) => this.incoming(e.data);
  }
  connect(client) {
    this.client = client;
    for (const env of this.log) client.receive(env); // 영속 로그 replay → 새 탭도 수렴
  }
  publish(env) {
    this.persist(env);
    this.client.receive(env); // 로컬 즉시 반영
    this.channel.postMessage(env); // 다른 탭에 복제
  }
  incoming(env) {
    this.persist(env);
    if (this.client) this.client.receive(env); // Client.applied 가 중복 적용을 막음
  }
  persist(env) {
    if (this.log.some((e) => e.id === env.id)) return;
    this.log.push(env);
    localStorage.setItem(LOG_KEY, JSON.stringify(this.log));
  }
}

function readLog() {
  try {
    return JSON.parse(localStorage.getItem(LOG_KEY) || "[]");
  } catch {
    return [];
  }
}

function param(name) {
  return new URLSearchParams(location.search).get(name);
}

// --- 로그인 게이트(내가 누구인가) -------------------------------------------
const me = param("me");
const myName = param("name") || me;
const root = document.getElementById("app");

if (!me) {
  renderLogin();
} else {
  startMessenger(me, myName);
}

function renderLogin() {
  root.innerHTML =
    '<div class="login">' +
    "<h1>buddy live</h1>" +
    "<p>이 탭의 사용자를 정하세요. 다른 탭을 다른 id 로 열면 서로 대화할 수 있어요.</p>" +
    '<form id="login-form">' +
    '<input id="login-id" placeholder="내 id (예: alice)" autocomplete="off" required />' +
    '<input id="login-name" placeholder="표시 이름 (예: 앨리스)" autocomplete="off" />' +
    "<button type=\"submit\">입장</button>" +
    "</form>" +
    '<p class="tip">빠른 데모: 한 탭은 <code>?me=alice</code>, 다른 탭은 <code>?me=bob</code></p>' +
    "</div>";
  document.getElementById("login-form").addEventListener("submit", (e) => {
    e.preventDefault();
    const id = document.getElementById("login-id").value.trim();
    const nm = document.getElementById("login-name").value.trim();
    if (!/^[A-Za-z0-9_-]+$/.test(id)) {
      alert("id 는 영문/숫자/_/- 만 가능합니다.");
      return;
    }
    const url = new URL(location.href);
    url.searchParams.set("me", id);
    if (nm) url.searchParams.set("name", nm);
    location.href = url.toString();
  });
}

function startMessenger(meId, meName) {
  const bus = new BroadcastBus();
  const client = new Client(meId, bus);
  let selected = null;

  client.join(meName);
  client.setPresence(true);
  window.addEventListener("beforeunload", () => client.setPresence(false));

  const render = () => {
    // 입력값/포커스 보존(innerHTML 교체 시 손실 방지)
    const prevAdd = root.querySelector('[data-role="add-input"]');
    const prevText = root.querySelector('[data-role="input"]');
    const addVal = prevAdd ? prevAdd.value : "";
    const textVal = prevText ? prevText.value : "";
    const focusRole = document.activeElement?.dataset?.role;

    root.innerHTML = renderMessenger(client.state, meId, selected);

    const addInput = root.querySelector('[data-role="add-input"]');
    const textInput = root.querySelector('[data-role="input"]');
    if (addInput) addInput.value = addVal;
    if (textInput) textInput.value = textVal;
    if (focusRole === "add-input" && addInput) addInput.focus();
    if (focusRole === "input" && textInput) {
      textInput.focus();
    }
    // 대화창 스크롤 맨 아래로
    const messages = root.querySelector('[data-role="messages"]');
    if (messages) messages.scrollTop = messages.scrollHeight;
  };

  client.onChange(render);
  render();

  // 이벤트 위임 — 클릭
  root.addEventListener("click", (e) => {
    const el = e.target.closest("[data-action]");
    if (!el) return;
    const action = el.dataset.action;
    const id = el.dataset.id;
    if (action === "open") {
      selected = id;
      client.readRoom(id); // 읽음 처리(안읽음 0) → onChange 가 재렌더
      render();
    } else if (action === "accept") {
      client.acceptBuddy(id);
    } else if (action === "reject") {
      client.rejectBuddy(id);
    }
  });

  // 이벤트 위임 — 폼 제출(버디 추가 / 전송)
  root.addEventListener("submit", (e) => {
    const form = e.target.closest("[data-action]");
    if (!form) return;
    e.preventDefault();
    if (form.dataset.action === "add") {
      const input = form.querySelector('[data-role="add-input"]');
      const to = input.value.trim();
      if (to && to !== meId) {
        client.join(to); // 상대가 아직 없으면 최소 노드 생성(데모 편의)
        client.requestBuddy(to);
      }
      input.value = "";
    } else if (form.dataset.action === "send") {
      const input = form.querySelector('[data-role="input"]');
      const text = input.value;
      if (selected && text.trim()) {
        const cid = `c-${meId}-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`;
        client.sendMessage(selected, text, cid, Date.now());
        input.value = "";
      }
    }
  });
}
