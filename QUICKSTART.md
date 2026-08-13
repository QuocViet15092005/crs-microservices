# Buổi 4 — Quick Start Guide

## 1️⃣ Setup Database

```sql
CREATE DATABASE auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 2️⃣ Check & Update Passwords

Check MySQL password in these files and update if needed:
- `auth-service/src/main/resources/application.properties`
- `course-service/src/main/resources/application.properties`  
- `registration-service/src/main/resources/application.properties`

Look for: `spring.datasource.password=`

## 3️⃣ Start Services (in order)

```bash
# Terminal 1: auth-service (8081)
cd auth-service
mvn spring-boot:run

# Terminal 2: course-service (8082)
cd course-service
mvn spring-boot:run

# Terminal 3: registration-service (8083)
cd registration-service
mvn spring-boot:run

# Terminal 4: api-gateway (8080)
cd api-gateway
mvn spring-boot:run
```

Wait for all services to be fully up before testing.

## 4️⃣ Test Accounts

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| student1 | student123 | STUDENT |

## 5️⃣ Quick Test with cURL

**Get JWT Token:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**Get Courses (no auth needed):**
```bash
curl http://localhost:8080/api/courses
```

**Create Course (ADMIN only):**
```bash
curl -X POST http://localhost:8080/api/courses \
  -H "Authorization: Bearer <your_token_here>" \
  -H "Content-Type: application/json" \
  -d '{"tenMonHoc":"Test","soTinChi":3,"soChoToiDa":30}'
```

**Use API Key (public endpoint):**
```bash
curl http://localhost:8080/api/public/courses \
  -H "X-API-KEY: crs-partner-key-2026"
```

## 6️⃣ Import to Postman

1. Open Postman
2. Click "Import" (top-left)
3. Select "Buoi_4_Postman_Collection.json"
4. All requests are ready to use!

**⚠️ Important:** Update token values in requests after login, as they expire after 24 hours.

## 7️⃣ Verify JWT Token

Copy the token (middle part between dots) and paste at https://jwt.io to decode and verify:
- `sub`: username
- `role`: user role (ADMIN or STUDENT)
- `exp`: expiration timestamp

## 8️⃣ Common Issues

| Issue | Solution |
|-------|----------|
| Connection refused on port 8080/8081/8082/8083 | Services not started - check Terminal |
| 401 Unauthorized on POST /api/courses | Missing Authorization header |
| 403 Forbidden on POST /api/courses | Student token - need admin token |
| Token invalid on another service | jwt.secret mismatch - verify all 3 files have same secret |
| api-gateway won't start | Spring Cloud version mismatch - recreate project from start.spring.io |

## 9️⃣ File Locations

- **Complete Guide**: `BUOI_4_GUIDE.md`
- **Postman Collection**: `Buoi_4_Postman_Collection.json`
- **Auth Service**: `auth-service/`
- **API Gateway**: `api-gateway/`
- **Updated Services**: `course-service/`, `registration-service/`

## 🔟 Commit to Git

```bash
git add auth-service/
git commit -m "init: auth-service with JWT login + seed data"

git add api-gateway/
git commit -m "init: api-gateway routing + auth header filter + api key filter"

git add course-service/ registration-service/
git commit -m "feat: jwt verification + role-based authorization across services"

git push
```

---

**🎯 Key Learning**: Gateway fast-rejects missing tokens, but each service independently verifies JWT signatures — Zero Trust architecture.
