# 회귀 검증 범위 (Regression Scope)

> 02_plan: 변경의 직접 대상과 상류/하류/공유 표면을 선택하고 근거를 남긴다.

## 직접 대상 (direct)

5개 도메인 서비스의 전 AC 케이스 (`*ServiceTest`).

## 공유/연계 표면 (shared)

| 연계 | 위험 | 검증 케이스 |
| --- | --- | --- |
| 다이어리 공개범위 ← 일촌 상태 | 일촌 수락/해제가 ILCHON 글 노출을 바꿔야 함 | `DiaryServiceTest::list_respects_visibility`, `ilchon_only_blocks_stranger` |
| 미니룸 구매 ← 도토리 차감/멱등 | 구매가 도토리 잔액·멱등성을 정확히 재사용해야 함 | `MiniroomServiceTest::buy_charges_dotori_and_grants`, `buy_rejected_when_insufficient`, `duplicate_buy_click_idempotent` |

## 제외 (justified exclusions)

- 웹/HTTP 계층: 미구현(범위 밖).
- 화면 UI parity: 별도 화면 태스크로 분리.
- 영속 DB 스키마: 인메모리 구현이라 해당 없음.

## 결과

전 범위 27/27 PASS (`sdd/04_verify/01_feature/cyworld.md`).
