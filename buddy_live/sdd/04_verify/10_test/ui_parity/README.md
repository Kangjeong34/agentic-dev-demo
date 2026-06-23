# UI parity 스냅샷

`messenger.html` 은 `renderMessenger(canonicalState(), "alice", "bob")` 의 결정적 출력입니다.
`tests/render.test.js` 가 실제 렌더러 출력과 이 파일을 **정확히 일치**(`assert.equal`)로 대조해
화면-도메인 일치를 검증합니다.

- 입력 상태: `tests/fixtures.js::canonicalState()` (alice 시점, bob 선택).
- EOL: `buddy_live/.gitattributes` 에서 `eol=lf` 고정(OS 무관 결정성).
- 재생성(렌더 의도 변경 시에만):

```
node --input-type=module -e "import {renderMessenger} from './src/web/render.js'; import {canonicalState,VIEWER,SELECTED} from './tests/fixtures.js'; import {writeFileSync} from 'node:fs'; writeFileSync('sdd/04_verify/10_test/ui_parity/messenger.html', renderMessenger(canonicalState(),VIEWER,SELECTED));"
```

재생성 후에는 반드시 `node tools/run_proof.js` 로 17/17 PASS 를 재확인합니다.
