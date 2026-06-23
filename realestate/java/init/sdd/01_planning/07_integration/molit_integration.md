# 외부 연계 계약 (07_integration): data.go.kr 국토교통부 실거래가

> 2단계 산출물입니다. 실재 공개 API와의 연계 계약과 회복력 정책을 박제합니다.
> 계약 정본은 `00_sources/01_apis/molit_apt_trade_api.md`입니다. 본 사업은 **상세(Dev) 엔드포인트**를 표준으로 채택합니다(2024 신규 항목 전부 포함).

## 엔드포인트
- 아파트 매매 실거래가(상세, 권장): `https://apis.data.go.kr/1613000/RTMSDataSvcAptTradeDev/getRTMSDataSvcAptTradeDev` (data.go.kr 15126469 / 상세 15126468)
- 기본 엔드포인트(`RTMSDataSvcAptTrade`)는 일부 신규 항목이 없어 사용하지 않습니다.
- 확장 후보(범위 외): 아파트 전월세 실거래가 data.go.kr 15126474.

## 요청 파라미터
| 파라미터 | 필수 | 의미 |
| --- | --- | --- |
| serviceKey | 필수 | 인증키 (환경변수 `MOLIT_SERVICE_KEY`로만 주입, 비노출). 직접 URL 조립 시 Decoding 키 사용 |
| LAWD_CD | 필수 | 법정동코드 5자리(시군구). 예: 서울 종로구 `11110` |
| DEAL_YMD | 필수 | 계약월 YYYYMM. 예: `202405` |
| pageNo / numOfRows | 선택 | 페이징. numOfRows 최대 1000으로 호출 수 최소화(PER-002) |

## 응답 처리 (XML 전제, SIR-002)
- 응답은 XML. `<response>` 아래 `<header>`(resultCode/resultMsg)와 `<body>`(items·numOfRows·pageNo·totalCount).
- `totalCount` 기준으로 전 페이지를 끝까지 수집(SFR-002), 누락 없이 정규화.
- 핵심 item 필드: `sggCd`, `umdNm`, `jibun`, `aptNm`, `aptSeq`, `excluUseAr`, `dealYear/Month/Day`, `dealAmount`(만원·콤마), `floor`, `buildYear`, `dealingGbn`, `estateAgentSggNm`, `cdealType`(해제 O)/`cdealDay`, `rgstDate`, `slerGbn`/`buyerGbn`, `landLeaseholdGbn`.

## 결과코드 처리 (API 공개명세 §6)
| resultCode | 의미 | 처리 |
| --- | --- | --- |
| 000 | 정상 | item 정규화·적재 |
| 03 | NODATA | 정상 종료(빈 결과) |
| 01 / 04 / 99 | 내부·HTTP·미상 오류 | 재시도 대상 |
| 22 | 트래픽 초과 | 백오프 후 재시도 또는 익일 재개 |
| 12 / 20 / 30 / 31 / 32 | 폐기·권한·키·기간·IP 오류 | 즉시 실패·알림(재시도 안 함) |

## 회복력 정책 (AC-2, SIR-005, resilience4j)
| 정책 | 값 |
| --- | --- |
| 타임아웃 | 연결·응답 타임아웃 설정 |
| 재시도 | 3회, 0.5s 간격 (재시도 대상 코드에 한함) |
| 서킷브레이커 | sliding window 20, 실패율 50%, open 10s |
| fallback | 빈 결과(부분 수집 허용, SFR-011) |

## 트래픽 한도 (CONR-001, PER-004)
- 개발계정 일일 10,000건. 시군구×계약월을 순차/제한 동시 수집으로 한도 안에서 호출 조절.
- 인증키 만료·교체는 설정 갱신으로 무중단 반영(SECR-005).

> data.go.kr는 트래픽·운영시간 제한이 있습니다. 외부 장애를 수집 경계 안에 가두어 거래원장·분석으로 전파시키지 않습니다.

## 법정동코드 조회 (DAR-008, SIR-006)
- 행정표준코드관리시스템 `https://www.code.go.kr/stdcode/regCodeL.do` (10자리 중 앞 5자리). 전국 250개 시군구를 마스터로 보관.
