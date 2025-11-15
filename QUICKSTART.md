# API Gateway - Quick Start Guide

## 🚀 Get Started in 5 Minutes

### Prerequisites Check
```bash
java -version  # Should be 17+
mvn -version   # Should be 3.9+
```

### Run Locally (H2 Database)

1. **Navigate to project**
   ```bash
   cd api-gateway
   ```

2. **Start the application**
   ```bash
   ./run.sh run-dev
   ```
   
   Or manually:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

3. **Access the API**
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - H2 Console: http://localhost:8080/h2-console

### Test the API

#### 1. Login with Test Account
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "apiKey": "GW_TEST_PARTNER_KEY_001",
    "secret": "test123"
  }'
```

Save the `token` from the response.

#### 2. Make an Authenticated Request
```bash
curl -X GET http://localhost:8080/api/v1/partners/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

#### 3. Register a New Partner
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Company",
    "email": "contact@mycompany.com",
    "secret": "mypassword123",
    "roles": ["USER"]
  }'
```

### Run with Docker Compose (Full Stack)

```bash
# Start all services
./run.sh docker-up

# View logs
docker-compose logs -f

# Stop services
./run.sh docker-down
```

This starts:
- API Gateway on port 8080
- PostgreSQL on port 5432
- Redis on port 6379
- Prometheus on port 9090
- Grafana on port 3000

## 📝 Default Test Credentials

### Admin Account
- **API Key**: `GW_ADMIN_DEFAULT_KEY_001`
- **Secret**: `admin123`
- **Roles**: ADMIN, USER

### Test Partner
- **API Key**: `GW_TEST_PARTNER_KEY_001`
- **Secret**: `test123`
- **Roles**: USER

⚠️ **Change these in production!**

## 🔗 Important URLs

| Service | URL | Notes |
|---------|-----|-------|
| API | http://localhost:8080 | Main endpoint |
| Swagger UI | http://localhost:8080/swagger-ui.html | API Documentation |
| Health Check | http://localhost:8080/health | Status check |
| Actuator | http://localhost:8080/actuator | Spring Boot Actuator |
| Prometheus | http://localhost:9090 | Metrics (Docker only) |
| Grafana | http://localhost:3000 | Dashboards (Docker only) |
| H2 Console | http://localhost:8080/h2-console | DB Console (Dev only) |

## 📚 Next Steps

1. **Read the Documentation**
   - `README.md` - Complete feature documentation
   - `DEPLOYMENT.md` - Production deployment guide

2. **Import Postman Collection**
   - File: `API_Gateway.postman_collection.json`
   - Contains all API endpoints ready to test

3. **Explore Swagger UI**
   - Interactive API documentation
   - Try out endpoints directly from the browser

4. **Check Audit Logs**
   ```bash
   curl http://localhost:8080/api/v1/audit/my-logs \
     -H "Authorization: Bearer YOUR_TOKEN"
   ```

## 🛠️ Available Scripts

```bash
./run.sh build         # Build the project
./run.sh test          # Run tests
./run.sh run-dev       # Run with dev profile
./run.sh run-prod      # Run with production profile
./run.sh docker-build  # Build Docker image
./run.sh docker-up     # Start with Docker Compose
./run.sh docker-down   # Stop Docker services
```

## 🐛 Troubleshooting

**Port already in use?**
```bash
lsof -ti:8080 | xargs kill -9
```

**Can't connect to database?**
- In dev mode, H2 is in-memory, no setup needed
- For Docker, ensure containers are running: `docker-compose ps`

**Need help?**
- Check `DEPLOYMENT.md` for detailed troubleshooting
- Review logs: `tail -f logs/api-gateway.log`
- Check health: `curl http://localhost:8080/health`

## 📊 Quick Performance Check

```bash
# Check health
curl http://localhost:8080/health | jq

# View metrics
curl http://localhost:8080/actuator/metrics | jq

# Check database status
curl http://localhost:8080/actuator/health | jq '.components.db'
```

## 🎯 What's Included

✅ JWT Authentication & Authorization  
✅ Rate Limiting  
✅ IP Whitelisting  
✅ Comprehensive Audit Logging  
✅ Role-Based Access Control  
✅ Database Migrations (Flyway)  
✅ Redis Caching  
✅ Prometheus Metrics  
✅ Docker Support  
✅ Swagger Documentation  
✅ Integration Tests  
✅ Health Checks  

---

**Happy Coding! 🚀**

For full documentation, see README.md
