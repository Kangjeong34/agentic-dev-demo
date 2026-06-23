# proof 증빙 (생성물) · 버디버디

> 자동 생성 — `node sdd/99_toolchain/01_automation/gen_proof_evidence.js`.
> 소스: `tmp/proof-results.json` (`node tools/run_proof.js` 산출). 수기 편집 금지.

## 집계

```
[ 17 tests found ] [ 17 tests successful ] [ 0 tests failed ]
PROOF: total=17 passed=17 failed=0 status=PASS
```

상태: **PASS** · 생성시각: 2026-06-23T05:34:54.340Z

## 파일별 집계

| 테스트 파일 | total | pass | fail |
| --- | --- | --- | --- |
| `tests/buddy.test.js` | 6 | 6 | 0 |
| `tests/chat.test.js` | 6 | 6 | 0 |
| `tests/e2e.test.js` | 1 | 1 | 0 |
| `tests/render.test.js` | 2 | 2 | 0 |
| `tests/transport.test.js` | 2 | 2 | 0 |

## AC ↔ 테스트 매핑 (전부 PASS)

| 도메인/화면 | 테스트 | 커버 |
| --- | --- | --- |
| 버디 | `buddy.test.js` | AC-1 신청 / AC-2 양방향수락 / AC-3 거절 / AC-4 멱등 / AC-5 양방향해제 / AC-6 접속상태 |
| 대화 | `chat.test.js` | AC-1 전송·안읽음 / AC-2 비버디거부 / AC-3 읽음 / AC-4 양쪽동일순서 / AC-5 멱등 / AC-6 빈문자거부 |
| 화면 | `render.test.js` | SCR-1 접속·배지 / SCR-3 정렬·me·them + ui_parity 스냅샷 일치 |
| 라이브복제 | `transport.test.js` | 두 탭 수렴 / 늦은 합류 replay 수렴 |
| e2e | `e2e.test.js` | 친구추가→수락→양방향대화→읽음 전 흐름 + 최종 상태 수렴 |
