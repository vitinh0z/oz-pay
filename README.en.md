# OzPay

SaaS platform for intelligent payment orchestration across multiple gateways

## Overview

OzPay is a multi-tenant system that abstracts the complexity of integrating with different payment providers. The platform offers a unified API that enables transaction processing through multiple gateways (Stripe, Cielo, PayPal, etc.) with intelligent routing, advanced resilience, and complete observability.

The project will be commercialized as SaaS (domain: OzPay.app or OzPay.io in planning) but maintains an open-source codebase to ensure transparency and allow the community to contribute improvements.

## Motivation

### Problems OzPay Solves

**Multiple Payment APIs**
- Each gateway has its own API, data format, and behavior
- Integrating with multiple providers requires maintaining duplicated and complex code
- OzPay offers a single, consistent API for all gateways

**Transient Failures**
- Gateways can temporarily fail or experience latency issues
- A transaction rejected by one gateway may be approved by another
- OzPay implements intelligent retry and automatic failover between gateways

**Secure Credentials**
- Each tenant needs their own credentials for the gateways
- Storing and managing credentials securely is critical
- OzPay uses CredentialVault with per-tenant isolation

**Complex Multi-Tenancy**
- SaaS needs to completely isolate data and credentials for each customer
- Tenant context must be propagated throughout the application
- OzPay implements native multi-tenancy with ThreadLocal and data isolation

## Architecture

OzPay follows Clean Architecture with clear separation between Domain, Application, and Infrastructure. The system consists of 7 main layers:

### API Layer (SaaS Entry Point)

**PaymentController**
- REST API for processing payments
- Unified endpoint that abstracts multiple gateways
- Input validation and response formatting

**ApiKeyFilter**
- Authentication via `X-Nexus-Key` header
- Tenant identification and validation
- Security context establishment

### Orchestration Layer

**MetaPaymentGateway**
- Central orchestrator that coordinates the entire payment flow
- Manages idempotency to prevent transaction duplication
- Publishes domain events for asynchronous integration
- Coordinates transaction state persistence

### Decision Engine (The Brain)

**PaymentRouter**
- Selects the best gateway based on configurable strategy
- Supports different strategies: round-robin, least-latency, cost-optimized

**SmartRoutingStrategy**
- Default strategy combining multiple factors
- Scorers for latency, cost, success rate, and availability
- Decision based on weighted score of each factor

**RoutingDecision**
- Auditable routing decision result
- Contains justification and scores for each evaluated gateway
- Enables post-analysis for optimization

### Execution Engine (The Muscle)

**PaymentExecutor**
- Executes transaction on the selected gateway
- Manages timeout and cancellation
- Records result and execution time

**GatewayRegistry**
- Registry Pattern for all available gateways
- Allows dynamic addition/removal of gateways
- Maintains metadata for each gateway (average latency, success rate)

**RetryPolicy**
- Retry policy with exponential backoff
- Configurable per error type
- Limits retry attempts to prevent loops

### Context and State

**PaymentContext**
- Contains complete transaction state
- Includes `tenantId` for multi-tenant isolation
- Provides credential access via `getCredential(gatewayName)`
- Immutable to avoid side effects

**ExecutionHistory**
- Auditable history of all attempts
- Records routing decisions
- Enables debugging and failure analysis

### Gateway Adapters

**PaymentGateway Interface**
- Common contract for all providers
- Specific implementations: StripeGateway, CieloGateway, PayPalGateway
- Per-tenant credential isolation via `context.getCredential()`
- Translation of domain models to specific APIs

### Infrastructure Services

**IdempotencyService**
- Distributed lock with Redis
- Prevents duplicate transaction processing
- Uses idempotency key provided by client

**CredentialVault**
- Secure credential management
- Per-tenant isolation
- Credential rotation support

**TransactionRepository**
- Transaction persistence
- Queries for history and auditing
- Support for filtering by tenant, gateway, status, etc.

**DomainEventPublisher**
- Domain event publishing
- Enables asynchronous integration
- Events: PaymentCreated, PaymentCompleted, PaymentFailed

## Key Features

### Native Multi-Tenancy

- **API Key per Tenant**: Authentication via `X-Nexus-Key` header
- **ThreadLocal Context**: Automatic tenant propagation throughout application
- **CredentialVault**: Complete per-tenant credential isolation
- **Data Partitioning**: Automatic queries filtered by tenant

### Intelligent Routing

- **Success Rate**: Prioritizes gateways with approval history
- **Latency**: Considers average response time of each gateway
- **Cost**: Optimizes based on transaction fees
- **Availability**: Avoids gateways with open circuit breaker

### Hardcore Resilience

- **Idempotency**: Distributed lock with Redis prevents duplication
- **Circuit Breaker**: Resilience4j for fault isolation
- **Retry with Exponential Backoff**: Progressive attempts with increasing delay
- **Configurable Timeout**: Time limits per gateway
- **Automatic Failover**: Gateway switching on failure

### Observability

- **Prometheus Metrics**: Metrics for latency, success rate, errors
- **Grafana Dashboards**: Real-time visualization
- **Auditable History**: All decisions and attempts are recorded
- **Distributed Tracing**: End-to-end transaction tracking

## Technology Stack

- **Runtime**: Java 21 + Spring Boot 3
- **Architecture**: Clean Architecture (Domain/Application/Infrastructure)
- **Persistence**: PostgreSQL 16 + JPA
- **Cache and Lock**: Redis
- **Resilience**: Resilience4j (Circuit Breaker, Retry, Timeout)
- **Observability**: Prometheus + Grafana + Spring Actuator
- **Containerization**: Docker + Docker Compose
- **Validation**: Bean Validation (Jakarta)
- **Build**: Maven

## Development Roadmap

### Phase 0: Foundation
- Spring Boot 3 + Java 21
- Clean Architecture: Domain/Application/Infrastructure
- Docker Compose: PostgreSQL 16

### Phase 1: Domain Model
- Entities: Payment & Tenant
- Value Objects: Money & Currency
- Ports: PaymentGateway

### Phase 2: Application
- DTOs: Java Records
- UseCase: ProcessPayment
- Unit tests without Spring

### Phase 3: Infra Basic
- JPA Adapters & Entity
- PaymentController REST
- FakeGateway (Thread.sleep)

### Phase 4: SaaS Security
- ApiKeyFilter OncePerRequest
- ThreadLocal Context
- Multi-tenancy in DB

### Phase 5: Gateway Engine
- Mocks: Stripe & Cielo
- Registry Pattern Map
- Strategy: SmartRouter

### Phase 6: Hardcore Resilience
- Distributed Redis Lock
- Circuit Breaker Resilience4j
- Exponential Backoff Retry

### Phase 7: Observability
- Prometheus + Grafana
- Spring Actuator Metrics
- Latency Dashboard

### Phase 8: Launch
- Swagger OpenAPI
- Complete README.md
- Deploy Script

## Setup

### Prerequisites

- Java 21
- Docker and Docker Compose
- Maven 3.8+

### Installation

```bash
# Clone the repository
git clone https://github.com/vitinh0z/oz-pay.git
cd oz-pay

# Start dependencies (PostgreSQL, Redis)
docker-compose up -d

# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

The application will be available at `http://localhost:8080`

## Basic Usage

### Process a Payment

```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -H "X-Nexus-Key: your-tenant-api-key" \
  -d '{
    "idempotencyKey": "unique-transaction-id-123",
    "amount": 10000,
    "currency": "BRL",
    "customerEmail": "customer@example.com",
    "description": "Premium Subscription - Monthly Plan"
  }'
```

### Success Response

```json
{
  "transactionId": "txn_abc123xyz",
  "status": "COMPLETED",
  "gateway": "stripe",
  "amount": 10000,
  "currency": "BRL",
  "createdAt": "2026-01-14T10:30:00Z",
  "executionTimeMs": 245
}
```

## Project Status

OzPay is under active development. The architecture and roadmap are defined, and the project is being built incrementally following the planned 8 phases.

Currently, the project foundation is established with:
- Clean Architecture structure
- Domain entities (User, Tenant)
- Persistence layer with JPA

The next phases will include implementation of the payment engine, intelligent routing, and resilience.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

### Open-Source + SaaS Strategy

OzPay will be commercialized as SaaS (OzPay.app or OzPay.io), but the source code will remain open-source. This strategy offers:

- **Transparency**: Customers can audit the code that processes their payments
- **Trust**: No vendor lock-in, self-hosting possibility
- **Community**: Community contributions improve the product for everyone
- **Innovation**: Fast feedback and open collaboration

## Contributing

Contributions are welcome! Please:

1. Fork the project
2. Create a branch for your feature (`git checkout -b feature/MyFeature`)
3. Commit your changes (`git commit -m 'Add MyFeature'`)
4. Push to the branch (`git push origin feature/MyFeature`)
5. Open a Pull Request

### Guidelines

- Maintain Clean Architecture
- Write unit tests for new features
- Follow Java code conventions
- Document public APIs

## Contact

Developed by [@vitinh0z](https://github.com/vitinh0z)

For questions and suggestions, open an issue on GitHub.
