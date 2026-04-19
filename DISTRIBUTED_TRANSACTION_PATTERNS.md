# Distributed Transaction Patterns in Microservices

A comprehensive guide to handling distributed transactions across microservices, comparing **2 Phase Commit**, **Saga Orchestration**, and **Saga Choreography** patterns using the order-service, inventory-service, and notification-service architecture.

---

## Table of Contents

1. [The Problem: Distributed Transactions](#the-problem-distributed-transactions)
2. [Pattern 1: 2 Phase Commit (2PC)](#pattern-1-2-phase-commit-2pc)
3. [Pattern 2: Saga Orchestration](#pattern-2-saga-orchestration)
4. [Pattern 3: Saga Choreography](#pattern-3-saga-choreography)
5. [Comparison Matrix](#comparison-matrix)
6. [When to Use Which Pattern](#when-to-use-which-pattern)

---

## The Problem: Distributed Transactions

In a microservices architecture, a single business operation often spans multiple services, each with its own database:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         USER REQUEST                                     │
│                    "Place Order for LAPTOP-001"                          │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                                                                          │
│   ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐   │
│   │  Order Service  │    │ Inventory Svc   │    │ Notification    │   │
│   │                 │    │                 │    │ Service         │   │
│   │  PostgreSQL     │    │  PostgreSQL    │    │                 │   │
│   │  (Orders DB)    │    │  (Inventory DB)│    │  (Email)        │   │
│   └─────────────────┘    └─────────────────┘    └─────────────────┘   │
│                                                                          │
│   Task:                  Task:                   Task:                   │
│   1. Create order       1. Check stock         1. Send email            │
│   2. Set status         2. Reserve stock       2. Log notification      │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

**The Challenge:** How do we ensure all three tasks succeed together, or all fail together, when each service manages its own database independently?

---

## Pattern 1: 2 Phase Commit (2PC)

### Overview

2 Phase Commit is a **synchronous protocol** that ensures **atomicity** across distributed databases. It uses a central **Coordinator** to manage the commit/rollback process.

### How It Works

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     2 PHASE COMMIT PROTOCOL                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   COORDINATOR                                                            │
│   (Order Service)                                                        │
│        │                                                                 │
│        │              PHASE 1: PREPARE                                   │
│        ├─────────────────────────────────────────────────────────────► │
│        │                      │                    │                    │
│        │                      ▼                    ▼                    │
│        │              ┌─────────────┐      ┌─────────────┐             │
│        │              │ Inventory   │      │ Order       │             │
│        │              │ Database    │      │ Database    │             │
│        │              └─────────────┘      └─────────────┘             │
│        │                      │                    │                    │
│        │              Ask: "Can you              Ask: "Can you           │
│        │                   prepare?"                 prepare?"          │
│        │                      │                    │                    │
│        │◄─────────────────────┼────────────────────┘                    │
│        │              (Vote: YES/NO)                                    │
│        │                                                                      │
│        ▼              PHASE 2: DECISION                                  │
│   ┌─────────────────────────────────────────────────────────────┐        │
│   │                                                              │        │
│   │   ALL VOTED YES?  ──►  COMMIT                               │        │
│   │   ANY VOTED NO?    ──►  ROLLBACK                            │        │
│   │                                                              │        │
│   └─────────────────────────────────────────────────────────────┘        │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Step-by-Step Execution

#### Scenario: User orders 2 units of Product "LAPTOP-001"

```
┌────────────────────────────────────────────────────────────────────────┐
│                     PHASE 1: PREPARE (Voting Phase)                    │
│                                                                        │
│  Order Service (Coordinator)                                           │
│       │                                                                │
│       │                                                                │
│       ├──► Ask Inventory DB: "Can I reserve 2 LAPTOP-001?"            │
│       │                                                                │
│       │   ┌─────────────────────────────────────────────┐             │
│       │   │ Inventory Service Response: YES             │             │
│       │   │                                             │             │
│       │   │ 1. BEGIN TRANSACTION                        │             │
│       │   │ 2. SELECT * FROM inventory                 │             │
│       │   │    WHERE sku = 'LAPTOP-001' FOR UPDATE     │             │
│       │   │ 3. Found 10 units in stock                  │             │
│       │   │ 4. PREPARE TRANSACTION                      │             │
│       │   │    (LOCKS the row - waiting for commit)    │             │
│       │   │ 5. Response: "READY"                        │             │
│       │   └─────────────────────────────────────────────┘             │
│       │                                                                │
│       │                                                                │
│       ├──► Ask Order DB: "Can I insert this order?"                  │
│       │                                                                │
│       │   ┌─────────────────────────────────────────────┐             │
│       │   │ Order Database Response: YES                │             │
│       │   │                                             │             │
│       │   │ 1. BEGIN TRANSACTION                        │             │
│       │   │ 2. PREPARE to INSERT order                  │             │
│       │   │ 3. Response: "READY"                        │             │
│       │   └─────────────────────────────────────────────┘             │
│       │                                                                │
│       │                                                                │
│       └──► All voted YES ──► PROCEED TO PHASE 2                        │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│                     PHASE 2: COMMIT (Decision Phase)                   │
│                                                                        │
│  Order Service (Coordinator)                                           │
│       │                                                                │
│       │                                                                │
│       ├──► Tell Inventory DB: "COMMIT"                                 │
│       │        └──► Inventory DB: Deducts 2 units (10 → 8)            │
│       │        └──► Inventory DB: Releases locks                       │
│       │                                                                │
│       ├──► Tell Order DB: "COMMIT"                                    │
│       │        └──► Order DB: Inserts order record                    │
│       │        └──► Order DB: Releases locks                          │
│       │                                                                │
│       │                                                                │
│       └──► TRANSACTION COMPLETE ✅                                     │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘
```

### Code Example (Hypothetical 2PC Implementation)

```java
// OrderService.java - Acts as COORDINATOR
@Service
public class OrderService2PC {
    
    @Autowired private DataSource orderDataSource;
    @Autowired private RestTemplate inventoryClient;  // REST call to Inventory Service
    
    public void placeOrder(OrderRequest request) {
        
        try {
            // ===== PHASE 1: PREPARE (Voting Phase) =====
            
            // 1. Ask Inventory Service to prepare
            boolean inventoryPrepared = askInventoryToPrepare(request);
            
            // 2. Ask Order Database to prepare
            boolean orderPrepared = askOrderDatabaseToPrepare(request);
            
            // 3. Check if ALL participants voted YES
            if (inventoryPrepared && orderPrepared) {
                
                // ===== PHASE 2: COMMIT =====
                commitInventoryReservation(request);
                commitOrderCreation(request);
                
                return new OrderResponse("SUCCESS", orderId);
                
            } else {
                // ===== PHASE 2: ROLLBACK =====
                rollbackAll(request);
                throw new OrderException("Transaction failed - insufficient inventory");
            }
            
        } catch (Exception e) {
            rollbackAll(request);
            throw new OrderException("Transaction failed: " + e.getMessage());
        }
    }
    
    private boolean askInventoryToPrepare(OrderRequest request) {
        // REST call to Inventory Service
        // Inventory Service must:
        //   1. BEGIN TRANSACTION
        //   2. SELECT FOR UPDATE (lock the row)
        //   3. PREPARE TRANSACTION (don't commit)
        //   4. Return "READY" or "ABORT"
        
        // ⚠️ PROBLEM: The row is LOCKED until we commit!
        // Other requests for LAPTOP-001 will BLOCK here
        
        try {
            InventoryResponse response = inventoryClient.postForEntity(
                "/inventory/prepare",
                request,
                InventoryResponse.class
            );
            return response.getStatus() == PREPARED;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean askOrderDatabaseToPrepare(OrderRequest request) {
        Connection conn = null;
        try {
            conn = orderDataSource.getConnection();
            conn.setAutoCommit(false);
            
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO orders (sku, quantity, status) VALUES (?, ?, 'PENDING')"
            );
            ps.setString(1, request.getSku());
            ps.setInt(2, request.getQuantity());
            ps.executeUpdate();
            
            // PREPARE the transaction (don't commit yet)
            conn.prepareStatement("PREPARE TRANSACTION 'order_" + orderId + "'");
            
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            // DON'T CLOSE - we need to keep the prepared state
        }
    }
}
```

```java
// InventoryService.java - Acts as PARTICIPANT
@Service
public class InventoryService2PC {
    
    @Transactional
    public boolean prepareReserveStock(String sku, int quantity) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            
            // ⚠️ CRITICAL: Lock the row - ALL other requests WAIT here
            PreparedStatement ps = conn.prepareStatement(
                "SELECT quantity FROM inventory WHERE sku = ? FOR UPDATE"
            );
            ps.setString(1, sku);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                int available = rs.getInt("quantity");
                
                if (available >= quantity) {
                    // Store the intended update (but don't execute yet)
                    pendingUpdates.put(sku, new PendingUpdate(available, quantity));
                    
                    // PREPARE - this transaction is now "waiting"
                    // It holds the lock but doesn't commit
                    conn.prepareStatement("PREPARE TRANSACTION 'inventory_" + sku + "'");
                    
                    return true;  // Vote: YES
                }
            }
            
            conn.rollback();
            return false;  // Vote: NO
            
        } catch (SQLException e) {
            return false;
        } finally {
            // ⚠️ DO NOT CLOSE CONNECTION
            // The prepared transaction holds locks until commit/rollback
        }
    }
    
    @Transactional
    public void commitReserve(String sku) {
        // Called by coordinator - execute the pending update
        Connection conn = getConnectionForPreparedTransaction(sku);
        conn.commit();
    }
    
    @Transactional
    public void rollbackReserve(String sku) {
        // Called by coordinator - undo everything
        Connection conn = getConnectionForPreparedTransaction(sku);
        conn.rollback();
    }
}
```

### Problems with 2 Phase Commit

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         2PC PITFALLS                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. BLOCKING - The Critical Problem                                     │
│     ═══════════════════════════════════════                             │
│                                                                         │
│     Timeline (User A orders LAPTOP-001, takes 3 seconds)                │
│                                                                         │
│     Time:  0s    1s    2s    3s    4s    5s    6s                     │
│            │     │     │     │     │     │     │                       │
│     User A ──►─────────PREPARE─────────COMMIT──✅                       │
│                            │                    │                       │
│     User B ───────────────►│ (WAITING)─────────│                       │
│                             │                   │                       │
│     User C ─────────────────────────────────────►│ (WAITING)────────── │
│                                                       │                 │
│     Result: User B and C see "loading..." for 3+ seconds 😡            │
│             All waiting for User A's transaction to complete            │
│                                                                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  2. COORDINATOR FAILURE = DISASTER                                      │
│     ════════════════════════════════════════                             │
│                                                                         │
│          Order Service          Inventory DB         Order DB           │
│          (Coordinator)                                │                 │
│               │                    │                   │                │
│               │──── PREPARE ──────►│                   │                │
│               │──── PREPARE ─────────────────────────►│                │
│               │◄──── READY ───────│                   │                │
│               │◄──── READY ───────────────────────────│                │
│               │                    │                   │                │
│               │  💥 CRASHES        │                   │                │
│               │  before COMMIT     │                   │                │
│               │                    │                   │                │
│               │    ☠️ ROWS LOCKED FOREVER!            │                │
│               │    ☠️ MANUAL RECOVERY REQUIRED         │                │
│               │                    │                   │                │
│                                                                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  3. TECHNOLOGY LIMITATIONS                                              │
│     ════════════════════════════════                                     │
│                                                                         │
│     Your Services Use:                                                   │
│     ┌──────────────┬──────────────────────────────────────┐            │
│     │ Order DB     │ PostgreSQL ✓ (supports XA)          │            │
│     │ Inventory DB │ PostgreSQL ✓ (supports XA)          │            │
│     │ Product DB   │ MongoDB ✗ (NO XA support!)           │            │
│     └──────────────┴──────────────────────────────────────┘            │
│                                                                         │
│     Problem: 2PC requires ALL databases to be XA-compliant            │
│              MongoDB doesn't support 2PC natively                      │
│                                                                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  4. NO HORIZONTAL SCALING                                               │
│     ════════════════════════════════                                     │
│                                                                         │
│     Can't run multiple Order Service instances!                         │
│                                                                         │
│     ┌─────────────────────────────────────────────────────┐            │
│     │                                                      │            │
│     │  Order Svc-1 (Coordinator)  ←── This one is leader   │            │
│     │  Order Svc-2 (Coordinator)  ←── Which one knows the  │            │
│     │  Order Svc-3 (Coordinator)      prepare state?       │            │
│     │                                                      │            │
│     │  ☠️ Split brain scenario if one crashes mid-transaction          │
│     │                                                      │            │
│     └─────────────────────────────────────────────────────┘            │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### When to Use 2PC?

- ✅ All services share the **same database instance**
- ✅ You need **strong ACID consistency**
- ✅ You accept blocking and latency
- ✅ Examples: Banking systems, stock trading, airline reservations

---

## Pattern 2: Saga Orchestration

### Overview

In Saga Orchestration, a **central Orchestrator** commands each step of the saga. The orchestrator knows the business process and tells each service what to do.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      SAGA ORCHESTRATION PATTERN                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│                        ┌─────────────────────┐                           │
│                        │   SAGA ORCHESTRATOR  │                           │
│                        │   (Order Service)    │                           │
│                        └──────────┬──────────┘                           │
│                                   │                                      │
│           ┌───────────────────────┼───────────────────────┐             │
│           │                       │                       │             │
│           ▼                       ▼                       ▼             │
│   ┌───────────────┐       ┌───────────────┐       ┌───────────────┐    │
│   │   Inventory   │       │   Payment     │       │ Notification  │    │
│   │   Service     │       │   Service     │       │ Service       │    │
│   └───────────────┘       └───────────────┘       └───────────────┘    │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Flow Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                     SAGA ORCHESTRATION FLOW                              │
│                                                                        │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                     SAGA ORCHESTRATOR                           │   │
│  │                    (Order Service)                              │   │
│  │                                                                  │   │
│  │  Step 1: Create order (PENDING)                                 │   │
│  │                        │                                        │   │
│  └────────────────────────┼────────────────────────────────────────┘   │
│                           │                                           │
│           ┌───────────────┼───────────────┐                          │
│           ▼               ▼               ▼                          │
│    ┌───────────┐   ┌───────────┐   ┌───────────┐                      │
│    │Inventory  │   │Payment   │   │Notifica- │                      │
│    │Service    │   │Service   │   │tion       │                      │
│    └─────┬─────┘   └─────┬─────┘   └─────┬─────┘                      │
│          │               │               │                             │
│          │◄─reserveStock─│               │                             │
│          │──success────►│               │                             │
│          │               │◄─chargePayment─│                           │
│          │               │──paymentOK──►│                             │
│          │               │               │◄─sendEmail────               │
│          │               │               │                             │
│          │               │               │                             │
│          │               │               │                             │
│          ▼               ▼               ▼                             │
│    ┌─────────────────────────────────────────────────────────────┐    │
│    │  Saga Complete! All steps succeeded.                       │    │
│    └─────────────────────────────────────────────────────────────┘    │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘
```

### Code Example

```java
// SagaOrchestrator.java - The Brain
@Component
public class OrderSagaOrchestrator {
    
    @Autowired private OrderService orderService;
    @Autowired private InventoryClient inventoryClient;
    @Autowired private PaymentClient paymentClient;
    @Autowired private NotificationClient notificationClient;
    
    public SagaResult executePlaceOrderSaga(OrderRequest request) {
        
        // ===== STEP 1: Create Order =====
        String orderId = orderService.createOrder(request);
        
        try {
            // ===== STEP 2: Reserve Inventory =====
            InventoryResult inventoryResult = inventoryClient.reserveStock(
                request.getSku(), 
                request.getQuantity()
            );
            
            if (!inventoryResult.isSuccess()) {
                // COMPENSATING ACTION: Cancel order
                orderService.cancelOrder(orderId, "INVENTORY_UNAVAILABLE");
                return SagaResult.failed("Inventory unavailable");
            }
            
            // ===== STEP 3: Process Payment =====
            PaymentResult paymentResult = paymentClient.chargePayment(
                request.getPaymentDetails(),
                request.getTotalAmount()
            );
            
            if (!paymentResult.isSuccess()) {
                // COMPENSATING ACTION: Release inventory
                inventoryClient.releaseStock(request.getSku(), request.getQuantity());
                // COMPENSATING ACTION: Cancel order
                orderService.cancelOrder(orderId, "PAYMENT_FAILED");
                return SagaResult.failed("Payment failed");
            }
            
            // ===== STEP 4: Send Notification =====
            notificationClient.sendOrderConfirmation(
                request.getEmail(), 
                orderId
            );
            
            // ===== STEP 5: Confirm Order =====
            orderService.confirmOrder(orderId);
            
            return SagaResult.success(orderId);
            
        } catch (Exception e) {
            // COMPENSATING ACTION: Full rollback
            inventoryClient.releaseStock(request.getSku(), request.getQuantity());
            orderService.cancelOrder(orderId, "SYSTEM_ERROR");
            return SagaResult.failed(e.getMessage());
        }
    }
}
```

### Handling Failures with Compensating Actions

```
┌────────────────────────────────────────────────────────────────────────┐
│                SAGA ORCHESTRATION - FAILURE SCENARIO                    │
│                                                                        │
│  Scenario: Payment fails after inventory is reserved                   │
│                                                                        │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                                                                  │   │
│  │  STEP 1: Create Order          ✅ SUCCESS                        │   │
│  │                        │                                          │   │
│  │                        ▼                                          │   │
│  │  STEP 2: Reserve Inventory   ✅ SUCCESS                          │   │
│  │                        │                                          │   │
│  │                        ▼                                          │   │
│  │  STEP 3: Process Payment     ❌ FAILED (card declined)            │   │
│  │                        │                                          │   │
│  │                        ▼                                          │   │
│  │  ┌──────────────────────────────────────────────────────────┐   │   │
│  │  │              COMPENSATING ACTIONS                         │   │   │
│  │  │                                                           │   │   │
│  │  │  1. Release Inventory    ←── Undo step 2                  │   │   │
│  │  │  2. Cancel Order        ←── Undo step 1                   │   │   │
│  │  │                                                           │   │   │
│  │  └──────────────────────────────────────────────────────────┘   │   │
│  │                        │                                          │   │
│  │                        ▼                                          │   │
│  │  Saga FAILED - System is consistent ✅                           │   │
│  │                                                                  │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘
```

### Pros and Cons of Orchestration

| Pros | Cons |
|------|------|
| ✅ Clear business logic flow | ❌ Central point of failure |
| ✅ Easy to understand and debug | ❌ Orchestrator knows all steps (coupling) |
| ✅ Good for complex workflows | ❌ Risk of orchestrator becoming "god class" |
| ✅ Easier to add new steps | ❌ Requires careful compensation logic |
| ✅ Single place for saga state | |

---

## Pattern 3: Saga Choreography

### Overview

In Saga Choreography, **services publish events** and **react to events** from other services. There's no central orchestrator - each service knows its own responsibility.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      SAGA CHOREOGRAPHY PATTERN                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   ┌─────────────┐         ┌─────────────┐         ┌─────────────┐     │
│   │    Order    │         │  Inventory   │         │Notification│     │
│   │   Service   │         │   Service    │         │  Service    │     │
│   └──────┬──────┘         └──────┬──────┘         └──────┬──────┘     │
│          │                       │                        │            │
│          │ OrderPlaced           │                        │            │
│          │──────────────────────►│                        │            │
│          │                       │                        │            │
│          │                       │ InventoryReserved      │            │
│          │◄──────────────────────│                        │            │
│          │                       │                        │            │
│          │ OrderConfirmed        │                        │            │
│          │───────────────────────────────────────────────►│            │
│          │                       │                        │            │
└─────────────────────────────────────────────────────────────────────────┘
```

### Your Current Implementation (Real Example)

```
┌────────────────────────────────────────────────────────────────────────┐
│              CHOREOGRAPHY SAGA - SUCCESS SCENARIO                       │
│                                                                        │
│  User           OrderService       Kafka          InventorySvc        │
│    │                 │                │                 │             │
│    │──placeOrder()──►│                 │                 │             │
│    │                 │                 │                 │             │
│    │                 │──OrderPlaced───►│                 │             │
│    │                 │                 │                 │             │
│    │                 │                 │◄─OrderPlaced───│             │
│    │                 │                 │                 │             │
│    │                 │                 │──check stock────────────────│
│    │                 │                 │◄─reserved──────────────     │
│    │                 │                 │                 │             │
│    │                 │◄─InventoryReserved────────────────────────     │
│    │                 │                 │                 │             │
│    │                 │──OrderConfirmed►│                 │             │
│    │                 │                 │                 │             │
│    │                 │                 │◄─OrderConfirmed              │
│    │                 │                 │                 │             │
│    │                 │                 │                 │──sendEmail──│
│    │                 │                 │                 │             │
│    │◄──OrderID───────│                 │                 │             │
│    │                 │                 │                 │             │
└────────────────────────────────────────────────────────────────────────┘
```

### Code Implementation (Your Actual Code)

```java
// OrderService.java - PUBLISHER
@Service
public class OrderService {
    
    @Autowired private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    
    public OrderResponse placeOrder(OrderRequest request) {
        // 1. Create order in local DB
        Order order = Order.builder()
            .orderNumber(UUID.randomUUID().toString())
            .sku(request.getSku())
            .price(request.getPrice())
            .quantity(request.getQuantity())
            .orderStatus(OrderStatus.PENDING)
            .build();
        
        orderRepository.save(order);
        
        // 2. Publish event - NO direct call to InventoryService!
        OrderPlacedEvent event = OrderPlacedEvent.builder()
            .orderNumber(order.getOrderNumber())
            .sku(request.getSku())
            .quantity(request.getQuantity())
            .email(request.getEmail())
            .build();
        
        kafkaTemplate.send("order-placed", event);
        
        // 3. Return immediately (async processing)
        return new OrderResponse(order.getOrderNumber(), "PENDING");
    }
}
```

```java
// InventoryKafkaListener.java - REACTOR
@Component
public class InventoryKafkaListener {
    
    @KafkaListener(topics = "order-placed")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        // React to event - do the work
        boolean reserved = inventoryService.reserveStock(
            event.getSku(), 
            event.getQuantity()
        );
        
        if (reserved) {
            // Publish success event
            kafkaTemplate.send("inventory-reserved", 
                InventoryReservedEvent.builder()
                    .orderNumber(event.getOrderNumber())
                    .build()
            );
        } else {
            // Publish failure event
            kafkaTemplate.send("inventory-reservation-failed",
                InventoryReservationFailedEvent.builder()
                    .orderNumber(event.getOrderNumber())
                    .build()
            );
        }
    }
}
```

```java
// OrderKafkaListener.java - REACTS TO INVENTORY EVENTS
@Component
public class OrderKafkaListener {
    
    @KafkaListener(topics = "inventory-reserved")
    public void handleInventoryReserved(InventoryReservedEvent event) {
        // Update order status to CONFIRMED
        Order order = orderRepository.findByOrderNumber(event.getOrderNumber());
        order.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        
        // Publish confirmation event for notification service
        kafkaTemplate.send("order-confirmed",
            OrderConfirmedEvent.builder()
                .orderNumber(order.getOrderNumber())
                .email(order.getEmail())
                .build()
        );
    }
    
    @KafkaListener(topics = "inventory-reservation-failed")
    public void handleInventoryFailed(InventoryReservationFailedEvent event) {
        // Compensating action: Mark order as FAILED
        Order order = orderRepository.findByOrderNumber(event.getOrderNumber());
        order.setOrderStatus(OrderStatus.FAILED);
        orderRepository.save(order);
        
        // No further events published - saga ends here
    }
}
```

### Success Flow Visualization

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    CHOREOGRAPHY SAGA - HAPPY PATH                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Time →                                                                   │
│                                                                          │
│  T0: OrderService.createOrder() → status=PENDING                        │
│       │                                                                   │
│       ▼                                                                   │
│  T1: Publish OrderPlacedEvent ──────────────────────────────────────►   │
│                                                                          │
│  T2:                                              InventoryService        │
│       │                                           receives event         │
│       │                                           reserveStock()          │
│       │                                           ✅ Success              │
│       │                                                                   │
│  T3: ◄───────────────────────────────────── Publish InventoryReserved   │
│                                                                          │
│  T4: OrderService.updateStatus(CONFIRMED)                                │
│       │                                                                   │
│       ▼                                                                   │
│  T5: Publish OrderConfirmedEvent ────────────────────────────────►      │
│                                                                          │
│  T6:                                                   NotificationSvc   │
│       │                                            receives event        │
│       │                                            sendEmail()           │
│       │                                                                   │
│       │   ✅ COMPLETE - All services did their job                       │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Failure Flow Visualization

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    CHOREOGRAPHY SAGA - FAILURE PATH                     │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  T0: OrderService.createOrder() → status=PENDING                        │
│       │                                                                   │
│       ▼                                                                   │
│  T1: Publish OrderPlacedEvent ──────────────────────────────────────►   │
│                                                                          │
│  T2:                                              InventoryService        │
│       │                                           receives event         │
│       │                                           reserveStock()          │
│       │                                           ❌ FAILED (no stock)    │
│       │                                                                   │
│  T3: ◄───────────────────────────────────── Publish InventoryFailed      │
│                                                                          │
│  T4: OrderService.updateStatus(FAILED) ← COMPENSATING ACTION            │
│       │                                                                   │
│       ▼                                                                   │
│  T5: NO OrderConfirmedEvent published                                   │
│       │                                                                   │
│       ▼                                                                   │
│  T6:                                             NotificationService     │
│                                                    receives NOTHING      │
│       │                                                                   │
│       │   ✅ CONSISTENT - No email sent for failed order                 │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Pros and Cons of Choreography

| Pros | Cons |
|------|------|
| ✅ Loose coupling (services don't know each other) | ❌ Harder to trace overall flow |
| ✅ No single point of failure | ❌ Events scattered across services |
| ✅ Each service is simple and focused | ❌ Risk of cyclic dependencies |
| ✅ Scales well | ❌ Harder to debug distributed issues |
| ✅ Works with different technologies | ❌ Duplicated event handling logic |

---

## Comparison Matrix

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         PATTERN COMPARISON                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Aspect              │ 2 Phase Commit │ Saga Orch │ Saga Choreography       │
│  ────────────────────┼────────────────┼───────────┼─────────────────       │
│                                                                             │
│  Consistency Model   │ Strong (ACID)  │ Eventual  │ Eventual               │
│  Blocking            │ YES ⚠️         │ NO        │ NO                     │
│  Latency             │ HIGH           │ LOW       │ LOW                    │
│  Coupling            │ TIGHT          │ MODERATE  │ LOOSE                  │
│  Single Point of     │               │           │                         │
│  Failure             │ COORDINATOR    │ ORCHESTRA │ NONE ✅                │
│  Works with          │               │           │                         │
│  Different DBs       │ NO ❌          │ YES       │ YES ✅                  │
│  Horizontal          │               │           │                         │
│  Scaling             │ NO ❌          │ LIMITED   │ YES ✅                  │
│  Complexity          │ HIGH           │ MEDIUM     │ MEDIUM                 │
│  Debugging           │ MEDIUM         │ EASY      │ HARD                   │
│  Order of Events     │ GUARANTEED ✅   │ GUARANTEED│ NOT GUARANTEED         │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Visual Comparison

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          2 PHASE COMMIT                                  │
│                                                                         │
│   Coordinator ───────────── PREPARE ────────────► Participant          │
│       │                         │                                        │
│       │                         ▼                                        │
│       │                    ┌─────────┐                                   │
│       │                    │ LOCKED! │  ←── Other requests WAIT          │
│       │                    └─────────┘                                   │
│       │                         │                                        │
│       │◄────────────────────────┘                                        │
│       │        (All voted YES)                                           │
│       │                                                                  │
│       │                                                                  │
│       ▼              TIME                                                 │
│   ┌─────────┐                                                          │
│   │ COMMIT  │──────────────────────────────────────────► Participant     │
│   └─────────┘                                                          │
│                                                                          │
├─────────────────────────────────────────────────────────────────────────┤
│                       SAGA ORCHESTRATION                                  │
│                                                                          │
│   Orchestrator                                                          │
│       │                                                                  │
│       ├─── STEP 1 ───► Service A ──► success ──► Service B             │
│       │                        │                   │                   │
│       │                        │◄─── compensate    │                   │
│       │                        │     if fail        │                   │
│       │                        │                    ▼                   │
│       │                        │              Service C                  │
│       │                        │                    │                    │
│       │                        │◄────────────────────┘                   │
│       │                        │      compensate if fail                 │
│       │                        │                                          │
│       ▼                        ▼                                          │
│   Result                 Status                                           │
│                                                                          │
├─────────────────────────────────────────────────────────────────────────┤
│                      SAGA CHOREOGRAPHY                                    │
│                                                                          │
│   Service A                                                             │
│       │                                                                 │
│       │──publish Event 1 ────────────────────────────────────►          │
│       │                                                                 │
│       │                                             Service B          │
│       │                                                 │              │
│       │                              ◄───publish Event 2──┤            │
│       │                                                 │              │
│       │                     Service A (reacts to Event 2)│            │
│       │                                                 │              │
│       │                         ◄───publish Event 3──────┘            │
│       │                                                                 │
│       │                                             Service C          │
│       │                                                 │              │
│       │                            ◄─── No direct communication! ──────┤
│       │                                                                 │
│       │                                                                 │
│       ▼                                                                 │
│   Complete                                                              │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## When to Use Which Pattern

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        DECISION GUIDE                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Use 2PHASE COMMIT when:                                                  │
│  ═══════════════════════                                                 │
│  ✓ All services share the SAME database                                  │
│  ✓ You need STRONG ACID consistency                                     │
│  ✓ Low transaction volume (blocking is acceptable)                     │
│  ✓ Examples: Financial transactions, inventory in single DB            │
│                                                                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Use SAGA ORCHESTRATION when:                                            │
│  ═══════════════════════════                                             │
│  ✓ Complex workflows with many steps                                     │
│  ✓ You want CLEAR visibility of saga flow                               │
│  ✓ Centralized error handling is preferred                              │
│  ✓ Examples: E-commerce checkout, loan processing                       │
│                                                                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Use SAGA CHOREOGRAPHY when:                                             │
│  ═══════════════════════════                                             │
│  ✓ Services should remain INDEPENDENT                                   │
│  ✓ You need HIGH SCALABILITY                                            │
│  ✓ Different teams own different services                               │
│  ✓ You want to AVOID single point of failure                            │
│  ✓ Examples: Your current microservices architecture                   │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Your Project: Why Saga Choreography?

Based on your current implementation:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                   WHY CHOREOGRAPHY WORKS FOR YOU                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  1. DIFFERENT DATABASES                                                   │
│     ═════════════════════                                                 │
│     • Order Service      → PostgreSQL                                    │
│     • Inventory Service  → PostgreSQL + Redis                           │
│     • Product Service    → MongoDB                                       │
│     • Notification Svc   → Email (SMTP)                                  │
│                                                                          │
│     2PC can't work with MongoDB! Saga can. ✅                           │
│                                                                          │
│  2. INDEPENDENT SCALING                                                  │
│     ═════════════════════                                                 │
│     • Each service can scale independently                              │
│     • No coordinator bottleneck                                          │
│     • Run multiple instances without conflict                            │
│                                                                          │
│  3. RESILIENCE                                                           │
│     ═══════════                                                           │
│     • Kafka buffers messages during failures                            │
│     • Services recover and process pending events                       │
│     • No data loss during temporary outages                             │
│                                                                          │
│  4. TEAM AUTONOMY                                                       │
│     ═══════════════                                                       │
│     • Order team → owns OrderService                                    │
│     • Inventory team → owns InventoryService                            │
│     • Teams don't need to coordinate for changes                        │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Summary

| Pattern | Best For | Avoid When |
|---------|----------|------------|
| **2PC** | Single database, strong consistency | Microservices, high concurrency |
| **Saga Orchestration** | Complex workflows, clear logic | You need loose coupling |
| **Saga Choreography** | Microservices, independent teams, scalability | You need to trace full flow easily |

Your current implementation using **Saga Choreography with Kafka** is the **right choice** for a microservices architecture with multiple databases and independent services.
