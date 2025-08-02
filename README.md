# LensCart Backend Microservices

A complete microservices-based e-commerce platform for eyewear products built with Spring Boot and Docker.

## 🚀 Quick Setup

### Prerequisites
- Docker Desktop
- Java 17
- Maven

### 1. Clone Repository
```bash
git clone <repository-url>
cd LensCart_Backend_Dockers-master
```

### 2. Skip Tests (Default)
Tests are automatically skipped during build. To run tests:
```bash
# Run tests for specific service
cd <service-folder>
./mvnw test -DskipTests=false
```

### 4. Configure Environment
```bash
# Copy and edit environment file
cp .env.example .env
# Edit .env file with your secure passwords
```

### 5. Start All Services
```bash
docker-compose -f docker-compose-lenses.yml up -d
```

### 3. Access Services
- **Eureka Dashboard**: http://localhost:8761
- **MySQL Database**: localhost:3307

## 📋 API Services

| Service | Port | Swagger UI |
|---------|------|------------|
| Lenses | 8082 | http://localhost:8082/swagger-ui.html |
| Frames | 8083 | http://localhost:8083/swagger-ui.html |
| Glass | 8084 | http://localhost:8084/swagger-ui.html |
| Customer Admin | 8085 | http://localhost:8085/swagger-ui.html |
| Sunglasses | 8086 | http://localhost:8086/swagger-ui.html |
| Cart | 8087 | http://localhost:8087/swagger-ui.html |
| Orders | 8088 | http://localhost:8088/swagger-ui.html |
| Product | 8089 | http://localhost:8089/swagger-ui.html |

## 🛠️ Manual Build (if needed)

### Build Individual Service
```bash
cd <service-folder>
./mvnw clean package -DskipTests
docker build -t <service-name> .
```

### Build All Services
```bash
# Lenses
cd lenses && ./mvnw clean package -DskipTests && docker build -t lenses-service . && cd ..

# Frames  
cd frames && ./mvnw clean package -DskipTests && docker build -t frames-service . && cd ..

# Glass
cd Glass && ./mvnw clean package -DskipTests && docker build -t glass-service . && cd ..

# Customer Admin
cd customer-admin-service && ./mvnw clean package -DskipTests && docker build -t customer-admin-service . && cd ..

# Sunglasses
cd sunglassmodule && ./mvnw clean package -DskipTests && docker build -t sunglasses-service . && cd ..

# Cart
cd cart && ./mvnw clean package -DskipTests && docker build -t cart-service . && cd ..

# Orders
cd finalorderUpdated && ./mvnw clean package -DskipTests && docker build -t orders-service . && cd ..

# Product
cd product-sevice && ./mvnw clean package -DskipTests && docker build -t product-service . && cd ..
```

## 📝 Sample API Calls

### Add Lens
```bash
POST http://localhost:8082/add-lense
{
  "brand": "Ray-Ban",
  "lensImage": "https://example.com/lens.jpg",
  "shape": "Round",
  "colour": "Blue",
  "price": 150.0,
  "quantity": 10,
  "name": "Blue Round Lens"
}
```

### Add Frame
```bash
POST http://localhost:8083/add-frame
{
  "frameName": "Black Frame",
  "brand": "Ray-Ban",
  "color": "Black",
  "price": 200.0,
  "imageUrl": "https://example.com/frame.jpg",
  "shape": "Rectangular",
  "quantity": 15
}
```

## 🔧 Troubleshooting

### Services Not Starting?
```bash
# Check logs
docker logs <service-name>

# Restart specific service
docker-compose -f docker-compose-lenses.yml restart <service-name>
```

### Port Conflicts?
- Stop other applications using ports 8082-8089, 8761, 3307
- Or modify ports in docker-compose-lenses.yml

### Database Issues?
```bash
# Reset database
docker-compose -f docker-compose-lenses.yml down
docker volume prune
docker-compose -f docker-compose-lenses.yml up -d
```

## 🏗️ Architecture

- **Service Discovery**: Eureka Server (8761)
- **Database**: MySQL 8.0 (3307)
- **API Gateway**: Individual service endpoints
- **Orders**: Order management service
- **Documentation**: Swagger UI for each service

## 🎯 Features

- Complete microservices architecture
- Docker containerization
- Service discovery and registration
- MySQL database with auto-creation
- Swagger API documentation
- Order management system
- Load balancing ready

## 📚 Tech Stack

- **Backend**: Spring Boot 3.x, Spring Cloud
- **Database**: MySQL 8.0
- **Service Discovery**: Netflix Eureka
- **Containerization**: Docker & Docker Compose
- **Documentation**: OpenAPI 3.0 (Swagger)
- **Orders**: Order Processing
- **Build Tool**: Maven