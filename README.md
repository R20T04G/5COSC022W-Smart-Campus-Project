# Smart Campus & Room Management API

## Overview
This repository contains the completed JAX-RS coursework API for module 5COSC022W.
The API is implemented with in-memory data structures only and is designed to run in NetBeans with Maven WAR deployment.

## Project Constraints Followed
- Framework: JAX-RS (Jakarta EE 8), no Spring Boot.
- Storage: in-memory only (`ConcurrentHashMap`, `CopyOnWriteArrayList`), no SQL database.
- API root: `/api/v1`.

## Base URL
After deployment on default local server:

`http://localhost:8080/5COSC022W-Smart-Campus-Project/api/v1`

## Data Model
- Room: `id`, `name`, `capacity`, `sensorIds`
- Sensor: `id`, `type`, `status`, `currentValue`, `roomId`
- SensorReading: `id`, `timestamp`, `value`

## Endpoint Summary
- `GET /api/v1`
	- Discovery metadata and resource map.
- `GET /api/v1/rooms`
	- List all rooms.
- `POST /api/v1/rooms`
	- Create room, returns `201 Created` with `Location`.
- `GET /api/v1/rooms/{id}`
	- Get room by ID.
- `DELETE /api/v1/rooms/{id}`
	- Idempotent delete (`204` if deleted or already missing).
	- Returns `409` if room still has attached sensors.
- `GET /api/v1/sensors`
	- List sensors.
- `GET /api/v1/sensors?type=temperature`
	- Optional type filter.
- `POST /api/v1/sensors`
	- Create sensor after validating `roomId` exists.
- `GET /api/v1/sensors/{id}`
	- Get sensor by ID.
- `GET /api/v1/sensors/{id}/readings`
	- Get sensor reading history (sub-resource).
- `POST /api/v1/sensors/{id}/readings`
	- Create new reading and update parent sensor `currentValue`.
- `GET /api/v1/diagnostics/fail`
	- Intentional endpoint to demonstrate global `500` mapper safely.

## Error Handling
All errors return structured JSON:

```json
{
	"timestamp": "2026-04-22T12:34:56Z",
	"status": 422,
	"error": "Unprocessable Entity",
	"message": "roomId 999 does not reference an existing room.",
	"path": "5COSC022W-Smart-Campus-Project/api/v1/sensors"
}
```

Implemented mappers:
- `409 Conflict` (`ConflictException`)
- `422 Unprocessable Entity` (`UnprocessableEntityException`)
- `403 Forbidden` (`ForbiddenOperationException`)
- Global `ExceptionMapper<Throwable>` for safe `500` responses without stack trace leakage

## Report Answers (For Coursework PDF Submission)
The following are the conceptual justifications implemented in this API to satisfy the "Excellent (70%+)" rubric metrics.

### 1.1 JAX-RS Lifecycle & Synchronization
Request-scoped resources (the JAX-RS default) create a new instance per HTTP request, isolating state and eliminating race conditions at the controller level. For the underlying in-memory data store, we use thread-safe collections (`ConcurrentHashMap` and `CopyOnWriteArrayList`) instead of standard `HashMap`/`List` to prevent dirty reads and `ConcurrentModificationException`s during parallel operations.

### 1.2 Discovery Endpoint & HATEOAS
The discovery endpoint (`GET /api/v1`) makes the API self-documenting. By providing a map of resource URIs (`resources` object) and versioning metadata, clients can dynamically discover endpoints rather than relying on hardcoded URLs or static external documents. This decouples the client from the server's exact routing structure, allowing the API to evolve safely.

### 2.1 Room Implementation (POST Returns)
When a room is created, the API returns the full newly created JSON object rather than just an ID. While returning an ID-only saves initial bandwidth, returning the full object saves the client from having to make an immediate, subsequent `GET` request to verify the server-assigned fields (like `id`). This reduces network round-trips and overall latency.

### 2.2 Deletion & Idempotency
`DELETE /rooms/{id}` is fully idempotent. If a client sends the exact same DELETE request multiple times, the server state remains the same (the room is gone). The API returns a `204 No Content` for both successful deletions and cases where the room is already deleted. This allows clients to safely retry failed network calls without writing complex error-handling logic for 404s on retries.

### 3.1 Sensor Integrity & JAX-RS Validation
The JAX-RS `@Consumes(MediaType.APPLICATION_JSON)` annotation enforces strict media typing. If a client sends form data instead, JAX-RS automatically intercepts the request and throws a `415 Unsupported Media Type` before our business logic is even reached. Inside the logic, we validate the `roomId` foreign key constraint to ensure data relational integrity.

### 3.2 Filtering: QueryParams vs. PathParams
We use `@QueryParam` (`/sensors?type=temperature`) instead of `@PathParam` (`/sensors/temperature`) for filtering. Path parameters dictate resource identity and hierarchy, while query parameters represent optional refinements to a collection. Query strings are more flexible, scalable for multi-field filtering, and semantically correct for search operations.

### 4.1 Sub-Resource Locator Architecture
The endpoint `/sensors/{id}/readings` uses a sub-resource locator pattern. `SensorResource` contains a method annotated only with `@Path` that returns a new instance of `SensorReadingResource`. This delegates the processing of the readings hierarchy to a dedicated class, preventing the main sensor controller from becoming a bloated monolith and keeping concerns strictly separated.

### 5.1 Exception Mapping: 422 vs 404
When registering a sensor to a non-existent room, the API returns `422 Unprocessable Entity` rather than `404 Not Found`. A `404` implies that the POST URL itself doesn't exist. Since the URL is correct, but the *payload contents* contain an invalid relational reference, `422` is the semantically accurate HTTP status code for business-rule validation failures.

### 5.2 Global Safety Net (500 Errors)
The `GlobalThrowableMapper` acts as a catch-all for unhandled exceptions, converting them into clean `500 Internal Server Error` JSON responses. Without this, the server would leak raw Java stack traces to the client. Stack traces expose internal file paths, framework versions, and exact lines of code where logic fails—information an attacker can use to find known vulnerabilities or plan exploits.
```bash
curl -i http://localhost:8080/5COSC022W-Smart-Campus-Project/api/v1
```

2. Create room

```bash
curl -i -X POST http://localhost:8080/5COSC022W-Smart-Campus-Project/api/v1/rooms \
	-H "Content-Type: application/json" \
	-d '{"name":"Lab A","capacity":30}'
```

3. Create sensor for room 1

```bash
curl -i -X POST http://localhost:8080/5COSC022W-Smart-Campus-Project/api/v1/sensors \
	-H "Content-Type: application/json" \
	-d '{"type":"temperature","status":"active","roomId":1,"currentValue":22.5}'
```

4. Filter sensors by type

```bash
curl -i "http://localhost:8080/5COSC022W-Smart-Campus-Project/api/v1/sensors?type=temperature"
```

5. Add reading to sensor 1

```bash
curl -i -X POST http://localhost:8080/5COSC022W-Smart-Campus-Project/api/v1/sensors/1/readings \
	-H "Content-Type: application/json" \
	-d '{"value":23.1}'
```

6. Trigger 422 (invalid roomId)

```bash
curl -i -X POST http://localhost:8080/5COSC022W-Smart-Campus-Project/api/v1/sensors \
	-H "Content-Type: application/json" \
	-d '{"type":"temperature","roomId":999}'
```

7. Trigger 403 (forbidden type)

```bash
curl -i -X POST http://localhost:8080/5COSC022W-Smart-Campus-Project/api/v1/sensors \
	-H "Content-Type: application/json" \
	-d '{"type":"biometric","roomId":1}'
```

8. Trigger 500 (safe global mapper)

```bash
curl -i http://localhost:8080/5COSC022W-Smart-Campus-Project/api/v1/diagnostics/fail
```
