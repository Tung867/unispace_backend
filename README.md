# UniSpace — Backend (REST API)

> 대학생 맞춤형 공간 예약 REST API 서버
> Spring Boot 3 · MySQL · JWT · Google Drive 백업 · Docker · GitHub Actions · Prometheus/Grafana

이 저장소는 UniSpace 프로젝트의 **백엔드 표준(레퍼런스) 구현**입니다. **B 파트**(Core/Admin)와 **C 파트**(보안/JWT, Google Drive 백업, Docker, CI/CD, 모니터링) 로직을 모두 포함합니다. React 프론트엔드(A 파트)는 별도 폴더/저장소입니다.

---

## 1. 기술 스택

| 항목 | 기술 |
|---|---|
| 언어 / 프레임워크 | Java 17, Spring Boot 3.2.5 |
| 보안 | Spring Security + JWT (jjwt) |
| DB / ORM | MySQL 8 + Spring Data JPA |
| API 문서화 | Swagger UI (springdoc-openapi) |
| 외부 백업 | Google Drive API (예약 로그를 .txt 로 저장) |
| 인프라 | Docker, docker-compose |
| CI/CD | GitHub Actions |
| 모니터링 | Spring Actuator + Micrometer → Prometheus + Grafana |

---

## 2. Docker 로 바로 실행 (권장)

필요: **Docker Desktop**

```bash
cp .env.example .env          # 환경 변수 파일 생성
docker compose up --build     # app + mysql + prometheus + grafana 빌드 & 실행
```

실행 후 접속 주소:

| 서비스 | URL |
|---|---|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health check | http://localhost:8080/actuator/health |
| Metrics (Prometheus 포맷) | http://localhost:8080/actuator/prometheus |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 (admin / admin) |

> **포트 주의 (자주 발생하는 오류):** MySQL/8080/3000/9090 포트가 이미 사용 중이면 `.env` 의 `*_PORT` 값을 변경하세요.

### 자동 생성되는 계정 (DataInitializer)
- **관리자:** `admin` / `admin1234`
- 첫 실행 시 데모용 공간 2개 + 시설 3개(모니터, 빔 프로젝터, 화이트보드) 자동 생성.

---

## 3. Docker 없이 로컬 실행 (개발용)

JDK 17 + 실행 중인 MySQL 필요. `unispace` 데이터베이스 생성 후:

```bash
# Windows PowerShell 환경 변수 설정 예시
$env:DB_HOST="localhost"; $env:DB_PASSWORD="your_pw"
mvn spring-boot:run
```

기본값은 `localhost:3306`, 계정 `unispace` / 비밀번호 `unispace1234` (application.yml 또는 환경 변수로 변경 가능).

---

## 4. 폴더 구조

```
src/main/java/com/unispace
├── UniSpaceApplication.java
├── config/        # Security, Swagger, CORS, DataInitializer
├── security/      # JWT provider, filter, UserDetailsService   ← C 파트
├── domain/
│   ├── user/      # User, Role, UserRepository
│   ├── room/      # Room, Facility, Repository (+ Lock 쿼리)
│   └── reservation/ # Reservation, status, Repository (중복 검사 쿼리)
├── dto/           # request / response (Java record)
├── service/       # AuthService, UserService, RoomService,
│                  # ReservationService (Lock), AdminService,
│                  # GoogleDriveBackupService                  ← C 파트
├── controller/    # Auth, User, Room, Reservation, Admin
└── exception/     # ErrorCode, BusinessException, GlobalExceptionHandler
```

---

## 5. 주요 API 목록

| Method | Endpoint | 권한 | 설명 |
|---|---|---|---|
| POST | `/api/auth/signup` | public | 회원가입 |
| POST | `/api/auth/login` | public | 로그인 → JWT 발급 |
| GET | `/api/rooms` | public | 공간 목록 |
| GET | `/api/rooms/{id}/availability` | public | 시설 + 점유 시간대 현황 |
| GET/PUT | `/api/users/me` | USER | 프로필 조회/수정 (소속) |
| POST | `/api/reservations` | USER | 공간 예약 (**중복 시간대 충돌 방지**) |
| POST | `/api/reservations/{id}/return` | USER | 반납 |
| DELETE | `/api/reservations/{id}` | USER | 취소 |
| GET | `/api/reservations/me` | USER | 내 예약 목록 |
| POST/PUT/DELETE | `/api/admin/rooms` | ADMIN | 공간 관리 + 시설 편집 |
| POST/GET | `/api/admin/facilities` | ADMIN | 시설 관리 |
| GET | `/api/admin/users` | ADMIN | 사용자 목록 |
| PATCH | `/api/admin/users/{id}/role` | ADMIN | 권한 변경 |
| GET | `/api/admin/reservations` | ADMIN | 전체 예약 대시보드 |
| POST | `/api/admin/reservations/{id}/force-cancel` | ADMIN | 강제 취소 |
| POST | `/api/admin/backup` | ADMIN | Google Drive 백업 |

> Swagger UI 에서 **Authorize** 버튼을 누르고 토큰을 붙여넣으세요 ("Bearer " 는 입력하지 않아도 됨).
> `api.http` 파일에 샘플 요청이 준비되어 있습니다 (VS Code/IntelliJ REST Client 용).

---

## 6. 중복 대여 충돌 방지 (Lock)

proposal 의 핵심 기능입니다. 예약 생성 시(`ReservationService.reserve`):

1. 트랜잭션을 열고 `findByIdForUpdate` 로 `Room` 행에 **비관적 쓰기 락(PESSIMISTIC_WRITE)** → 같은 공간에 대한 동시 요청을 순차 처리.
2. 시간대가 **겹치는 예약** 조회 (`start < newEnd AND end > newStart`, status = RESERVED).
3. 겹치면 `RESERVATION_CONFLICT`(HTTP 409) 반환, 없으면 저장.

1번 덕분에 두 사용자가 동시에 예약을 눌러도 검사 단계를 우회할 수 없어 중복 예약이 방지됩니다.

---

## 7. Google Drive 백업 (C 파트)

기본값은 **비활성화**(`GOOGLE_DRIVE_ENABLED=false`) — 자격증명 없이도 앱이 바로 실행되도록 함.

활성화 방법:
1. Google Cloud 에서 **서비스 계정** 생성 → **Drive API** 활성화 → JSON 키 파일 다운로드.
2. 파일을 `config/google-credentials.json` 에 배치 (`.gitignore` 처리됨, 컨테이너에 `:ro` 마운트).
3. (선택) Drive 폴더 생성 후 서비스 계정 이메일에 공유, 폴더 ID 를 `GOOGLE_DRIVE_FOLDER_ID` 에 설정.
4. `.env` 에서 `GOOGLE_DRIVE_ENABLED=true` 로 변경 후 재실행.
5. `POST /api/admin/backup` 호출 → 전체 예약 내역을 `.txt` 로 만들어 Drive 에 업로드.

---

## 8. 모니터링 (Prometheus + Grafana)

- 앱이 `/actuator/prometheus` 에 메트릭 노출.
- Prometheus(`monitoring/prometheus.yml`)가 15초마다 앱을 스크랩.
- Grafana 는 Prometheus 를 데이터소스로 자동 연결하고, **UniSpace 대시보드가 자동으로 로드**됩니다 (`monitoring/grafana/dashboards/unispace-dashboard.json`). http://localhost:3000 접속 → 좌측 Dashboards → "UniSpace - Application Dashboard" → 요청 수/응답시간 p95/JVM 힙/CPU/스레드/DB 커넥션 그래프 확인.

---

## 9. CI/CD (GitHub Actions)

`.github/workflows/ci.yml` — `main`, `develop` 브랜치 `push`/`pull_request` 시 실행:
1. JDK 17 설정 (Maven 캐시).
2. `mvn clean verify` — 테스트는 **H2 인메모리**로 실행되어 CI 에 MySQL 불필요.
3. `docker build` 로 Dockerfile 빌드 검증.

proposal 의 브랜치 전략(`feature/* → develop → main`)과 일치.

### 테스트 실행 (동시성 포함)
```bash
mvn test
```
- `UniSpaceApplicationTests` — 스프링 컨텍스트 로드 검증.
### 배포 (GHCR - Docker 이미지 자동 배포)
`.github/workflows/release.yml` — `main` 브랜치 push 시 Docker 이미지를 빌드해 **GitHub Container Registry(ghcr.io)** 에 자동 배포 (별도 서버 불필요).

준비: repo → **Settings → Actions → General → Workflow permissions → "Read and write permissions"** 선택 (이미지 push 권한).

배포된 이미지 사용:
```bash
docker pull ghcr.io/<계정>/unispace-backend:latest
docker run -p 8080:8080 ghcr.io/<계정>/unispace-backend:latest
```
> 이미지 태그: `latest`(기본 브랜치) + 커밋 SHA + 태그(`v*`) 푸시 시 버전 태그.
> 패키지가 비공개라 pull 이 안 되면 repo 의 Packages 설정에서 visibility 를 public 으로 변경.

---

## 10. (선택·심화) MySQL Master-Slave Replication

이 버전은 단순함과 안정성을 위해 단일 MySQL 로 동작합니다. proposal 의 "DB Replication (Master-Slave)" 를 구현하려면 `AbstractRoutingDataSource` 로 읽기/쓰기를 라우팅할 수 있습니다:

```java
// 아이디어: 쓰기 -> master, 읽기(readOnly) -> slave
public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                ? "slave" : "master";
    }
}
```
master/slave 2개의 datasource + `LazyConnectionDataSourceProxy` 가 함께 필요합니다. 원하시면 이 부분도 완전하게 작성해 드릴게요.

---

## 11. 중요 참고 사항

- 파일 생성 환경에서는 **Java 컴파일/실행을 하지 못했습니다**(Maven Central 접근 차단). 따라서 본인 PC 에서 `docker compose up --build` 또는 `mvn clean verify` 로 실행하세요. 오류 로그를 주시면 함께 고치겠습니다.
- 본 프로젝트는 A/B/C 로 역할이 나뉜 팀 과제입니다. 이 버전은 참고/대조용으로 B + C 를 모두 작성했으니, 사용 전 팀원과 합의해 주세요.
