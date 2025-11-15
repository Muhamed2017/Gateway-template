# API Gateway Deployment Guide

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Production Deployment](#production-deployment)
4. [Kubernetes Deployment](#kubernetes-deployment)
5. [Monitoring Setup](#monitoring-setup)
6. [Troubleshooting](#troubleshooting)

## Prerequisites

### Required Software
- Java 17 or higher
- Maven 3.9+
- PostgreSQL 16+ (for production)
- Redis 7+ (for production)
- Docker & Docker Compose (optional)

### Environment Setup

#### Development Environment
```bash
# Install Java 17 (Ubuntu/Debian)
sudo apt update
sudo apt install openjdk-17-jdk

# Verify installation
java -version
```

#### Database Setup (Production)
```bash
# PostgreSQL
sudo apt install postgresql-16

# Create database
sudo -u postgres createdb api_gateway
sudo -u postgres createuser -P api_user

# Grant privileges
sudo -u postgres psql
GRANT ALL PRIVILEGES ON DATABASE api_gateway TO api_user;
```

## Local Development Setup

### Option 1: Quick Start with H2 (In-Memory Database)

```bash
# Clone repository
git clone <repository-url>
cd api-gateway

# Run with dev profile
./run.sh run-dev
```

Access points:
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: (leave empty)

### Option 2: Full Stack with Docker Compose

```bash
# Start all services (PostgreSQL, Redis, App, Prometheus, Grafana)
./run.sh docker-up

# View logs
docker-compose logs -f api-gateway

# Stop services
./run.sh docker-down
```

## Production Deployment

### Step 1: Build the Application

```bash
# Build JAR file
mvn clean package -DskipTests

# Or use the script
./run.sh build
```

The JAR file will be created at `target/api-gateway-1.0.0.jar`

### Step 2: Configure Environment Variables

Create a `.env` file:

```bash
# Database Configuration
export DATABASE_URL=jdbc:postgresql://your-db-host:5432/api_gateway
export DATABASE_USERNAME=api_user
export DATABASE_PASSWORD=your_secure_password

# Redis Configuration
export REDIS_HOST=your-redis-host
export REDIS_PORT=6379
export REDIS_PASSWORD=your_redis_password

# JWT Configuration (CRITICAL - Must be 256-bit or longer)
export JWT_SECRET=your-super-secret-jwt-key-change-this-to-a-very-long-random-string

# Application Configuration
export SPRING_PROFILES_ACTIVE=prod
```

### Step 3: Run the Application

```bash
# Source environment variables
source .env

# Run the application
java -jar target/api-gateway-1.0.0.jar
```

### Step 4: Verify Deployment

```bash
# Health check
curl http://localhost:8080/actuator/health

# Expected response
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

## Kubernetes Deployment

### Step 1: Build Docker Image

```bash
docker build -t your-registry/api-gateway:1.0.0 .
docker push your-registry/api-gateway:1.0.0
```

### Step 2: Create Kubernetes Manifests

#### ConfigMap (k8s/configmap.yaml)
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: api-gateway-config
data:
  SPRING_PROFILES_ACTIVE: "prod"
  DATABASE_URL: "jdbc:postgresql://postgres-service:5432/api_gateway"
  REDIS_HOST: "redis-service"
  REDIS_PORT: "6379"
```

#### Secret (k8s/secret.yaml)
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: api-gateway-secret
type: Opaque
stringData:
  DATABASE_USERNAME: "api_user"
  DATABASE_PASSWORD: "your_password"
  REDIS_PASSWORD: "your_redis_password"
  JWT_SECRET: "your-very-long-secret-key"
```

#### Deployment (k8s/deployment.yaml)
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
      - name: api-gateway
        image: your-registry/api-gateway:1.0.0
        ports:
        - containerPort: 8080
        envFrom:
        - configMapRef:
            name: api-gateway-config
        - secretRef:
            name: api-gateway-secret
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
```

#### Service (k8s/service.yaml)
```yaml
apiVersion: v1
kind: Service
metadata:
  name: api-gateway-service
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
  selector:
    app: api-gateway
```

### Step 3: Deploy to Kubernetes

```bash
# Create namespace
kubectl create namespace api-gateway

# Apply configurations
kubectl apply -f k8s/ -n api-gateway

# Check status
kubectl get pods -n api-gateway
kubectl get services -n api-gateway

# View logs
kubectl logs -f deployment/api-gateway -n api-gateway
```

## Monitoring Setup

### Prometheus Configuration

1. Prometheus is already configured in `docker-compose.yml`
2. For Kubernetes, add ServiceMonitor:

```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: api-gateway-metrics
spec:
  selector:
    matchLabels:
      app: api-gateway
  endpoints:
  - port: http
    path: /actuator/prometheus
    interval: 30s
```

### Grafana Dashboards

1. Access Grafana: http://localhost:3000 (admin/admin)
2. Add Prometheus data source:
   - URL: http://prometheus:9090
3. Import Spring Boot dashboard (ID: 12900)
4. Create custom dashboard with key metrics:
   - Request rate per partner
   - Response times (P95, P99)
   - Error rates
   - Database connection pool status
   - Cache hit rates

### Key Metrics to Monitor

```promql
# Request rate
rate(http_server_requests_seconds_count[5m])

# Error rate
rate(http_server_requests_seconds_count{status=~"5.."}[5m])

# Response time P95
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# Database connections
hikaricp_connections_active

# Cache hit rate
cache_gets{result="hit"} / cache_gets
```

## Troubleshooting

### Common Issues

#### 1. Application Won't Start

**Problem**: Port 8080 already in use
```bash
# Find process using port 8080
lsof -ti:8080

# Kill the process
kill -9 <PID>
```

**Problem**: Database connection failed
```bash
# Check database is running
pg_isready -h localhost -p 5432

# Test connection
psql -h localhost -U api_user -d api_gateway

# Check Flyway migrations
SELECT * FROM flyway_schema_history;
```

#### 2. Authentication Issues

**Problem**: JWT token expired
- Tokens expire after 1 hour
- Use refresh token endpoint to get new token
- Check JWT_SECRET is same across all instances

**Problem**: Invalid credentials
- Verify API key and secret
- Check partner status is ACTIVE
- Review audit logs for failed login attempts

#### 3. Performance Issues

**Problem**: Slow response times
```bash
# Check database query performance
SELECT * FROM audit_logs WHERE duration_ms > 1000 ORDER BY timestamp DESC LIMIT 100;

# Check database connections
SELECT count(*) FROM pg_stat_activity WHERE datname = 'api_gateway';

# Check cache hit rate
# Access: http://localhost:8080/actuator/metrics/cache.gets
```

**Problem**: High memory usage
```bash
# Check JVM heap usage
jmap -heap <PID>

# Generate heap dump
jmap -dump:live,format=b,file=heap.bin <PID>

# Analyze with VisualVM or Eclipse MAT
```

#### 4. Database Migration Issues

**Problem**: Flyway migration failed
```bash
# Repair Flyway
mvn flyway:repair

# Or manually
psql -h localhost -U api_user -d api_gateway
DELETE FROM flyway_schema_history WHERE success = false;
```

### Log Analysis

```bash
# View real-time logs
tail -f logs/api-gateway.log

# Search for errors
grep -i error logs/api-gateway.log

# Count errors by type
grep ERROR logs/api-gateway.log | awk '{print $NF}' | sort | uniq -c

# Find slow requests
grep "duration_ms" logs/api-gateway.log | awk '$NF > 1000' | wc -l
```

### Health Checks

```bash
# Application health
curl http://localhost:8080/actuator/health | jq

# Database health
curl http://localhost:8080/health | jq '.data.database'

# Cache health
curl http://localhost:8080/health | jq '.data.cache'

# Metrics
curl http://localhost:8080/actuator/metrics | jq
```

## Performance Tuning

### JVM Optimization

```bash
# Production JVM options
java -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+UseContainerSupport \
     -XX:MaxRAMPercentage=75.0 \
     -XX:InitialRAMPercentage=50.0 \
     -Xlog:gc*:file=gc.log:time,uptime:filecount=5,filesize=10m \
     -jar api-gateway-1.0.0.jar
```

### Database Tuning

```sql
-- Increase connection pool
ALTER SYSTEM SET max_connections = 200;

-- Optimize shared buffers
ALTER SYSTEM SET shared_buffers = '256MB';

-- Enable query statistics
ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';
```

### Redis Tuning

```bash
# redis.conf
maxmemory 512mb
maxmemory-policy allkeys-lru
timeout 300
```

## Security Checklist

- [ ] Change default JWT_SECRET
- [ ] Remove or change default partner credentials
- [ ] Enable HTTPS/TLS
- [ ] Configure firewall rules
- [ ] Set up IP whitelisting for admin endpoints
- [ ] Enable rate limiting
- [ ] Configure CORS appropriately
- [ ] Regular security updates
- [ ] Database encryption at rest
- [ ] Rotate credentials regularly
- [ ] Monitor audit logs for suspicious activity

## Backup & Recovery

### Database Backup

```bash
# Create backup
pg_dump -h localhost -U api_user api_gateway > backup_$(date +%Y%m%d).sql

# Automated daily backups (crontab)
0 2 * * * pg_dump -h localhost -U api_user api_gateway > /backups/api_gateway_$(date +\%Y\%m\%d).sql
```

### Restore from Backup

```bash
# Drop and recreate database
dropdb -h localhost -U postgres api_gateway
createdb -h localhost -U postgres api_gateway

# Restore
psql -h localhost -U api_user api_gateway < backup_20241115.sql
```

## Support

For issues and questions:
- Documentation: Check README.md and this guide
- Logs: Review application logs
- Metrics: Check Grafana dashboards
- Support: support@enterprise.com
