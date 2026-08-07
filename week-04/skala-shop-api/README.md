# SKALA 온라인 쇼핑몰 API

상품, 고객, 주문을 관리하는 Spring Boot 기반 REST API 프로젝트입니다.

강의 교안 PDF 527~561페이지의 온라인 쇼핑몰 실습을 기준으로 처음부터 구현합니다. 별도 스켈레톤 코드는 사용하지 않으며, 과제 안내에 따라 JWT 인증과 Docker 적용은 제외합니다.

## 1. 프로젝트 목표

- 상품과 고객 정보를 CRUD 방식으로 관리합니다.
- 고객은 보유 포인트를 이용해 상품을 주문합니다.
- 주문 시 포인트와 주문 수량을 하나의 트랜잭션으로 처리합니다.
- 주문 취소 시 수량을 줄이고 결제 포인트를 환급합니다.
- Validation과 전역 예외 처리로 잘못된 요청을 일관되게 처리합니다.
- AOP로 API 요청, 응답 및 처리 시간을 기록합니다.
- Actuator로 애플리케이션 상태를 확인합니다.

## 2. 구현 범위

### 포함

- 상품 CRUD 및 페이징 조회
- 고객 CRUD 및 페이징 조회
- 고객 로그인 정보 검증
- 고객별 주문 상품 목록 조회
- 상품 주문 및 취소
- 상품 재고 차감·복구 및 재고 부족 검증
- JPA 낙관적 락을 이용한 동시 주문 재고 보호
- 주문·취소 이력 저장
- 구매 이력 기반 개인화 상품 추천
- JPA 엔터티 연관관계
- 주문·취소 트랜잭션
- Jakarta Validation
- 전역 예외 처리
- Swagger/OpenAPI
- AOP API 로깅
- Spring Boot Actuator
- H2 인메모리 데이터베이스
- 자동화 테스트

### 제외

- JWT 토큰 발급 및 검증
- 로그인 세션과 쿠키
- Docker 이미지 생성 및 배포
- 실제 결제 시스템

JWT를 사용하지 않으므로 주문과 취소 요청에는 `customerId`를 직접 포함합니다.

## 3. 기술 스택

- Java 21
- Spring Boot 3.2.0
- Gradle 8.5
- Spring Web
- Spring Data JPA
- Jakarta Validation
- Spring AOP
- Spring Boot Actuator
- H2 Database
- Springdoc OpenAPI
- JUnit 5

## 4. 패키지 구성 계획

```text
src/main/java/com/skala/shop
├── ShopApplication.java
├── controller/     REST API 요청 처리
├── service/        비즈니스 로직과 트랜잭션
├── repository/     Spring Data JPA 저장소
├── entity/         Product, Customer, OrderItem, OrderHistory
├── dto/            요청·응답 전용 객체
├── exception/      오류 코드와 전역 예외 처리
├── common/         공통 응답과 페이징 응답
└── aop/            API 요청·응답 로깅
```

Controller는 HTTP 요청과 응답만 담당하고, 핵심 주문 로직은 Service에 구현합니다.

## 5. 도메인 설계

### Product

| 필드 | 형식 | 설명 |
|---|---|---|
| id | Long | 자동 생성 상품 ID |
| productName | String | 상품명, 중복 불가 |
| productPrice | Long | 상품 가격, 1원 이상 |
| category | ProductCategory | 상품 카테고리 |
| stockQuantity | Integer | 현재 주문 가능한 재고 수량 |
| version | Long | 동시 주문 충돌을 감지하는 JPA 버전 |

### Customer

| 필드 | 형식 | 설명 |
|---|---|---|
| customerId | String | 고객 ID이자 기본키 |
| customerPassword | String | 로그인 검증용 비밀번호 |
| customerPoint | Long | 상품 주문에 사용할 포인트 |

회원가입 시 초기 포인트는 `1,000,000`점으로 설정합니다. 비밀번호는 고객 응답 DTO에 포함하지 않습니다.

### OrderItem

| 필드 | 형식 | 설명 |
|---|---|---|
| id | Long | 자동 생성 주문 항목 ID |
| customer | Customer | 주문 고객, 다대일 관계 |
| product | Product | 주문 상품, 다대일 관계 |
| quantity | Integer | 고객이 주문한 상품 수량 |

한 고객이 같은 상품을 여러 번 주문하면 새로운 행을 만들지 않고 기존 `OrderItem.quantity`를 증가시킵니다.

### OrderHistory

| 필드 | 형식 | 설명 |
|---|---|---|
| id | Long | 자동 생성 이력 ID |
| customer | Customer | 주문 또는 취소를 실행한 고객 |
| product | Product | 대상 상품 |
| type | OrderType | `ORDER` 또는 `CANCEL` |
| quantity | Integer | 주문 또는 취소 수량 |
| unitPrice | Long | 처리 당시 상품 단가 |
| totalAmount | Long | 단가와 수량을 곱한 금액 |
| createdAt | LocalDateTime | 처리 시각 |

`OrderItem`은 현재 보유 수량을 나타내고, `OrderHistory`는 취소 후에도 남아 있는 과거 주문·취소 기록을 나타냅니다.

금액과 포인트는 소수점 계산 오류가 발생하지 않도록 교안의 `Double` 대신 원 단위 정수인 `Long`을 사용합니다.

## 6. API 설계

교안에 혼용된 URI를 일관된 REST 형식으로 정리하여 사용합니다.

### 상품 API

| Method | URI | 설명 |
|---|---|---|
| GET | `/api/products?page=0&size=10` | 상품 목록 페이징 조회 |
| GET | `/api/products/{id}` | 상품 상세 조회 |
| POST | `/api/products` | 상품 등록 |
| PUT | `/api/products/{id}` | 상품 수정 |
| DELETE | `/api/products/{id}` | 상품 삭제 |
| PATCH | `/api/products/{id}/stock` | 상품 재고 추가 |
| GET | `/api/products/popular?limit=5` | 전체 인기 상품 조회 |

### 고객 API

| Method | URI | 설명 |
|---|---|---|
| GET | `/api/customers?page=0&size=10` | 고객 목록 페이징 조회 |
| GET | `/api/customers/{customerId}` | 고객과 주문 상품 목록 조회 |
| POST | `/api/customers` | 고객 회원가입 |
| POST | `/api/customers/login` | 고객 ID와 비밀번호 검증 |
| PUT | `/api/customers/{customerId}` | 고객 포인트 수정 |
| DELETE | `/api/customers/{customerId}` | 고객 삭제 |
| POST | `/api/customers/order` | 상품 주문 |
| POST | `/api/customers/cancel` | 주문 취소 |
| GET | `/api/customers/{customerId}/recommendations?limit=5` | 구매 이력 기반 개인화 추천 |

### 주문·취소 요청

```json
{
  "customerId": "skala01",
  "productId": 1,
  "quantity": 2
}
```

## 7. 비즈니스 규칙

### 회원가입과 로그인

- 고객 ID와 비밀번호는 필수입니다.
- 이미 존재하는 고객 ID로 가입할 수 없습니다.
- 회원가입 시 초기 포인트 `1,000,000`점을 지급합니다.
- 로그인 시 고객 ID와 비밀번호가 모두 일치해야 합니다.
- 고객 조회와 로그인 응답에는 비밀번호를 노출하지 않습니다.

### 상품 관리

- 상품명은 필수이며 중복될 수 없습니다.
- 상품 가격은 1원 이상이어야 합니다.
- 상품 재고는 0개 이상이어야 합니다.
- 존재하지 않는 상품은 조회, 수정 또는 삭제할 수 없습니다.
- 주문 내역에서 사용 중인 상품의 삭제 정책은 구현 단계에서 명확한 오류 응답으로 제한합니다.

### 상품 주문

1. 고객과 상품이 존재하는지 확인합니다.
2. 수량이 1개 이상인지 검증합니다.
3. `상품 가격 × 수량`으로 주문 금액을 계산합니다.
4. 고객 포인트가 주문 금액보다 적으면 주문을 거부합니다.
5. 상품 재고가 주문 수량보다 적으면 주문을 거부합니다.
6. 포인트와 재고를 차감합니다.
7. 기존에 주문한 상품이면 수량을 누적하고, 없으면 새 주문 항목을 생성합니다.
8. `ORDER` 주문 이력을 저장합니다.
9. 포인트·재고·주문 항목·주문 이력을 하나의 `@Transactional` 작업으로 처리합니다.

### 주문 취소

1. 고객, 상품 및 주문 항목이 존재하는지 확인합니다.
2. 취소 수량이 보유 수량보다 많으면 거부합니다.
3. 취소 후 수량이 남으면 수량을 감소시킵니다.
4. 취소 후 수량이 0이면 주문 항목을 삭제합니다.
5. `상품 가격 × 취소 수량`만큼 고객 포인트를 환급합니다.
6. 취소 수량만큼 상품 재고를 복구합니다.
7. `CANCEL` 주문 이력을 저장합니다.
8. 수량·포인트·재고·주문 이력을 하나의 `@Transactional` 작업으로 처리합니다.

### 개인화 상품 추천

- 고객의 순수 주문 이력을 이용해 선호 카테고리를 계산합니다.
- 고객의 평균 구매 가격과 상품 가격의 유사도를 계산합니다.
- 전체 고객 주문량을 집계해 상품 인기도를 계산합니다.
- 품절 상품은 추천 대상에서 제외합니다.
- 구매 이력이 없는 신규 고객에게는 전체 인기 상품을 추천합니다.
- 카테고리 40점, 가격 적합도 30점, 인기도 20점, 재고 10점으로 추천 점수를 계산합니다.
- 응답에는 점수뿐 아니라 추천 이유도 함께 제공합니다.

## 8. 오류 처리 계획

| 오류 코드 | 적용 상황 | HTTP 상태 |
|---|---|---:|
| `INVALID_PARAMETER` | 필수값 누락, 잘못된 가격·수량 | 400 |
| `NOT_AUTHENTICATED` | 로그인 ID 또는 비밀번호 불일치 | 401 |
| `DATA_NOT_FOUND` | 고객, 상품 또는 주문을 찾을 수 없음 | 404 |
| `DATA_DUPLICATED` | 고객 ID 또는 상품명 중복 | 409 |
| `INSUFFICIENT_FUNDS` | 주문에 필요한 포인트 부족 | 400 |
| `INSUFFICIENT_QUANTITY` | 주문한 수량보다 많이 취소 | 400 |
| `INSUFFICIENT_STOCK` | 주문 수량보다 상품 재고가 부족 | 400 |
| `CONCURRENT_ORDER_CONFLICT` | 동시 주문으로 상품 버전 충돌 | 409 |

`@RestControllerAdvice`에서 모든 예외를 공통 JSON 형식으로 변환합니다.

## 9. 단계별 구현 계획

- [x] 1단계: Gradle 프로젝트와 Spring Boot 기본 설정 생성
- [x] 2단계: 초기 애플리케이션 컨텍스트 테스트
- [x] 3단계: Product, Customer, OrderItem 엔터티 구현
- [x] 4단계: 요청·응답 DTO 구현
- [x] 5단계: JPA Repository와 초기 상품 데이터 구현
- [x] 6단계: 카테고리·재고·낙관적 락·주문 이력 모델 확장
- [x] 7단계: 확장 DTO, Repository와 카테고리별 초기 데이터 구현
- [x] 8단계: 공통 응답, 오류 코드, 전역 예외 처리 구현
- [x] 9단계: 상품 CRUD·재고 관리·페이징 API 구현 및 테스트
- [x] 10단계: 고객 CRUD와 로그인 API 구현 및 테스트
- [x] 11단계: 재고·포인트·주문 이력 통합 주문/취소 구현
- [x] 12단계: 개인화 추천 점수와 추천 API 구현
- [x] 13단계: Validation 및 동시 주문 실패 시나리오 검증
- [x] 14단계: AOP 요청·응답·처리 시간 로깅 적용
- [x] 15단계: Actuator 상태 확인과 전체 시나리오 최종 점검

각 단계가 끝날 때 `./gradlew test`와 Swagger 수동 테스트를 함께 실행합니다.

## 10. 전체 검증 시나리오

1. 고객 `skala01`을 회원가입하고 초기 포인트가 `1,000,000`인지 확인합니다.
2. 올바른 ID와 비밀번호로 로그인합니다.
3. 상품 목록을 조회합니다.
4. 15,000원 상품을 2개 주문합니다.
5. 고객 포인트가 `970,000`으로 감소했는지 확인합니다.
6. 같은 상품을 다시 주문했을 때 수량이 누적되는지 확인합니다.
7. 주문 상품 목록에서 상품과 수량을 확인합니다.
8. 상품 1개를 취소하고 포인트가 15,000점 환급되는지 확인합니다.
9. 나머지 수량을 모두 취소하면 주문 항목이 삭제되는지 확인합니다.
10. 포인트 부족, 초과 취소, 잘못된 수량 및 없는 데이터 요청의 오류 응답을 확인합니다.
11. AOP 로그에서 API 요청, 응답 및 처리 시간을 확인합니다.
12. `/actuator/health`에서 `UP` 상태를 확인합니다.
13. 고객별 주문 카테고리를 다르게 만든 뒤 추천 순서가 서로 다른지 확인합니다.
14. 재고보다 많은 주문을 거부하고 취소 시 재고가 복구되는지 확인합니다.
15. 동시에 마지막 재고를 주문해도 재고가 음수가 되지 않는지 자동화 테스트로 확인합니다.

## 11. 실행 방법

```bash
./gradlew clean bootRun
```

| 용도 | URL |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Actuator Health | `http://localhost:8080/actuator/health` |
| H2 Console | `http://localhost:8080/h2-console` |

H2 접속 정보:

```text
JDBC URL: jdbc:h2:mem:shopdb
User Name: sa
Password: 입력하지 않음
```

### 초기 상품 데이터

`data.sql`에서 전자기기, 생활용품, 식품, 패션 카테고리별 상품 5개씩 총 20개를 제공합니다.

| 카테고리 | 상품 |
|---|---|
| ELECTRONICS | 무선마우스, 블루투스키보드, USB허브, 보조배터리, 스마트워치 |
| LIVING | 텀블러, 디퓨저, 수납바구니, 핸드워시, 쿠션 |
| FOOD | 그래놀라, 커피원두, 견과세트, 프로틴바, 녹차세트 |
| FASHION | 에코백, 후드티, 티셔츠, 볼캡, 양말세트 |

## 12. 완료 기준

- 모든 API가 Swagger에서 정상적으로 실행됩니다.
- 정상 요청은 적절한 HTTP 상태와 응답 DTO를 반환합니다.
- 잘못된 요청은 정해진 오류 코드와 HTTP 상태를 반환합니다.
- 주문과 취소 후 고객 포인트와 주문 수량이 일치합니다.
- 정상 및 예외 시나리오 자동화 테스트가 통과합니다.
- AOP 로그와 Actuator 상태를 확인할 수 있습니다.
