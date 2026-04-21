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

## Submission Notes (Deviations and Justification)
- `DELETE /rooms/{id}` returns `204` for both deleted and already missing rooms to preserve idempotency.
- The rule `type=biometric` is blocked with `403` to clearly demonstrate the forbidden-operation rubric requirement.
- `GET /diagnostics/fail` exists only to demo the global 500 mapper in Postman/video.

## Build and Run (NetBeans / Maven)
1. Open the inner Maven project folder:
	 `5COSC022W-Smart-Campus-Project/`
2. Build:
	 `mvn clean package`
3. Deploy generated WAR to your Jakarta EE server (GlassFish/Payara/Tomcat with compatible setup).

## cURL Demo Commands
Use these for README/report evidence and quick checks.

1. Discovery

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
