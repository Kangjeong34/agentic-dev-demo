# 99_toolchain · 자동화 게이트 (verify 중점)

버디버디 데모의 완료 기준은 "사람이 봤다"가 아니라 "게이트가 통과시켰다"입니다.
두 개의 게이트 + 하나의 생성기로 구성됩니다(무의존 — Node 24 내장 기능만).

## 게이트 1 · proof 테스트 게이트

```
node tools/run_proof.js     # 또는: npm test (= node --test)
```

버디6 + 대화6 + 렌더2 + transport2 + e2e1 = **17 케이스**를 결정적으로 실행합니다.
브라우저 비가용 환경이라 화면은 Playwright 대신 **순수 렌더 함수의 결정적 HTML 스냅샷 대조**
(`render.test.js` ↔ `ui_parity/messenger.html`)로 검증합니다.
exit 0 = 통과이며, 결과는 `tmp/proof-results.json`(집계 + 파일별 통계)에 기록됩니다.

> **UI parity 결정성**: 스냅샷(`sdd/04_verify/10_test/ui_parity/messenger.html`)은 `buddy_live/.gitattributes`
> 에서 `eol=lf` 로 고정합니다. Windows 체크아웃의 CRLF 변환이 parity 를 거짓 FAIL 시키지 않도록 하는 핀입니다.

## 게이트 2 · 도메인 경계 게이트 (구조)

```
node sdd/99_toolchain/01_automation/run_arch_check.js
```

두 규칙을 코드로 판정합니다.

- 규칙 1: 도메인(`src/domain`)은 화면(`src/web`)을 import 하지 않습니다. 도메인은 화면을 먹여 살릴 뿐
  화면에 의존하지 않습니다(단방향). 위반 시 exit 1.
- 규칙 2: 도메인은 무의존입니다 — 상대경로/`node:` 빌트인 외 외부 패키지 import 금지.

이 경계가 "도메인 리듀서를 화면 없이도 테스트로 증명"하는 verify 전략을 구조적으로 보장합니다.

## 생성기 · proof 증빙

```
node sdd/99_toolchain/01_automation/gen_proof_evidence.js
```

`tmp/proof-results.json` 을 사람이 읽는 `04_verify/10_test/proof_evidence.md` 로 변환합니다(커밋되는 증거).

## 한 번에 (권장 순서)

```
node tools/run_proof.js \
  && node sdd/99_toolchain/01_automation/run_arch_check.js \
  && node sdd/99_toolchain/01_automation/gen_proof_evidence.js
```
