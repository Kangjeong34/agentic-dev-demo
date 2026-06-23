# -*- coding: utf-8 -*-
"""생성기(generator): proof 게이트 산출물에서 proof_evidence.md 를 자동 생성한다.

toolchain 세 갈래 중 '생성기' 역할이다. 사람이 테스트 결과를 보고 손으로 증거
문서를 쓰는 대신, 이 스크립트가 TestRunner 가 남긴 tmp/proof-results.json 을 파싱해
sdd/04_verify/10_test/proof_evidence.md 를 만든다.

이커머스 데모의 gen_proof_evidence.py 와 같은 패턴(입력 → 출력 단방향)이다.
이커머스는 Gradle JUnit XML 을 읽지만, 미니홈피는 JDK26 폴백 러너가 남기는
tmp/proof-results.json 을 신뢰원으로 삼는다(표준 환경에서는 동일 JSON 을 Gradle 이 생성).

사용법:
    # 먼저 proof 게이트를 실행해 tmp/proof-results.json 을 갱신한다
    #   표준(JDK17~21): ./gradlew test
    #   현재 환경(JDK26): tools/TestRunner.java 로 컴파일·실행 (04_verify 재현 명령 참조)
    python3 sdd/99_toolchain/01_automation/gen_proof_evidence.py
    python3 sdd/99_toolchain/01_automation/gen_proof_evidence.py --dry-run
"""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
RESULTS_FILE = ROOT / "tmp/proof-results.json"
OUT_FILE = ROOT / "sdd/04_verify/10_test/proof_evidence.md"


# ── 입력 파서 ─────────────────────────────────────────────────────────────────

def _is_screen(name: str) -> bool:
    n = name.lower()
    return "screen" in n or "parity" in n


def load_results(results_file: Path) -> dict:
    data = json.loads(results_file.read_text(encoding="utf-8"))
    data.setdefault("classes", [])
    return data


# ── 마크다운 생성기 ───────────────────────────────────────────────────────────

def render(data: dict) -> str:
    total = data.get("total", 0)
    passed = data.get("passed", 0)
    failed = data.get("failed", 0)
    status = data.get("status", "FAIL" if failed else "PASS")
    gate = data.get("gate", "proof gate")
    classes = sorted(data.get("classes", []), key=lambda c: c["name"])

    status_line = "PROOF PASS" if status == "PASS" else "PROOF FAIL"
    screen_classes = [c for c in classes if _is_screen(c["name"])]
    domain_classes = [c for c in classes if not _is_screen(c["name"])]

    lines: list[str] = [
        "# 04_verify · proof 증빙",
        "",
        "> **생성기 산출물입니다.** `gen_proof_evidence.py` 가 `tmp/proof-results.json` 에서 자동 생성했습니다.",
        "> 이 파일은 'SDD가 코드로 통과시켰다'의 공식 증거입니다. 손으로 수정하지 않습니다.",
        "",
        "## 요약",
        "",
        "```",
        status_line,
        f"gate = {gate}",
        f"total tests = {total} · passed = {passed} · failed = {failed}",
    ]
    for c in classes:
        lines.append(f"{c['name']:<28}{c['passed']}/{c['tests']}")
    lines += ["```", ""]

    if domain_classes:
        lines += [
            f"## 도메인 테스트 ({sum(c['tests'] for c in domain_classes)}개)",
            "",
            "| 클래스 | 통과 | 실패 |",
            "| --- | --- | --- |",
        ]
        for c in domain_classes:
            lines.append(f"| {c['name']} | {c['passed']} | {c['failed']} |")
        lines.append("")

    if screen_classes:
        lines += [
            f"## 화면 parity ({sum(c['tests'] for c in screen_classes)}개)",
            "",
            "| 클래스 | 통과 | 실패 |",
            "| --- | --- | --- |",
        ]
        for c in screen_classes:
            lines.append(f"| {c['name']} | {c['passed']} | {c['failed']} |")
        lines.append("")

    lines += [
        "## 게이트 판정",
        "",
        f"- proof 게이트: `tmp/proof-results.json` → **{status_line}**",
        f"- 전체 {total}개 중 {passed}개 통과, {failed}개 실패.",
        "",
    ]
    if status == "PASS":
        lines.append("> **통과** — 모든 AC 가 JUnit 으로 검증되었습니다.")
    else:
        lines.append("> **실패** — 실패한 케이스를 수정한 뒤 게이트를 다시 실행합니다.")
    lines.append("")
    return "\n".join(lines)


# ── 메인 ──────────────────────────────────────────────────────────────────────

def main() -> int:
    parser = argparse.ArgumentParser(description="proof 게이트 산출물 → proof_evidence.md 생성기")
    parser.add_argument("--dry-run", action="store_true", help="파일을 쓰지 않고 내용만 출력합니다")
    parser.add_argument("--results-file", default=str(RESULTS_FILE),
                        help=f"proof JSON 경로 (기본: {RESULTS_FILE.relative_to(ROOT)})")
    args = parser.parse_args()

    results_file = Path(args.results_file)
    if not results_file.exists():
        print(f"[gen_proof_evidence] proof 결과 파일이 없습니다: {results_file}", file=sys.stderr)
        print("  먼저 ./gradlew test (또는 TestRunner) 를 실행해 주세요.", file=sys.stderr)
        return 1

    data = load_results(results_file)
    content = render(data)

    if args.dry_run:
        print(content)
        print("[dry-run] 실제 파일을 쓰지 않았습니다.")
        return 0

    OUT_FILE.parent.mkdir(parents=True, exist_ok=True)
    OUT_FILE.write_text(content, encoding="utf-8")
    print(f"[gen_proof_evidence] proof 결과 → {OUT_FILE.relative_to(ROOT)}")
    print(f"  {data.get('status', '?')}: 통과 {data.get('passed', 0)}/{data.get('total', 0)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
