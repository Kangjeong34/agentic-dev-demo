// 라이브 복제(transport) — "두 유저가 로컬에서 대화"의 핵심.
// 각 클라이언트는 자신의 상태를 들고, 커맨드가 만든 이벤트를 버스로 발행한다. 버스는 모든 클라이언트
// (자기 자신 포함)에 같은 순서로 이벤트를 전달하고, 각자 reduce 로 접는다.
// → 같은 이벤트 로그를 접으므로 모든 클라이언트가 동일 상태로 수렴한다(= 두 탭/두 유저가 같은 화면).
//
// 여기엔 BroadcastChannel 의존이 없다(Node 에서 그대로 테스트 가능). 브라우저용 버스 어댑터는 app.js.

import { initialState, reduce } from "../domain/index.js";
import * as domain from "../domain/index.js";

let _seq = 0;
function envelopeId() {
  return `e${++_seq}-${Math.random().toString(36).slice(2, 8)}`;
}

// 메모리 버스: 한 프로세스 안에서 여러 클라이언트를 잇는다(테스트/단일 페이지 다중 뷰).
export class LocalBus {
  constructor() {
    this.clients = [];
    this.log = [];
  }
  connect(client) {
    this.clients.push(client);
    for (const env of this.log) client.receive(env); // 늦게 합류한 탭에 과거 로그 replay
  }
  publish(env) {
    this.log.push(env);
    for (const c of this.clients) c.receive(env);
  }
}

export class Client {
  constructor(selfId, bus) {
    this.selfId = selfId;
    this.bus = bus;
    this.state = initialState();
    this.applied = new Set(); // 봉투 id 중복 적용 방지(복제/재전송 가드)
    this.listeners = new Set();
    bus.connect(this);
  }

  onChange(fn) {
    this.listeners.add(fn);
    return () => this.listeners.delete(fn);
  }

  receive(env) {
    if (this.applied.has(env.id)) return;
    this.applied.add(env.id);
    this.state = reduce(this.state, env.event);
    for (const fn of this.listeners) fn(this.state);
  }

  // 커맨드 빌더(state -> event|null)를 현재 상태에 적용해 이벤트를 발행. 무효면 발행 안 함.
  dispatch(build) {
    const event = build(this.state);
    if (!event) return false;
    this.bus.publish({ id: envelopeId(), event });
    return true;
  }

  // --- 편의 커맨드 (도메인 커맨드 래핑) ---
  join(name) {
    return this.dispatch((s) => domain.join(s, this.selfId, name));
  }
  setPresence(online) {
    return this.dispatch((s) => domain.setPresence(s, this.selfId, online));
  }
  requestBuddy(to) {
    return this.dispatch((s) => domain.requestBuddy(s, this.selfId, to));
  }
  acceptBuddy(from) {
    return this.dispatch((s) => domain.acceptBuddy(s, from, this.selfId));
  }
  rejectBuddy(from) {
    return this.dispatch((s) => domain.rejectBuddy(s, from, this.selfId));
  }
  removeBuddy(other) {
    return this.dispatch((s) => domain.removeBuddy(s, this.selfId, other));
  }
  sendMessage(to, text, clientMessageId, ts = Date.now()) {
    const id = clientMessageId ?? envelopeId();
    return this.dispatch((s) => domain.sendMessage(s, this.selfId, to, text, id, ts));
  }
  readRoom(other) {
    return this.dispatch((s) => domain.readRoom(s, this.selfId, other));
  }
}
