# 데이터 모델 (04_data)

> 2단계 산출물입니다. 표준 거래 스키마·멱등 자연키·정합 규칙·집계 모델을 정의합니다.
> 사상의 단일 사실 원천은 `00_sources/03_data_spec/realprice_data_spec.md`이며, 본 문서가 내부 표준(AptTransaction)으로의 정본 사상입니다.

## 1. 표준 거래 스키마 (AptTransaction)

| 필드 | 타입 | 의미 | 원천 매핑 | 변환 |
| --- | --- | --- | --- | --- |
| sggCd | String(5) | 법정동 시군구코드 | `sggCd` | 동일 |
| umdNm | String | 법정동(읍면동)명 | `umdNm` | trim |
| aptNm | String | 단지명 | `aptNm` | trim |
| exclusiveArea | double | 전용면적(㎡) | `excluUseAr` | 문자열→double |
| floor | int | 층 | `floor` | 문자열→int |
| buildYear | Integer | 건축년도 | `buildYear` | 문자열→int (결측 허용) |
| dealYear/Month/Day | int | 계약 연/월/일 | `dealYear/Month/Day` | 문자열→int |
| dealAmountWon | long | 거래금액(원) | `dealAmount`(만원·콤마) | 공백·콤마 제거 → ×10000 |
| canceled | boolean | 해제여부 | `cdealType` | `== "O"` → boolean |
| canceledDate | LocalDate? | 해제사유발생일 | `cdealDay` | `YY.MM.DD`→date (결측 허용) |
| rgstDate | LocalDate? | 등기일자 | `rgstDate` | `YY.MM.DD`→date (결측 허용) |
| dealingGbn | String? | 거래유형(중개/직거래) | `dealingGbn` | trim (결측 허용) |
| slerGbn / buyerGbn | String? | 매도자·매수자 구분(비식별) | `slerGbn`/`buyerGbn` | trim (결측 허용) |
| estateAgentSggNm | String? | 중개사 소재지 | `estateAgentSggNm` | trim (결측 허용) |
| landLeaseholdGbn | String? | 토지임대부 여부(Y/N) | `landLeaseholdGbn` | trim (결측 허용) |

> 등기·거래유형 보존 항목은 손실 없이 적재합니다(SFR-012). 모든 원천 값은 문자열로 내려오므로 형 변환 책임은 시스템에 있습니다(CONR-002, DAR-006).

## 2. 멱등 자연키 (AC-4, DAR-003)
```
naturalKey = sggCd | umdNm | aptNm | exclusiveArea | floor
           | dealYear | dealMonth | dealDay | dealAmountWon
```
- 거래원장 테이블에 자연키 유니크 제약을 둡니다. 같은 구간 재수집(백필 포함)에도 중복 행이 생기지 않는 upsert입니다.
- 사후 해제 전환(CONR-005): 동일 자연키 재수집 시 `canceled`/`canceledDate`는 갱신될 수 있습니다(원본 행은 보존, DAR-005).

## 3. 정합 규칙 (AC-3)
- **금액 변환(DAR-002):** `dealAmount` `" 82,500"`(만원) → 공백·콤마 제거 → `82500` → ×10000 → `825,000,000`원. 변환 실패·비양수 건은 스킵·보고(품질 게이트).
- **해제 제외(DAR-004):** `canceled = true`(`cdealType == "O"`)인 거래는 원장에 **적재하되**(논리적 보존, DAR-005) 시세 집계에서 **제외**합니다.
- 네 핵심 변환(만원→원, 문자열→숫자, 해제→boolean, `YY.MM.DD`→날짜)은 `common`의 공유 파서 한 곳에서만 수행합니다(03_architecture 판단 3).

## 4. 품질 게이트 (적재 전, data_spec §5)
- 헤더 `resultCode = 000` 응답만 적재 대상. 그 외는 적재하지 않고 회복력 정책 처리(AC-2).
- 필수 항목(`sggCd·umdNm·aptNm·excluUseAr·floor·dealYear·dealMonth·dealDay·dealAmount`) 충족·금액 양수·면적>0. 위반 건은 스킵·보고(부분 수집 허용, SFR-011).
- `totalCount` 대비 누락 없이 전 페이지 수집(SFR-002).

## 5. read model (MarketStat, AC-5 / DAR-007)
| 필드 | 의미 | 산출 |
| --- | --- | --- |
| sggCd / dealYear / dealMonth | 집계 키(시군구·계약 연월) | 그룹 키 |
| tradeCount | 집계 대상 거래 수 | `canceled=false` 건수 |
| medianPriceWon | 중위 거래금액(원) | 유효 거래 `dealAmountWon` 분포의 중위값 |
| medianPricePerM2Won | 중위 ㎡당 단가(원) | 거래별 `dealAmountWon / exclusiveArea` 분포의 중위값 |

> 통계는 거래원장(write model)이 아니라 analytics read model에서 산출·제공합니다(SFR-009). 해제 거래는 모든 집계에서 제외합니다.
