
---

# Campaign Analytics Engine — Phase 2: Distributed Streaming & Resilience

This project has been upgraded to a decoupled, high-throughput, real-time streaming analytics engine. By removing the batch aggregator from the API Gateway and implementing a distributed worker model with Apache Kafka, the system safely buffers high-volume traffic spikes, processes events concurrently without race conditions, and guarantees data integrity.

## Architecture Blueprint

```
                      [ Client Traffic / Curl ]
                                 │
                                 ▼
                     ┌───────────────────────┐
                     │  caampaign-analytics  │  (Inbound Ingestion Gateway)
                     │     (Port 8080)       │  (Returns 202 Accepted)
                     └───────────┬───────────┘
                                 │
                      (Key: campaignId)
                                 │
                                 ▼
                    ┌─────────────────────────┐
                    │  Apache Kafka Cluster   │  (Elastic Backpressure Buffer)
                    │  (campaign-events-raw)  │  (3 Partitions)
                    └────┬───────────────┬────┘
                         │               │
            ┌────────────┘               └────────────┐
            ▼                                         ▼
┌─────────────────────────┐               ┌─────────────────────────┐
│   campaign-processor    │               │   campaign-processor    │
│    (Worker Instance 1)  │               │    (Worker Instance 2)  │
├─────────────────────────┤               ├─────────────────────────┤
│ 1. Redis SETNX Guard    │               │ 1. Redis SETNX Guard    │
│ 2. Inline Aggregation   │               │ 2. Inline Aggregation   │
│ 3. Atomic DB UPSERT     │               │ 3. Atomic DB UPSERT     │
└───────────┬─────────────┘               └───────────┬─────────────┘
            │                                         │
            └────────────────────┬────────────────────┘
                                 │
                                 ▼
                    ┌─────────────────────────┐
                    │  PostgreSQL (Database)  │  (Atomic Read/Write Path)
                    │    `campaign_stats`     │  (Sub-millisecond Updates)
                    └─────────────────────────┘

```

---

## Key Milestone Enhancements

### 1. Ingestion & Aggregation Decoupling

* **The Shift:** The legacy `@Scheduled` batch-aggregator task inside `caampaign-analytics` has been deactivated.
* **The Mechanism:** The API Gateway is now a lightweight, non-blocking pass-through. It drops incoming event payloads straight onto a Kafka broker channel and instantly returns an HTTP `202 Accepted` status back to the client.

### 2. Key-Based Hashing & Concurrency Control

* **The Strategy:** Kafka payloads are routed explicitly using the `campaignId` as the record key.
* **The Mechanism:** This guarantees that all metrics associated with a single campaign dynamically route to the exact same partition and are consumed sequentially by the exact same worker thread. This eliminates distributed race conditions and row-locking resource contention in PostgreSQL.

### 3. Defensive Idempotency Gates

* **The Strategy:** Distributed consumer threads filter duplicate events at the caching layer before reaching disk storage.
* **The Mechanism:** Workers execute an atomic `SETNX` (set if absent) key check (`event:processed:{eventId}`) against Redis 7 with a 24-hour TTL. If a duplicate message is transmitted over the network, it is dropped instantly at the gate.

### 4. Resilient Fault Tolerance & Non-Blocking Retries

* **The Strategy:** Poison pills or transient environmental bugs are caught and isolated automatically without disrupting global consumer throughput.
* **The Mechanism:** Engineered with a custom Spring Kafka `DefaultErrorHandler` and a JSON-backed `DeadLetterPublishingRecoverer`. Failing tasks are retried locally exactly 3 times (initial delivery + 2 retries) with an explicit 2-second backoff. Permanent errors are safely dispatched to the isolated `campaign-events-raw-dlt` channel, allowing workers to commit offsets and move forward.

---

## Technical Specifications

| Technology | Role | Configuration / Details |
| --- | --- | --- |
| **Spring Boot** | Framework | v4.0.6 (Running on **Java 21**) |
| **Apache Kafka** | Event Streaming | 3 Partitions / Classic Group Protocol |
| **Redis 7** | Idempotency Cache | Distributed Memory Layer (`StringRedisTemplate`) |
| **PostgreSQL 15** | Relational Database | Atomic `INSERT ... ON CONFLICT DO UPDATE` (`UPSERT`) |

---

## Local Verification Commands

### 1. Fire Inbound Test Event Surge

Use this Debian bash script loop to flood the ingestion gateway with 60 parallel events across alternating campaign channels to verify backpressure cushioning:

```bash
for i in {1..60}; do
  CAMPAIGN=$((101 + (i % 3))) 
  UUID=$(cat /proc/sys/kernel/random/uuid)
  
  curl -s -X POST http://localhost:8080/api/v1/events \
    -H "Content-Type: application/json" \
    -d "{
      \"eventId\": \"$UUID\",
      \"campaignId\": $CAMPAIGN,
      \"eventType\": \"IMPRESSION\",
      \"timestamp\": \"2026-05-18T12:00:00Z\"
    }" > /dev/null
done
echo "🚀 Traffic spike simulated: 60 payloads written to Kafka."

```

### 2. Monitor Live Consumer Group Lag

To inspect how Kafka safely holds backpressure while horizontal worker nodes digest records at their own stable pace, execute the native Kafka metrics descriptor utility inside your broker container:

```bash
docker exec -it caampaign-analytics-kafka-1 \
  kafka-consumer-groups --bootstrap-server localhost:9092 \
  --describe --group campaign-processor-group

```

### 3. Check Live Aggregated Results

Connect to your `psql` instance and run this direct query against your data presentation table to verify that the inline `upsertStats` processor engine is incrementing campaign counters in true real-time:

```sql
SELECT campaign_id, hour_bucket, impression_count, click_count 
FROM campaign_stats 
ORDER BY hour_bucket DESC;

```

---