# AltRadar - 알트코인 급등 예측 시스템

AltRadar는 기술적 분석을 기반으로 알트코인의 급등 가능성을 예측하는 Spring Boot 기반 웹 애플리케이션입니다.

## 🚀 주요 기능

- **실시간 데이터 수집**: CoinGecko API를 통한 실시간 암호화폐 데이터 수집
- **기술적 분석**: RSI, MACD, 볼린저 밴드, 이동평균선 등 다양한 기술적 지표 분석
- **급등 가능성 점수**: 종합적인 분석을 통한 0-100점 급등 가능성 점수 계산
- **트렌드 분석**: 상승/하락/횡보 트렌드 자동 분석
- **리스크 평가**: 투자 리스크 레벨 자동 평가
- **웹 대시보드**: 직관적인 웹 인터페이스를 통한 결과 확인
- **REST API**: 외부 시스템과의 연동을 위한 REST API 제공

## 🛠 기술 스택

- **Backend**: Spring Boot 3.3.4, Java 21
- **Database**: H2 Database (In-Memory)
- **Build Tool**: Maven
- **Frontend**: Thymeleaf, Bootstrap 5, JavaScript
- **HTTP Client**: WebClient (Reactive)
- **Technical Analysis**: 자체 구현 기술적 분석 라이브러리

## 📋 요구사항

- Java 21 이상
- Maven 3.6 이상

## 🚀 설치 및 실행

### 1. 프로젝트 클론
```bash
git clone <repository-url>
cd AltRadar
```

### 2. 애플리케이션 실행
```bash
mvn spring-boot:run
```

### 3. 웹 브라우저에서 접속
```
http://localhost:8080
```

## 📊 주요 API 엔드포인트

### REST API
- `GET /api/crypto/pump-potential` - 상위 급등 가능성 코인 조회
- `GET /api/crypto/bullish-high-potential` - 상승 트렌드 고점수 코인 조회
- `GET /api/crypto/small-cap-high-potential` - 소형 시가총액 고점수 코인 조회
- `GET /api/crypto/{coinId}` - 특정 코인 상세 정보 조회
- `POST /api/crypto/update` - 데이터 수동 업데이트

### 웹 페이지
- `/` - 메인 대시보드
- `/pump-potential` - 급등 가능성 코인 목록
- `/bullish` - 상승 트렌드 코인 목록
- `/small-cap` - 소형 시가총액 코인 목록
- `/all-coins` - 전체 코인 목록

## 🔍 기술적 분석 지표

### 급등 가능성 점수 계산 요소
1. **RSI (Relative Strength Index)**: 과매도/과매수 상태 분석
2. **MACD**: 이동평균 수렴확산 지수
3. **볼린저 밴드**: 가격 변동성 및 지지/저항선 분석
4. **이동평균선**: 단기/장기 이동평균 비교
5. **거래량 분석**: 거래량 증가율 분석
6. **모멘텀**: 가격 변화율 분석
7. **변동성**: 가격 변동성 분석

### 점수 기준
- **80-100점**: 높은 급등 가능성 (LOW RISK)
- **60-79점**: 중간 급등 가능성 (MEDIUM RISK)
- **0-59점**: 낮은 급등 가능성 (HIGH RISK)

## 📈 트렌드 분석

- **BULLISH**: 상승 트렌드
- **BEARISH**: 하락 트렌드
- **SIDEWAYS**: 횡보 트렌드

## 🔧 설정

`application.yml` 파일에서 다음 설정을 변경할 수 있습니다:

```yaml
# CoinGecko API 설정
coingecko:
  api:
    base-url: https://api.coingecko.com/api/v3
    rate-limit: 1000

# 기술적 분석 설정
technical-analysis:
  rsi-period: 14
  macd-fast: 12
  macd-slow: 26
  macd-signal: 9
  sma-short: 20
  sma-long: 50
  bollinger-period: 20
  bollinger-std-dev: 2.0

# 급등 예측 설정
pump-prediction:
  min-score: 70
  max-risk-level: MEDIUM
  update-interval: 3600000
```

## ⚠️ 주의사항

- 이 시스템은 투자 조언이 아닌 참고용 도구입니다
- 실제 투자 결정은 충분한 연구와 전문가 상담을 통해 이루어져야 합니다
- 암호화폐 투자는 높은 위험을 수반합니다
- 과거 성과가 미래 수익을 보장하지 않습니다

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

## 📞 문의

프로젝트에 대한 문의사항이 있으시면 이슈를 등록해 주세요.
