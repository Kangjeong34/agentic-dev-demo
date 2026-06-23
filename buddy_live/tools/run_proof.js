// proof 게이트 — 완료 기준은 "사람이 봤다"가 아니라 "게이트가 통과시켰다".
// tests/*.test.js 를 node:test 로 실행하고 파일별 집계를 tmp/proof-results.json 에 기록한다.
// exit 0 = PASS. 이 산출물이 04_verify 증거의 1차 소스다.

import { spawnSync } from "node:child_process";
import { readdirSync, mkdirSync, writeFileSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const HERE = dirname(fileURLToPath(import.meta.url));
const ROOT = resolve(HERE, "..");
const TESTS_DIR = join(ROOT, "tests");
const OUT = join(ROOT, "tmp", "proof-results.json");

function parseCount(text, label) {
  const m = text.match(new RegExp(`^# ${label} (\\d+)`, "m"));
  return m ? Number(m[1]) : 0;
}

const files = readdirSync(TESTS_DIR)
  .filter((f) => f.endsWith(".test.js"))
  .sort();

const perFile = [];
let total = 0;
let passed = 0;
let failed = 0;

for (const file of files) {
  const res = spawnSync(process.execPath, ["--test", "--test-reporter=tap", join(TESTS_DIR, file)], {
    encoding: "utf8",
  });
  const out = (res.stdout || "") + (res.stderr || "");
  const t = parseCount(out, "tests");
  const p = parseCount(out, "pass");
  const f = parseCount(out, "fail");
  perFile.push({ file: `tests/${file}`, total: t, pass: p, fail: f });
  total += t;
  passed += p;
  failed += f;
  if (f > 0) process.stderr.write(out); // 실패 시 상세 출력
}

const status = failed === 0 && total > 0 ? "PASS" : "FAIL";
const report = { status, total, passed, failed, files: perFile, generatedAt: new Date().toISOString() };

mkdirSync(dirname(OUT), { recursive: true });
writeFileSync(OUT, JSON.stringify(report, null, 2) + "\n");

console.log(`[ ${total} tests found ] [ ${passed} tests successful ] [ ${failed} tests failed ]`);
console.log(`PROOF: total=${total} passed=${passed} failed=${failed} status=${status}`);
console.log(`→ ${OUT.replace(ROOT + "\\", "").replace(ROOT + "/", "")}`);

process.exit(status === "PASS" ? 0 : 1);
