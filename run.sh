#!/bin/bash

# API Gateway Build and Run Script

set -e

echo "================================"
echo "API Gateway - Build & Run"
echo "================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    print_info "Checking prerequisites..."
    
    if ! command -v java &> /dev/null; then
        echo "Error: Java is not installed"
        exit 1
    fi
    
    if ! command -v mvn &> /dev/null; then
        echo "Error: Maven is not installed"
        exit 1
    fi
    
    print_info "Prerequisites check passed"
}

# Clean and build
build() {
    print_info "Cleaning and building the application..."
    mvn clean package -DskipTests
    print_info "Build completed successfully"
}

# Run tests
test() {
    print_info "Running tests..."
    mvn test
    print_info "Tests completed"
}

# Run with dev profile
run_dev() {
    print_info "Starting application with dev profile (H2 database)..."
    print_info "Access points:"
    print_info "  - API: http://localhost:8080"
    print_info "  - Swagger UI: http://localhost:8080/swagger-ui.html"
    print_info "  - H2 Console: http://localhost:8080/h2-console"
    echo ""
    
    mvn spring-boot:run -Dspring-boot.run.profiles=dev
}

# Run with production profile
run_prod() {
    print_warn "Starting with production profile requires PostgreSQL and Redis"
    print_info "Make sure to set environment variables:"
    print_info "  - DATABASE_URL"
    print_info "  - DATABASE_USERNAME"
    print_info "  - DATABASE_PASSWORD"
    print_info "  - REDIS_HOST"
    print_info "  - JWT_SECRET"
    echo ""
    
    java -jar target/api-gateway-1.0.0.jar --spring.profiles.active=prod
}

# Docker build
docker_build() {
    print_info "Building Docker image..."
    docker build -t api-gateway:latest .
    print_info "Docker image built successfully"
}

# Docker compose up
docker_up() {
    print_info "Starting services with Docker Compose..."
    docker-compose up -d
    print_info "Services started. Access points:"
    print_info "  - API Gateway: http://localhost:8080"
    print_info "  - Swagger UI: http://localhost:8080/swagger-ui.html"
    print_info "  - Prometheus: http://localhost:9090"
    print_info "  - Grafana: http://localhost:3000 (admin/admin)"
}

# Docker compose down
docker_down() {
    print_info "Stopping services..."
    docker-compose down
    print_info "Services stopped"
}

# Show usage
usage() {
    echo "Usage: $0 {build|test|run-dev|run-prod|docker-build|docker-up|docker-down}"
    echo ""
    echo "Commands:"
    echo "  build         - Clean and build the application"
    echo "  test          - Run tests"
    echo "  run-dev       - Run with dev profile (H2 database)"
    echo "  run-prod      - Run with production profile"
    echo "  docker-build  - Build Docker image"
    echo "  docker-up     - Start all services with Docker Compose"
    echo "  docker-down   - Stop all services"
    exit 1
}

# Main
case "$1" in
    build)
        check_prerequisites
        build
        ;;
    test)
        check_prerequisites
        test
        ;;
    run-dev)
        check_prerequisites
        build
        run_dev
        ;;
    run-prod)
        check_prerequisites
        build
        run_prod
        ;;
    docker-build)
        docker_build
        ;;
    docker-up)
        docker_up
        ;;
    docker-down)
        docker_down
        ;;
    *)
        usage
        ;;
esac
