# Buổi 4 — Auth Service (JWT), API Gateway & Phân Quyền xuyên Service

## Hoàn Thành Tất Cả Yêu Cầu

Tất cả các file code cho Buổi 4 đã được tạo. Dưới đây là hướng dẫn chi tiết để chạy và test toàn bộ hệ thống.

## 1. Chuẩn Bị Database

Chạy lệnh SQL sau để tạo database cho auth-service:

```sql
CREATE DATABASE auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 2. Cấu Hình Database Passwords

Các file `application.properties` sau đã được cập nhật, nhưng bạn cần kiểm tra password MySQL:

- **auth-service**: `src/main/resources/application.properties`
- **course-service**: `src/main/resources/application.properties`
- **registration-service**: `src/main/resources/application.properties`

Nếu password MySQL khác, hãy cập nhật trong `spring.datasource.password=` của cả 3 service.

## 3. Khởi Động Các Services

**Thứ tự khởi động:**

1. **auth-service** (port 8081)
   ```
   cd auth-service
   mvn clean install
   mvn spring-boot:run
   ```

2. **course-service** (port 8082)
   ```
   cd course-service
   mvn clean install
   mvn spring-boot:run
   ```

3. **registration-service** (port 8083)
   ```
   cd registration-service
   mvn clean install
   mvn spring-boot:run
   ```

4. **api-gateway** (port 8080) — khởi động cuối cùng
   ```
   cd api-gateway
   mvn clean install
   mvn spring-boot:run
   ```

Nếu lần đầu khởi động auth-service, DataSeeder sẽ tự động tạo 2 tài khoản mẫu:
- **admin / admin123** (ROLE_ADMIN)
- **student1 / student123** (ROLE_STUDENT)

## 4. Test toàn bộ hệ thống qua Gateway (localhost:8080)

Sử dụng Postman hoặc cURL. Tất cả request từ đây trở đi đều qua Gateway, không gọi thẳng 8081/8082/8083.

### 4.1 Đăng nhập lấy JWT Token

**Request 1: Đăng nhập với tài khoản ADMIN**

```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiIsImlhdCI6MTc4NTIyOTY2MiwiZXhwIjoxNzg1MzE2MDYyfQ.j0Hbz2-tO25m5pumiw_a6wmIXqyIfctHzPD76Lp4IKQ",
  "username": "admin",
  "role": "ADMIN"
}
```

**Lưu token này để dùng trong các request tiếp theo**.

---

**Request 2: Đăng nhập với tài khoản STUDENT**

```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "student1",
  "password": "student123"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdHVkZW50MSIsInJvbGUiOiJTVFVERU5UIiwiaWF0IjoxNzg1MjMwMDI1LCJleHAiOjE3ODUzMTY0MjV9.ruogk8UWCwPgvw3d8oRvDRFhmvup7MEmOmNj9OHZGEE",
  "username": "student1",
  "role": "STUDENT"
}
```

### 4.2 Xem danh sách môn học (PUBLIC, không cần token)

```
GET http://localhost:8080/api/courses
```

**Response (200 OK):**
Trả về danh sách tất cả các môn học.

---

### 4.3 Tạo môn học (POST) — CHỈ ADMIN

**Request: Cố gắng tạo môn học mà không có Authorization Header**

```
POST http://localhost:8080/api/courses
Content-Type: application/json

{
  "tenMonHoc": "Kien truc phan mem",
  "soTinChi": 3,
  "soChoToiDa": 30
}
```

**Response (401 Unauthorized):**
Gateway chặn ngay vì thiếu Header Authorization.

---

**Request: Cố gắng tạo môn học với token của STUDENT**

```
POST http://localhost:8080/api/courses
Authorization: Bearer <token_cua_student1>
Content-Type: application/json

{
  "tenMonHoc": "Kien truc phan mem",
  "soTinChi": 3,
  "soChoToiDa": 30
}
```

**Response (403 Forbidden):**
Vượt qua Gateway (có header), nhưng course-service tự từ chối vì STUDENT không phải ROLE_ADMIN.

---

**Request: Tạo môn học với token của ADMIN (thành công)**

```
POST http://localhost:8080/api/courses
Authorization: Bearer <token_cua_admin>
Content-Type: application/json

{
  "tenMonHoc": "Kien truc phan mem",
  "soTinChi": 3,
  "soChoToiDa": 30
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "tenMonHoc": "Kien truc phan mem",
  "soTinChi": 3,
  "soChoToiDa": 30,
  "soChoTrong": 30
}
```

---

### 4.4 Đăng ký môn học (registration-service)

**Request: Đăng ký (yêu cầu Authentication, cả ADMIN và STUDENT đều được)**

```
POST http://localhost:8080/api/registrations
Authorization: Bearer <token_cua_student1>
Content-Type: application/json

{
  "studentId": 1,
  "courseId": 1
}
```

**Response (201 Created):**
Ghi nhận đăng ký, registration-service sẽ tự verify JWT + gọi ngầm sang course-service qua đường nội bộ.

---

### 4.5 API công khai cho đối tác (sử dụng X-API-KEY)

**Request: Xem danh sách môn học bằng API Key (không cần JWT)**

```
GET http://localhost:8080/api/public/courses
X-API-KEY: crs-partner-key-2026
```

**Response (200 OK):**
Trả về danh sách môn học cho đối tác ngoài.

---

**Request: Sai API Key**

```
GET http://localhost:8080/api/public/courses
X-API-KEY: sai-key
```

**Response (403 Forbidden):**
ApiKeyFilter chặn.

---

**Request: Thiếu API Key hoặc không có**

```
GET http://localhost:8080/api/public/courses
```

**Response (403 Forbidden):**
ApiKeyFilter chặn.

---

## 5. Kiểm Tra JWT Token bên trong

Sao chép token JWT từ response login (phần nằm giữa 2 dấu chấm `.`), dán vào https://jwt.io để xem payload:

**Ví dụ token của admin:**
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiIsImlhdCI6MTc4NTIyOTY2MiwiZXhwIjoxNzg1MzE2MDYyfQ.j0Hbz2-tO25m5pumiw_a6wmIXqyIfctHzPD76Lp4IKQ
```

**Payload:**
```json
{
  "sub": "admin",
  "role": "ADMIN",
  "iat": 1785229662,
  "exp": 1785316062
}
```

- `sub`: username
- `role`: role của user
- `iat`: thời điểm cấp token
- `exp`: thời điểm hết hạn token (86400000 ms = 24 giờ)

---

## 6. Các file chính đã tạo

### auth-service
- `auth-service/pom.xml` — Dependencies Spring Security + JJWT
- `auth-service/src/main/resources/application.properties` — Database + JWT config
- `auth-service/src/main/java/vn/edu/crs/authservice/entity/User.java` — Entity lưu user
- `auth-service/src/main/java/vn/edu/crs/authservice/entity/Student.java` — Entity sinh viên
- `auth-service/src/main/java/vn/edu/crs/authservice/security/JwtUtil.java` — Sinh JWT
- `auth-service/src/main/java/vn/edu/crs/authservice/dto/{LoginRequestDTO, LoginResponseDTO}.java`
- `auth-service/src/main/java/vn/edu/crs/authservice/service/AuthService.java` — Logic login
- `auth-service/src/main/java/vn/edu/crs/authservice/controller/AuthController.java` — Endpoint /auth/login
- `auth-service/src/main/java/vn/edu/crs/authservice/config/SecurityConfig.java` — Config bảo mật (mở public)
- `auth-service/src/main/java/vn/edu/crs/authservice/config/DataSeeder.java` — Tạo 2 user mẫu
- `auth-service/src/main/java/vn/edu/crs/authservice/exception/{InvalidCredentialsException, GlobalExceptionHandler}.java`

### api-gateway
- `api-gateway/pom.xml` — Spring Cloud Gateway + BOM
- `api-gateway/src/main/resources/application.yml` — Routing rules + CORS config
- `api-gateway/src/main/java/vn/edu/crs/apigateway/filter/AuthHeaderFilter.java` — Chặn sớm nếu thiếu Authorization
- `api-gateway/src/main/java/vn/edu/crs/apigateway/filter/ApiKeyFilter.java` — Kiểm tra X-API-KEY
- `api-gateway/src/main/java/vn/edu/crs/apigateway/ApiGatewayApplication.java`

### course-service (updated)
- `course-service/pom.xml` — Thêm Spring Security + JJWT
- `course-service/src/main/resources/application.properties` — Thêm jwt.secret
- `course-service/src/main/java/vn/edu/crs/courseservice/security/JwtAuthFilter.java` — Xác thực JWT độc lập
- `course-service/src/main/java/vn/edu/crs/courseservice/config/SecurityConfig.java` — Phân quyền: GET public, POST/PUT/DELETE chỉ ADMIN

### registration-service (updated)
- `registration-service/pom.xml` — Thêm Spring Security + JJWT
- `registration-service/src/main/resources/application.properties` — Thêm jwt.secret
- `registration-service/src/main/java/vn/edu/crs/registrationservice/security/JwtAuthFilter.java` — Xác thực JWT độc lập
- `registration-service/src/main/java/vn/edu/crs/registrationservice/config/SecurityConfig.java` — Tất cả /registrations/** yêu cầu authentication

---

## 7. Nguyên tắc bảo mật đã áp dụng

1. **Gateway chặn sớm** (AuthHeaderFilter, ApiKeyFilter) — nhẹ tải, giảm request lên các service
2. **Mỗi service tự xác thực JWT** (JwtAuthFilter) — Không tin tưởng mù quáng Gateway
3. **Phân quyền Role** — course-service: GET public, POST/PUT/DELETE chỉ ADMIN
4. **API Key riêng** — /api/public/courses dùng X-API-KEY, không cần JWT
5. **/internal/** không khai báo trong Gateway — chỉ gọi nội bộ (registration-service → course-service)

---

## 8. Commit Git

```bash
cd d:\PTPMHDV\Spring Boot\course-service

git add auth-service/
git commit -m "init: auth-service with JWT login + seed data"

git add api-gateway/
git commit -m "init: api-gateway routing + auth header filter + api key filter"

git add course-service/ registration-service/
git commit -m "feat: jwt verification + role-based authorization across services"

git push
```

---

## Lưu Ý

- JWT secret được lưu trong `application.properties` (chỉ dùng cho lab). Trong production, dùng environment variables hoặc secret manager.
- Token hết hạn sau 24 giờ (86400000 ms) — có thể điều chỉnh `jwt.expiration-ms`.
- Nếu gặp lỗi token invalid, kiểm tra jwt.secret ở cả 3 service có giống nhau không.
- CORS được cấu hình sẵn tại Gateway cho http://localhost:5173 (dùng cho Buổi 5).
