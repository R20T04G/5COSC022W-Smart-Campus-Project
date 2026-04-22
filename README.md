# Smart Campus and Room Management API

## Overview

This is my coursework submission for the **5COSC022W Client-Server Architectures** module. The API manages campus rooms, IoT sensors, and historical sensor readings using a RESTful interface.

As per the coursework requirements, there is no external database. Data is stored in-memory using thread-safe collections like `ConcurrentHashMap` and `CopyOnWriteArrayList`. The project is a Maven WAR application designed to be deployed to an Apache Tomcat server via NetBeans.

## Project Setup and Build Instructions

Follow these step-by-step instructions to build the project and launch the server:

1. **Prerequisites**: Make sure you have Java 8 (or higher), Apache Maven, NetBeans IDE, and Apache Tomcat installed.
2. **Open the Project**: Launch NetBeans. Go to `File` -> `Open Project...` and select the `5COSC022W-Smart-Campus-Project` directory.
3. **Configure Tomcat**: If you haven't already, add your Apache Tomcat server to NetBeans (Services tab -> Servers -> Add Server).
4. **Build the Project**: Right-click on the project in the NetBeans Projects pane and select **Clean and Build**. Maven will download the required JAX-RS dependencies and build the WAR file.
5. **Run the Server**: Right-click on the project and select **Run** (or **Deploy**). NetBeans will start Tomcat and deploy the application.
6. **Verify Deployment**: Once Tomcat is running, the API will be available at the base URL.

## Base URL

When running locally on port 8080:

```
http://localhost:8080/5COSC022W-Smart-Campus-Project/api/v1
```

## API Architecture and Data Model

The API is built using JAX-RS (Jakarta EE 8) and provides a clean resource hierarchy. 

**Data Entities:**
- **Room**: Represents a physical location. Fields: `id` (String), `name`, `capacity`, `sensorIds`.
- **Sensor**: Represents an IoT device in a room. Fields: `id` (String), `type`, `status`, `currentValue`, `roomId`.
- **SensorReading**: A single measurement. Fields: `id` (String), `timestamp` (long), `value`.

**Endpoints:**
- `GET /api/v1` - Discovery endpoint 
- `/api/v1/rooms` - Room management (GET, POST, GET {id}, DELETE {id})
- `/api/v1/sensors` - Sensor management (GET with `?type` filter, POST, GET {id})
- `/api/v1/sensors/{id}/readings` - Sub-resource for historical readings (GET, POST)

---

## Report Answers

Below are my answers to the conceptual questions from the coursework specification.

### 1.1 - JAX-RS Lifecycle and Synchronisation

By default, JAX-RS resource classes use a **request-scoped** lifecycle. This means the server creates a new instance of the resource class for every single HTTP request it receives. 

Because of this, we can't store our data in regular instance variables inside the resource classes, or they would be lost after each request. Instead, I used a `DataStore` class with `static` variables to act as an in-memory database shared across all requests. To prevent race conditions and data corruption when multiple requests try to modify data at the same time, I used thread-safe collections (`ConcurrentHashMap`, `CopyOnWriteArrayList`) and used `synchronized` blocks for complex updates (like creating a sensor and adding its ID to a room's list at the same time).

### 1.2 - Discovery Endpoint and HATEOAS

The discovery endpoint at `GET /api/v1` returns links to the primary resources in the API. This follows the concept of **HATEOAS** (Hypermedia as the Engine of Application State).

This approach benefits client developers because they don't have to hardcode URLs in their applications. If the server changes its internal URL structure later, the client can just read the discovery response to find the new paths. It makes the API self-documenting and much easier to navigate compared to relying entirely on static documentation.

### 2.1 - Room Implementation and POST Return Strategy

When a user fetches a list of rooms, returning the full room objects uses more network bandwidth compared to just returning a list of IDs. However, it significantly reduces client-side processing. 

If we only returned IDs, the client application would have to make an additional `GET` request for every single room to display its name and capacity. Returning the full objects upfront prevents this "N+1 request problem," making the application faster and reducing the total number of HTTP connections the server has to handle.

### 2.2 - Deletion and Idempotency

Yes, the `DELETE /rooms/{id}` operation is idempotent in this implementation.

If a client mistakenly sends the exact same `DELETE` request for a room multiple times, the first request will remove the room and return a `204 No Content` status. Any subsequent requests for that same ID will also return `204 No Content`. Because the end result on the server (the room does not exist) is exactly the same no matter how many times the request is sent, the operation is strictly idempotent.

### 3.1 - Sensor Integrity and Content-Type Enforcement

I used the `@Consumes(MediaType.APPLICATION_JSON)` annotation on the POST method to tell JAX-RS that this endpoint only understands JSON data. 

If a client attempts to send data in a different format, such as `text/plain` or `application/xml`, the JAX-RS framework will automatically intercept the request and return a `415 Unsupported Media Type` error. It handles this mismatch cleanly before the request ever reaches my Java method, saving me from having to write custom validation logic for the request format.

### 3.2 - Filtered Retrieval: QueryParams vs. PathParams

I used a query parameter (`?type=CO2`) for filtering instead of making it part of the URL path (`/sensors/type/CO2`).

Query parameters are generally considered superior for filtering because they are optional by nature. A path defines the identity of a specific resource, while a query string acts as a modifier for a collection. Using query parameters also makes it much easier to combine multiple filters in the future (e.g., `?type=CO2&status=active`) without having to create complex and confusing URL path structures.

### 4.1 - Sub-Resource Locator Architecture

The Sub-Resource Locator pattern (delegating `/sensors/{id}/readings` to a `SensorReadingResource` class) provides significant architectural benefits. 

Instead of cramming every single endpoint into one massive controller class, this pattern lets us divide our code into smaller, focused classes. The `SensorResource` only handles sensors, while `SensorReadingResource` only worries about readings. This makes the code much easier to read, maintain, and test, and it helps manage complexity as the API grows larger.

### 5.1 - Exception Mapping: Why 422 Instead of 404

When a client tries to create a sensor but provides a `roomId` that doesn't exist, returning `422 Unprocessable Entity` is much more semantically accurate than a `404 Not Found`.

A `404` error implies that the URL endpoint itself doesn't exist. In this case, the `POST /sensors` endpoint definitely exists and works fine. The actual problem is a logical error inside the JSON payload (a missing foreign key reference). HTTP 422 specifically means the server understands the content type and syntax of the request, but was unable to process the contained instructions, making it the perfect fit.

### 5.2 - Global Safety Net and Cybersecurity

Exposing internal Java stack traces to external API consumers is a major cybersecurity risk. 

Stack traces reveal detailed technical information about how the application is built. An attacker can gather internal file paths, the specific names of classes and packages, and the exact versions of libraries or frameworks being used (e.g., Tomcat, Jersey). They can use this information to search for known vulnerabilities (CVEs) in those specific library versions, or use the execution flow to map out potential logic flaws in the code. My catch-all `ExceptionMapper<Throwable>` prevents this by ensuring a generic 500 error is always returned instead.

### 5.3 - API Request & Response Logging Filters

Using JAX-RS filters for logging is advantageous because logging is a cross-cutting concern. 

If I manually inserted `Logger.info()` statements inside every single resource method, it would clutter the business logic with repetitive boilerplate code. More importantly, it relies on human memory: if another developer adds a new endpoint and forgets to add the logging statement, that endpoint becomes invisible to our logs. A filter applies globally and automatically intercepts every request and response, guaranteeing complete observability without touching the resource methods.

---

## Quick-Start Testing (cURL Examples)

Here are some sample cURL commands to test the API. *Note: IDs are automatically generated strings (e.g. ROOM-1).*

**1. View the Discovery endpoint**
```bash
curl -i http://localhost:8080/5COSC022W-Smart-Campus-Project/api/v1
```

**2. Create a new room**
```bash
curl -i -X POST http://localhost:8080/5COSC022W-Smart-Campus-Project/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name":"Library Study","capacity":50}'
```

**3. Create a sensor (assumes room ID is ROOM-1)**
```bash
curl -i -X POST http://localhost:8080/5COSC022W-Smart-Campus-Project/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type":"temperature","status":"active","roomId":"ROOM-1","currentValue":21.5}'
```

**4. Add a reading to the sensor (assumes sensor ID is SENSOR-1)**
```bash
curl -i -X POST http://localhost:8080/5COSC022W-Smart-Campus-Project/api/v1/sensors/SENSOR-1/readings \
  -H "Content-Type: application/json" \
  -d '{"value":22.1}'
```

**5. Get sensor reading history**
```bash
curl -i http://localhost:8080/5COSC022W-Smart-Campus-Project/api/v1/sensors/SENSOR-1/readings
```

**6. Trigger a 422 error (invalid room ID)**
```bash
curl -i -X POST http://localhost:8080/5COSC022W-Smart-Campus-Project/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type":"CO2","roomId":"INVALID-ROOM"}'
```
