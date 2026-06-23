# 실거래가 수집·집계 · Acceptance Criteria (EARS)

> 출처: `00_sources/02_requirements/realfield-부동산실거래.md`(원문 요구 1~5 → AC-1~AC-5),
> `00_sources/01_apis/molit_apt_trade_api.md`(연계 계약), `00_sources/03_data_spec/realprice_data_spec.md`(정합 규칙).
> 각 AC는 통과/실패를 판정할 수 있는 검증 가능한 명세입니다. 구현 에이전트는 이 다섯 줄을 벗어날 수 없습니다.

AC-1  When 특정 시군구(LAWD_CD 5자리)·계약월(DEAL_YMD, YYYYMM)로 수집을 요청하면,
      the system shall data.go.kr 아파트 매매 실거래가 상세 API(getRTMSDataSvcAptTradeDev)를
      totalCount까지 전 페이지 호출하고, item을 표준 스키마(AptTransaction)로 정규화해
      거래원장에 적재한다. (→ SFR-001~003, DAR-001)

AC-2  When data.go.kr 응답이 지연·실패하거나 resultCode가 000이 아니면,
      the system shall 재시도 → 서킷브레이커 → 빈 결과(부분 수집)로 우아하게 저하하고,
      성공 구간은 적재하며 배치를 계속한다. 외부 장애가 파이프라인 전체를 멈추지 않는다.
      (회복력 → SFR-011, SIR-005, PER-003)

AC-3  The 거래금액 파싱은 shall 콤마·선행 공백 포함 만원 문자열(" 82,500")의
      공백·콤마를 제거해 만원 정수를 얻고 ×10000 하여 원 단위 정수(825,000,000)로 변환하며,
      해제된 거래(cdealType = O)는 canceled=true로 적재하되 시세 집계에서 제외한다.
      (데이터 정합 → SFR-004·006, DAR-002·004, data_spec §2.6·§3.1)

AC-4  When 동일 (시군구·계약월) 수집이 재실행(백필 포함)되면,
      the system shall 자연키 기반 멱등 upsert로 중복 거래를 만들지 않는다.
      재수집 시 cdealType 정상→해제 전환은 반영한다. (→ SFR-005·013, DAR-003, CONR-005)

AC-5  When 시세 통계를 조회하면,
      the system shall 거래원장(write model)이 아니라 analytics read model(MarketStat)에서
      해제 제외 후 중위 거래금액·중위 ㎡당 단가·집계 거래 수를 반환한다.
      (CQRS 읽기 분리 → SFR-008·009, DAR-007, PER-001·003)

AC-R  회귀: 게이트웨이 라우팅·디스커버리 등록·기존 조회 API 계약이 무손상이어야 한다. (→ SIR-003·004)
