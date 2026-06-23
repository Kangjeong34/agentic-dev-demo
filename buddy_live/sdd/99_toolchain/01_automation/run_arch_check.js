// 도메인 경계 게이트(구조) — 도메인은 화면을 알지 못한다.
// 규칙 1: src/domain/*.js 는 src/web 을 import 하지 않는다(도메인→화면 의존 금지).
//         (화면/transport 는 도메인을 import 해도 된다 — 단방향.)
// 규칙 2: 도메인은 무의존 — 외부 패키지(node:* 제외, 상대경로 제외)를 import 하지 않는다.
// 위반이 1건이라도 있으면 exit 1.

import { readdirSync, readFileSync } from "node:fs";
import { join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = resolve(fileURLToPath(new URL("../../../", import.meta.url)));
const DOMAIN = join(ROOT, "src", "domain");

const IMPORT_RE = /(?:import|export)\s[^"']*?from\s*["']([^"']+)["']/g;

const violations = [];

for (const file of readdirSync(DOMAIN).filter((f) => f.endsWith(".js"))) {
  const src = readFileSync(join(DOMAIN, file), "utf8");
  let m;
  while ((m = IMPORT_RE.exec(src)) !== null) {
    const spec = m[1];
    // 규칙 1: 화면 계층 참조 금지
    if (spec.includes("/web/") || spec.includes("web/") || spec.endsWith("/render.js")) {
      violations.push(`[규칙1] src/domain/${file} → 화면(web) import: "${spec}"`);
    }
    // 규칙 2: 무의존(상대경로 또는 node: 빌트인만 허용)
    const isRelative = spec.startsWith(".") || spec.startsWith("/");
    const isBuiltin = spec.startsWith("node:");
    if (!isRelative && !isBuiltin) {
      violations.push(`[규칙2] src/domain/${file} → 외부 의존 import: "${spec}"`);
    }
  }
}

if (violations.length > 0) {
  console.error("ARCH CHECK: FAIL");
  for (const v of violations) console.error("  - " + v);
  process.exit(1);
}

console.log("ARCH CHECK: PASS (도메인→화면 위반 0 · 외부 의존 0)");
