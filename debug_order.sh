#!/bin/bash

echo "=== LensCart Order Service Debug Script ==="
echo "Testing order placement functionality..."
echo

# Check if services are running
echo "1. Checking if services are running..."
echo "Eureka Server (8761):"
curl -s http://localhost:8761/eureka/apps | grep -o "FINALORDERUPDATED\|CART-SERVICE\|PRODUCT-SERVICE" || echo "Services not registered"

echo
echo "Cart Service (8087):"
curl -s -o /dev/null -w "%{http_code}" http://localhost:8087/actuator/health || echo "Cart service not responding"

echo
echo "Product Service (8089):"
curl -s -o /dev/null -w "%{http_code}" http://localhost:8089/actuator/health || echo "Product service not responding"

echo
echo "Order Service (8088):"
curl -s -o /dev/null -w "%{http_code}" http://localhost:8088/actuator/health || echo "Order service not responding"

echo
echo "2. Testing order placement..."
echo "Sample order request:"

# Test order placement
curl -X POST "http://localhost:8088/order/place/1?address=Test Address" \
  -H "Content-Type: application/json" \
  -v

echo
echo "=== Debug Complete ==="
echo "If you see errors, check:"
echo "1. All services are running (docker-compose up)"
echo "2. Database is accessible"
echo "3. Cart has items for the customer"
echo "4. Products have sufficient stock"