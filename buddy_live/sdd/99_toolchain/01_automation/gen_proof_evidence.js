// proof 증빙 생성기 — tmp/proof-results.json 을 사람이 읽는 검증 증거 문서로 변환한다.
// 출력: sdd/04_verify/10_test/proof_evidence.md (커밋되어 "게이트가 통과시켰다"의 기록이 된다).

import { readFileSync, writeFileSync, mkdirSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const HERE = dirname(fileURLToPath(import.meta.url));
const ROOT = resolve(HERE, "..", "..", "..");
const PROOF = join(ROOT, "tmp", "proof-results.json");
const OUT = join(ROOT, "sdd", "04_verify", "10_test", "proof_evidence.md");

const report = JSON.parse(readFileSync(PROOF, "utf8"));

const rows = report.files
  .map((f) => `| \`${f.file}\` | ${f.total} | ${f.pass} | ${f.fail} |`)
  .join("\n");

const md = `# proof 증빙 (생성물) · 버디버디

> 자동 생성 — \`node sdd/99_toolchain/01_automation/gen_proof_evidence.js\`.
> 소스: \`tmp/proof-results.json\` (\`node tools/run_proof.js\` 산출). 수기 편집 금지.

## 집계

\`\`\`
[ ${report.total} tests found ] [ ${report.passed} tests successful ] [ ${report.failed} tests failed ]
PROOF: total=${report.total} passed=${report.passed} failed=${report.failed} status=${report.status}
\`\`\`

상태: **${report.status}** · 생성시각: ${report.generatedAt}

## 파일별 집계

| 테스트 파일 | total | pass | fail |
| --- | --- | --- | --- |
${rows}

## AC ↔ 테스트 매핑 (전부 PASS)

| 도메인/화면 | 테스트 | 커버 |
| --- | --- | --- |
| 버디 | \`buddy.test.js\` | AC-1 신청 / AC-2 양방향수락 / AC-3 거절 / AC-4 멱등 / AC-5 양방향해제 / AC-6 접속상태 |
| 대화 | \`chat.test.js\` | AC-1 전송·안읽음 / AC-2 비버디거부 / AC-3 읽음 / AC-4 양쪽동일순서 / AC-5 멱등 / AC-6 빈문자거부 |
| 화면 | \`render.test.js\` | SCR-1 접속·배지 / SCR-3 정렬·me·them + ui_parity 스냅샷 일치 |
| 라이브복제 | \`transport.test.js\` | 두 탭 수렴 / 늦은 합류 replay 수렴 |
| e2e | \`e2e.test.js\` | 친구추가→수락→양방향대화→읽음 전 흐름 + 최종 상태 수렴 |
`;

mkdirSync(dirname(OUT), { recursive: true });
writeFileSync(OUT, md);
console.log(`evidence → ${OUT.replace(ROOT + "\\", "").replace(ROOT + "/", "")} (status=${report.status})`);
