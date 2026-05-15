



# campaign-analytics: High-Performance System Design (Phase 1)

**campaign-analytics** is a real-time tracking engine designed to handle high-volume ad tech data. This Phase 1 implementation demonstrates a **Scalable Monolith** architecture capable of ingesting thousands of events and providing sub-100ms dashboard latency through intelligent pre-aggregation.

---

## 🚀 Phase 1 Milestones

* **High-Throughput Ingestion**: Optimized REST API (`/api/v1/events`) for recording raw "Click" and "Impression" events into an append-only store.
* **In-Memory Aggregator**: A background worker utilizing Java Stream `Collectors` to perform Map-Reduce logic, grouping millions of raw rows into hourly summary buckets.
* **Atomic Counter Upserts**: Implementation of PostgreSQL `ON CONFLICT` logic to ensure 100% data accuracy and prevent race conditions during concurrent updates.
* **Persistent Watermarking**: Redis-backed state management that tracks the last processed event timestamp, making the aggregator resilient to application restarts.
* **Read-Optimized Dashboard API**: A dedicated read-path (`/api/v1/stats`) optimized for fast time-series queries by pulling from pre-aggregated rollup tables.

---

## 🏗️ Architecture & Core Concepts

### CQRS (Command Query Responsibility Segregation)
The system separates the write-heavy ingestion path from the read-optimized analytics path:
1.  **Command Path**: Raw events are saved into an append-only table (`campaign_events`).
2.  **Aggregation Logic**: A scheduled job groups events by `CampaignID` and `HourBucket` in RAM, reducing database IO overhead.
3.  **Query Path**: Dashboard reads hit the pre-aggregated `campaign_stats` table, avoiding massive real-time table scans.

### Tech Stack
* **Backend**: Java 21, Spring Boot 3, Spring Data JPA.
* **Database**: PostgreSQL 15 (Relational storage & Atomic Upserts).
* **State Store**: Redis 7 (Watermark tracking).
* **Infrastructure**: Docker & Docker Compose.
* **Monitoring**: Grafana (SQL-Direct visualization).

---

## 🛠️ Getting Started

### 1. Start Infrastructure
```bash
docker compose up -d

```

### 2. Run the Application

```bash
./mvnw spring-boot:run

```

### 3. Ingest Sample Data

```bash
curl -X POST http://localhost:8080/api/v1/events \
     -H "Content-Type: application/json" \
     -d '{"campaignId": 101, "eventType": "CLICK"}'

```

### 4. Query Stats API

```bash
curl -X GET "http://localhost:8080/api/v1/stats/101"

```

---

## 📈 Phase 2 Roadmap (Distributed Architecture)

* **Kafka Decoupling**: Moving to an event-driven architecture to buffer ingestion spikes.
* **Consumer Workers**: Scaling out aggregation logic across multiple worker instances.
* **Reliability**: Implementing Dead Letter Queues (DLQ) and exponential backoff retries.
