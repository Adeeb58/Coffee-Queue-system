#!/bin/bash

# Automated Test Suite for Barista Queue System
# This script runs a complete test scenario with 100 orders

echo "========================================"
echo "Barista Queue System - Automated Test"
echo "========================================"
echo ""

# Configuration
API_URL="http://localhost:8080/api"
BACKEND_RUNNING=false
FRONTEND_RUNNING=false

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if backend is running
check_backend() {
    echo "Checking backend connection..."
    if curl -s "${API_URL}/drinks" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Backend is running${NC}"
        BACKEND_RUNNING=true
        return 0
    else
        echo -e "${RED}✗ Backend is not running${NC}"
        echo "Please start the backend first: mvn spring-boot:run"
        return 1
    fi
}

# Generate test orders
generate_orders() {
    echo ""
    echo "Generating 100 test orders..."
    response=$(curl -s -X POST "${API_URL}/test/generate")
    echo -e "${GREEN}${response}${NC}"
    sleep 2
}

# Get initial metrics
get_metrics() {
    echo ""
    echo "Fetching test metrics..."
    metrics=$(curl -s "${API_URL}/test/metrics")
    
    # Parse JSON (requires jq)
    if command -v jq &> /dev/null; then
        total=$(echo "$metrics" | jq -r '.totalOrders')
        pending=$(echo "$metrics" | jq -r '.pendingOrders')
        completed=$(echo "$metrics" | jq -r '.completedOrders')
        avgWait=$(echo "$metrics" | jq -r '.avgWaitTime')
        
        echo ""
        echo "=== Current Metrics ==="
        echo "Total Orders: $total"
        echo "Pending: $pending"
        echo "Completed: $completed"
        echo "Avg Wait Time: $avgWait min"
        echo "======================="
    else
        echo "$metrics" | python -m json.tool
    fi
}

# Auto-assign orders to baristas
auto_assign_orders() {
    echo ""
    echo "Auto-assigning orders to baristas..."
    
    # Get barista IDs (assuming 1, 2, 3)
    for i in {1..30}; do
        barista_id=$((($i % 3) + 1))
        echo "Assigning order to Barista $barista_id..."
        curl -s -X POST "${API_URL}/baristas/${barista_id}/assign-next" > /dev/null
        sleep 1
    done
    
    echo -e "${GREEN}✓ 30 orders assigned${NC}"
}

# Simulate order completion
simulate_completion() {
    echo ""
    echo "Simulating order completion..."
    
    # Get pending orders
    orders=$(curl -s "${API_URL}/orders/pending")
    
    # Complete first 10 orders (requires jq)
    if command -v jq &> /dev/null; then
        order_ids=$(echo "$orders" | jq -r '.[0:10][].id')
        
        for order_id in $order_ids; do
            echo "Completing order $order_id..."
            curl -s -X POST "${API_URL}/orders/${order_id}/complete" > /dev/null
            sleep 0.5
        done
        
        echo -e "${GREEN}✓ 10 orders completed${NC}"
    else
        echo "Install jq for automatic completion: sudo apt-get install jq"
    fi
}

# Generate performance report
generate_report() {
    echo ""
    echo "========================================="
    echo "FINAL TEST REPORT"
    echo "========================================="
    
    metrics=$(curl -s "${API_URL}/test/metrics")
    
    if command -v jq &> /dev/null; then
        echo "$metrics" | jq -r '
            "Total Orders: \(.totalOrders)",
            "Pending: \(.pendingOrders)",
            "In Progress: \(.inProgressOrders)",
            "Completed: \(.completedOrders)",
            "",
            "Performance Metrics:",
            "  Avg Wait Time: \(.avgWaitTime) min",
            "  Max Wait Time: \(.maxWaitTime) min",
            "  Min Wait Time: \(.minWaitTime) min",
            "  Timeout Rate: \(.timeoutRate)%",
            "",
            "Barista Performance:",
            (.baristaMetrics | to_entries[] | 
            "  \(.key):",
            "    Total Served: \(.value.totalOrdersServed)",
            "    Current Workload: \(.value.currentWorkload) min",
            "    Completed: \(.value.completedCount)"
            )
        '
    else
        echo "$metrics" | python -m json.tool
    fi
    
    echo "========================================="
}

# Clear test data
clear_data() {
    echo ""
    read -p "Do you want to clear test data? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Clearing test data..."
        curl -s -X DELETE "${API_URL}/test/clear"
        echo -e "${GREEN}✓ Test data cleared${NC}"
    fi
}

# Main execution
main() {
    # Check backend
    if ! check_backend; then
        exit 1
    fi
    
    # Run test sequence
    generate_orders
    sleep 2
    
    get_metrics
    sleep 2
    
    auto_assign_orders
    sleep 2
    
    get_metrics
    sleep 2
    
    simulate_completion
    sleep 2
    
    generate_report
    
    clear_data
    
    echo ""
    echo -e "${GREEN}Test completed! View detailed metrics at:${NC}"
    echo "http://localhost:3000 (Simulation tab)"
}

# Run main function
main