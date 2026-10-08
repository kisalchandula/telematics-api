# Telematics API

A Kotlin/JVM telematics backend that receives vehicle telemetry over TCP, decodes Teltonika Codec 8 AVL packets, persists telemetry in PostgreSQL, and exposes the latest vehicle position through a Spring Boot REST API.

The project was built as a focused proof-of-work demonstrating practical experience with **Kotlin/JVM, TCP networking, binary protocol decoding, PostgreSQL, Spring Boot, Docker, and vehicle telemetry processing**.

## Architecture

```text
┌─────────────────────┐
│  Vehicle / Simulator│
└──────────┬──────────┘
           │
           │ TCP :5000
           ▼
┌─────────────────────────────┐
│   Kotlin Telematics Gateway │
│                             │
│ • IMEI handshake             │
│ • Device authentication      │
│ • Packet framing             │
│ • CRC-16 validation          │
│ • Codec 8 AVL decoding       │
│ • Device session handling    │
└────────────┬────────────────┘
             │
             │ TelemetryEvent
             ▼
┌─────────────────────────────┐
│        PostgreSQL           │
│                             │
│ • Devices                   │
│ • Telemetry events          │
│ • Device state              │
└────────────┬────────────────┘
             │
             ▼
┌─────────────────────────────┐
│      Spring Boot API        │
│          :8080              │
│                             │
│ GET /api/devices/{imei}/    │
│     latest                  │
└────────────┬────────────────┘
             │
             ▼
┌─────────────────────────────┐
│       Web Map UI            │
│   Live vehicle positions    │
└─────────────────────────────┘
```

## Key Features

### TCP Telematics Gateway

* TCP server on port `5000`
* Teltonika IMEI handshake
* Device registration and active-device validation
* Device session management
* Binary packet framing
* CRC-16 validation
* Teltonika Codec 8 AVL packet decoding
* AVL acknowledgements
* Multiple concurrent device connections

### Vehicle Telemetry

The gateway extracts and stores information including:

* IMEI
* GPS latitude / longitude
* Event timestamp
* Altitude
* Speed
* Heading
* Satellite count
* IO elements

### Persistence

Telemetry is stored in PostgreSQL.

The database contains:

* Registered devices
* Telemetry events
* Device state

### REST API

Spring Boot exposes telemetry through HTTP.

Example:

```http
GET /api/devices/356307042441013/latest
```

Example response:

```json
{
  "imei": "356307042441013",
  "timestamp": "2023-11-14T22:13:20Z",
  "latitude": 49.0,
  "longitude": 8.0,
  "altitude": 120,
  "angle": 90,
  "satellites": 10,
  "speed": 50,
  "ioElements": {}
}
```

### Telemetry Simulator

The project includes a Kotlin simulator with three virtual vehicles:

```text
356307042441013 → Karlsruhe route 1
356307042441014 → Karlsruhe route 2
356307042441015 → Karlsruhe route 3
```

The simulator establishes real TCP connections to the gateway and sends Teltonika-compatible telemetry packets.

## Technology Stack

| Technology        | Purpose                             |
| ----------------- | ----------------------------------- |
| Kotlin            | Telematics gateway and simulator    |
| Java 21           | JVM runtime                         |
| Spring Boot       | REST API                            |
| Gradle            | Build system                        |
| TCP/IP            | Vehicle communication               |
| Teltonika Codec 8 | AVL telemetry protocol              |
| PostgreSQL        | Telemetry persistence               |
| TimescaleDB       | PostgreSQL time-series capabilities |
| JUnit             | Automated testing                   |
| Testcontainers    | Database integration testing        |
| Docker            | Containerized runtime               |
| Mapbox            | Vehicle visualization               |

## Running Locally

### Prerequisites

* JDK 21
* Docker Desktop
* Git

### Start PostgreSQL

From the project root:

```powershell
docker compose up -d postgres
```

### Start the API

Run Spring Boot directly:

```powershell
.\gradlew.bat bootRun
```

The application starts:

```text
REST API:          http://localhost:8080
Telematics TCP:    localhost:5000
```

### Run the Simulator

Open another terminal:

```powershell
.\gradlew.bat :simulator:run
```

The simulator starts three virtual vehicles and sends telemetry to the TCP gateway.

### Verify the API

Health check:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

Query the latest telemetry:

```powershell
Invoke-RestMethod http://localhost:8080/api/devices/356307042441013/latest
```

## Running with Docker

The project also contains Docker configuration for running the application and PostgreSQL together.

Build the application JAR first:

```powershell
.\gradlew.bat bootJar
```

Then:

```powershell
docker compose up
```

The services expose:

```text
Spring Boot API       http://localhost:8080
Telematics TCP        localhost:5000
PostgreSQL            localhost:5432
```

## Testing

Run the test suite with:

```powershell
.\gradlew.bat test
```

The tests cover areas including:

* TCP client connections
* IMEI processing
* Teltonika packet decoding
* CRC validation
* AVL packet handling
* Device sessions
* Telemetry processing
* Database integration

## Project Structure

```text
telematics-api/
│
├── src/
│   ├── main/
│   │   └── kotlin/
│   │       └── org/kisal/telematicsapi/
│   │           ├── device/
│   │           ├── domain/
│   │           ├── protocol/
│   │           ├── server/
│   │           └── ...
│   │
│   └── test/
│
├── simulator/
│   └── ...
│
├── frontend/
│   └── ...
│
├── schema.sql
├── Dockerfile
├── docker-compose.yml
├── build.gradle.kts
└── settings.gradle.kts
```

## What This Project Demonstrates

This project focuses on the complete telemetry ingestion path rather than only implementing a REST API:

```text
TCP connection
      ↓
IMEI authentication
      ↓
Binary packet framing
      ↓
CRC validation
      ↓
Codec 8 decoding
      ↓
Telemetry event mapping
      ↓
PostgreSQL persistence
      ↓
Spring Boot REST API
      ↓
Vehicle visualization
```

It demonstrates practical JVM backend development involving **network programming, binary protocols, concurrent device connections, database persistence, API development, automated testing, and containerization**.

The current implementation is intended as a technical demonstration of telematics ingestion and backend development rather than a production fleet-management platform.
