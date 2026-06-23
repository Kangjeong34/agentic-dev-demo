# ui_parity: 화면 스냅샷 sidecar

- `minihompy_main.html`: 미니홈피 메인 화면의 **캐노니컬 스냅샷**(검증 기준이자 열람 가능한 화면).
- 게이트 `MinihompyScreenParityTest::render_matches_canonical_snapshot` 가
  `MinihompyFixture.render()` 출력과 이 파일을 글자 단위로 대조한다.
- 스냅샷 갱신(의도적 화면 변경 시): `tools/SnapshotGen.java` 실행 → 이 파일 재생성 후 diff 검토.
- 실 강의에서는 Playwright exactness 의 회차별 PNG/JSON 증거가 이 트리에 쌓인다.
  본 환경(브라우저 비가용)에서는 결정적 HTML parity 로 대체한다.

## 화면 열람

`minihompy_main.html` 은 CSS 내장 단독 문서다. 브라우저로 바로 열어 시각 확인 가능.
