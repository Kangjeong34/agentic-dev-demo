# 회원가입 OTP · TODO

> 출처: `01_feature/auth_feature_spec.md`(AC-1~AC-6), `02_screen/signup_otp_screen_spec.md`(SCR-1~5)
> 현재 핵심 기능·화면은 구현·검증 완료. 아래는 잔여 위험(Residual Risk) 기반 개선 항목.

## 완료 (구현·검증됨)

- [x] **AC-1** 회원가입 요청 시 (email, purpose) OTP 6자리 발급 + TTL 300초 — `OtpServiceTest`
- [x] **AC-2** OTP 유효 + 올바른 입력 → 계정 생성 — `OtpServiceTest`, `SignupServiceTest`
- [x] **AC-3** 5회 연속 오답 → OTP 잠금 — `OtpServiceTest`(wrong-then-lock)
- [x] **AC-4** TTL(300초) 경과 후 제출 → 만료 거부 — `OtpServiceTest`(expiry)
- [x] **AC-5** 동일 사용자 재요청 멱등성 보장(중복 계정 방지) — `SignupServiceTest`(idempotent)
- [x] **AC-6** `signup_otp` 화면 UI parity 일치 — `signup_otp.html` + `run_ui_parity.py`(1/1 PASS)
- [x] **화면** `signup_otp` 레이아웃 4요소(제목·안내문·OTP 입력·확인 버튼) 구현

## 잔여 작업 (Residual Risk 기반)

- [ ] **상태 메시지 노출** — 오답/잠금/만료 결과를 화면 텍스트로 노출 (현재 서비스 계층 판정만 존재)
  - [ ] 오답 시 사용자 안내 문구 + 남은 시도 횟수 표시
  - [ ] 5회 잠금 상태 안내 문구
  - [ ] TTL 만료 안내 문구 + 재발급 동선
- [ ] **접근성 보강**
  - [ ] OTP 입력에 `<label for>` 또는 `aria-label` 연결
  - [ ] 명시적 포커스 순서(`tabindex`) 검토
- [ ] **렌더 검증 한계** — 브라우저 비가용으로 픽셀·반응형 미검증. 실 환경에서 Playwright exactness gate 적용 검토

## 검증 게이트

```
python3 sdd/99_toolchain/01_automation/run_ui_parity.py   # → ui_parity 1/1 PASS
```
