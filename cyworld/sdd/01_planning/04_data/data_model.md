# 싸이월드 도메인 데이터 모델 (계획)

> 01_planning: 기능 명세(일촌·다이어리·방명록·도토리·미니룸)를 지지하는 핵심 엔티티와 관계.
> 구현 단계에서 스키마로 확정하며, 영속성 작업 시 SDD 스키마-패리티 규칙을 따른다.

## 엔티티

| 엔티티 | 핵심 필드 | 비고 |
| --- | --- | --- |
| `User` | id, displayName, statusMessage | 미니홈피 주인 |
| `Ilchon` | userA, userB, status(pending/accepted), nicknameA, nicknameB, review | 양방향 관계 1행으로 표현 (← ilchon AC) |
| `Diary` | id, ownerId, content, visibility(ALL/ILCHON/PRIVATE), createdAt | 공개범위는 Ilchon 상태로 판정 (← diary AC-2) |
| `GuestbookEntry` | id, ownerId, authorId, content, secret(bool), createdAt | 비밀글 노출 분기 (← guestbook AC-2) |
| `DotoriWallet` | userId, balance | 음수 불가 (← dotori AC-5) |
| `DotoriTransaction` | id, userId, paymentKey, delta, type(CHARGE/SPEND), createdAt | paymentKey 유니크 = 멱등 키 (← dotori AC-4) |
| `Item` | id, name, type(ROOM/BGM), price | 상점 시드 데이터 |
| `OwnedItem` | userId, itemId | 보유 목록 (← miniroom AC-1) |
| `MiniroomLayout` | userId, placements(json), representativeBgmId | 배치·대표곡 (← miniroom AC-3/AC-5) |

## 관계 요약

- `User 1 — N Diary / GuestbookEntry`
- `User N — N User` via `Ilchon` (status로 신청/성립 구분)
- `User 1 — 1 DotoriWallet`, `1 — N DotoriTransaction`
- `User N — N Item` via `OwnedItem`, `User 1 — 1 MiniroomLayout`

## 무결성 가드레일

- `DotoriTransaction.paymentKey` **UNIQUE** → 멱등 결제 (dotori AC-4).
- `DotoriWallet.balance >= 0` 제약 → 음수 잔액 방지 (dotori AC-5).
- `Ilchon` 동일 쌍 중복 방지 제약 → 중복 일촌 방지 (ilchon AC-4).
- `OwnedItem` (userId,itemId) UNIQUE → 중복 지급 방지.

## Residual Risk

- 데모는 인메모리/단일 노드 저장 가정. 분산·동시성 제약은 비기능 요구로 분리.
