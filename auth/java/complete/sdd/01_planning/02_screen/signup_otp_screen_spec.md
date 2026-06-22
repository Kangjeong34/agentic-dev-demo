# 회원가입 OTP 화면 명세서 (signup_otp)

> 01_planning: 회원가입 OTP 입력 화면(`signup_otp`)의 설계 기준을 정의한다.
> 출처는 요구사항 원문(`00_sources/02_requirements/auth-signup.md`)과 기능 명세
> (`01_feature/auth_feature_spec.md`, AC-1~AC-6)이며, 화면 동작은 기능 AC와 1:1로 매핑된다.
> 본 문서는 이미 구현·검증된 화면을 정제한 설계 명세이며 신규 코드 변경은 없다(문서 전용, rollout 미발생).
> 사실의 원천(렌더 결과)은 캐노니컬 스냅샷 `04_verify/10_test/ui_parity/signup_otp.html` 이다.

## 화면 개요

| 항목 | 내용 |
| --- | --- |
| 화면 ID | `signup_otp` |
| 목적 | 이메일로 발급된 6자리 OTP를 입력받아 회원가입을 완료한다. |
| 진입 조건 | 회원가입 요청으로 (email, purpose)에 묶인 OTP가 발급된 직후 (← AC-1) |
| 정상 이탈 | 유효한 OTP 입력 → 계정 생성 후 다음 단계로 이동 (← AC-2) |
| 비정상 이탈 | 5회 연속 오답으로 OTP 잠금(← AC-3) 또는 TTL(300초) 만료 거부(← AC-4) |
| 디자인 기준 | 승인된 스냅샷과 UI parity 일치 (← AC-6) |

## 레이아웃 구조

`<main class="signup">` 단일 컨테이너 안에 4개 요소가 세로 순서로 배치된다.

| 순서 | 요소 | HTML | 역할 |
| --- | --- | --- | --- |
| 1 | 제목 | `<h1>인증번호 입력</h1>` | 화면 제목 |
| 2 | 안내문 | `<p>이메일로 받은 6자리 인증번호를 입력하세요.</p>` | 입력 가이드 |
| 3 | OTP 입력 | `<input name="otp" inputmode="numeric" maxlength="6"/>` | 6자리 인증번호 입력 |
| 4 | 확인 버튼 | `<button type="submit">확인</button>` | OTP 검증 제출 |

## 요소·필드 명세

| 요소 | 라벨/문구 | 속성·제약 | 필수 |
| --- | --- | --- | --- |
| 제목 (`h1`) | `인증번호 입력` | 고정 텍스트 | — |
| 안내문 (`p`) | `이메일로 받은 6자리 인증번호를 입력하세요.` | 고정 텍스트 | — |
| OTP 입력 (`input`) | (placeholder 없음) | `name="otp"`, `inputmode="numeric"`(숫자 키패드 유도), `maxlength="6"`(6자리 제한) | 필수 |
| 확인 버튼 (`button`) | `확인` | `type="submit"` | — |

> 컨테이너 클래스는 `signup`. 위 요소·속성·문구는 캐노니컬 스냅샷과 글자 단위로 일치해야 한다(UI parity 대상).

## 상호작용·상태 명세 (EARS)

**SCR-1** When 사용자가 6자리 OTP를 입력하고 '확인'을 제출하면, the system shall 입력값을
(email, purpose)에 묶인 OTP와 검증한다. (← AC-2)

**SCR-2** While OTP가 유효할 때, when 올바른 OTP가 제출되면, the system shall 계정을 생성하고
화면을 정상 이탈시킨다. (← AC-2)

**SCR-3** When OTP를 5회 연속 틀리면, the system shall 해당 OTP를 잠그고 추가 시도를 거부한다. (← AC-3)

**SCR-4** When OTP TTL(300초)이 지난 뒤 제출되면, the system shall 검증을 만료로 거부한다. (← AC-4)

**SCR-5** When 같은 사용자의 가입이 재요청·재제출되어도, the system shall 멱등성을 보장해
계정을 중복 생성하지 않는다. (← AC-5)

## 문구(메시지) 명세

| 구분 | 위치 | 문구 |
| --- | --- | --- |
| 고정 | 제목 | `인증번호 입력` |
| 고정 | 안내문 | `이메일로 받은 6자리 인증번호를 입력하세요.` |
| 고정 | 버튼 | `확인` |

> 상태별(오답/잠금/만료) 사용자 메시지는 현재 화면 스냅샷에 포함되어 있지 않다. 본 데모 환경에서는
> 결과 처리를 서비스 계층(OTP 검증)에서 판정하며, 화면 텍스트로의 노출은 미구현 영역이다(아래 Residual Risk).

## 접근성

| 항목 | 현재 구현 | 비고 |
| --- | --- | --- |
| 숫자 입력 힌트 | `inputmode="numeric"` 제공 | 모바일 숫자 키패드 유도 |
| 입력 길이 제한 | `maxlength="6"` 제공 | 6자리 초과 입력 차단 |
| 라벨 연결 | 미구현 | `<label for>`/`aria-label` 부재 — 개선 후보 |
| 포커스 순서 | DOM 순서 기반 기본값 | 명시적 `tabindex` 없음 |

## 디자인 일치 기준 (UI parity)

- 캐노니컬 스냅샷: `sdd/04_verify/10_test/ui_parity/signup_otp.html`
- 검증 게이트: `python3 sdd/99_toolchain/01_automation/run_ui_parity.py` → `ui_parity 1/1 PASS`
- 실 강의 데모는 Playwright exactness gate로 픽셀 비교하지만, 본 환경(브라우저 비가용)에서는
  결정적 HTML parity로 대체한다. (← AC-6)

## 검증 매핑

| 화면 동작 | 기능 AC | 테스트 |
| --- | --- | --- |
| SCR-1·SCR-2 (정상 제출→계정 생성) | AC-2 | `src/test/.../OtpServiceTest`, `SignupServiceTest` |
| SCR-3 (5회 오답 잠금) | AC-3 | `OtpServiceTest`(wrong-then-lock) |
| SCR-4 (TTL 만료 거부) | AC-4 | `OtpServiceTest`(expiry) |
| SCR-5 (멱등성) | AC-5 | `SignupServiceTest`(idempotent) |
| 화면 일치 | AC-6 | `signup_otp.html` + `99_toolchain/01_automation/run_ui_parity.py` |

## Residual Risk

- 실제 렌더 픽셀·반응형 레이아웃은 미검증(브라우저 비가용) — 결정적 HTML parity로만 보증.
- 오답/잠금/만료에 대한 화면 노출 메시지가 미구현(서비스 계층 판정만 존재). 화면 텍스트 명세는 추후 보강 대상.
- 접근성 라벨/포커스 명시는 현재 미구현 — 개선 후보.
