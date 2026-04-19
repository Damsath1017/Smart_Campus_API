# Smart Campus — Sensor & Room Management API

**Module:** 5COSC022W — Client-Server Architectures  
**Student:** Damsath  
**Technology:** Java · JAX-RS (Jersey 2.39.1) · Apache Tomcat 9 · Maven  
**Data Storage:** In-memory (`ConcurrentHashMap`, `ArrayList`)

---

## 1. API Design Overview

The Smart Campus API is a RESTful web service built using the JAX-RS (Jersey) framework. It provides campus facilities managers and automated building systems with a seamless interface to manage **Rooms**, **Sensors**, and **Sensor Readings** across the university campus.

### Resource Hierarchy

```
/api/v1                          → Discovery (API metadata + HATEOAS links)
/api/v1/rooms                    → Room collection
/api/v1/rooms/{roomId}           → Individual room
/api/v1/sensors                  → Sensor collection (supports ?type= filter)
/api/v1/sensors/{sensorId}       → Individual sensor
/api/v1/sensors/{sensorId}/readings → Sensor reading history (sub-resource)
```

### Key Design Decisions

- **Versioned API** — All endpoints are under `/api/v1` to support future versioning.
- **Resource nesting** — Sensor readings are accessed as a sub-resource of sensors using the JAX-RS Sub-Resource Locator pattern.
- **In-memory storage** — `ConcurrentHashMap` is used to provide thread-safe access without a database.
- **Referential integrity** — Creating a sensor validates that the referenced `roomId` exists. Deleting a room is blocked if sensors are still assigned.

---

## 2. How to Build and Run

### Prerequisites

- **Java JDK 11+** (tested with JDK 17)
- **Apache Maven 3.9+**
- **Apache Tomcat 9** (download from https://tomcat.apache.org/download-90.cgi)

### Step-by-Step Instructions

**Step 1: Clone the repository**
```bash
git clone https://github.com/Damsath1017/Smart_Campus_API.git
cd Smart_Campus_API
```

**Step 2: Build the WAR file**
```bash
mvn clean package
```
This produces `target/SmartCampusAPI.war`.

**Step 3: Deploy to Tomcat**
```bash
# Copy the WAR file to Tomcat's webapps directory
copy target\SmartCampusAPI.war C:\tomcat\webapps\
```

**Step 4: Start Tomcat**
```bash
C:\tomcat\bin\startup.bat
```

**Step 5: Verify the API is running**
Open a browser or use curl:
```bash
curl http://localhost:8080/SmartCampusAPI/api/v1
```

You should see a JSON response with API metadata and resource links.

---

## 3. Sample curl Commands

### 3.1 Discovery Endpoint
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1
```

### 3.2 Get All Rooms
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms
```

### 3.3 Create a New Room
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"SCI-202\", \"name\": \"Science Lab 202\", \"capacity\": 40}"
```

### 3.4 Get a Specific Room
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301
```

### 3.5 Register a New Sensor (with room validation)
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"TEMP-005\", \"type\": \"Temperature\", \"status\": \"ACTIVE\", \"currentValue\": 21.0, \"roomId\": \"LIB-301\"}"
```

### 3.6 Get All Sensors Filtered by Type
```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2"
```

### 3.7 Post a Sensor Reading (updates currentValue)
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\": 23.7}"
```

### 3.8 Get Reading History for a Sensor
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings
```

### 3.9 Attempt to Delete a Room with Sensors (triggers 409)
```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301
```

### 3.10 Delete an Empty Room (204 Success)
```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/SCI-202
```

---

## 4. Report — Answers to Coursework Questions

### Part 1: Service Architecture & Setup

**Q1.1 — Default lifecycle of a JAX-RS Resource class. Is a new instance created per request or is it a singleton?**

By default, JAX-RS resource classes follow a **per-request lifecycle**. This means the JAX-RS runtime (Jersey, in our case) creates a **new instance** of the resource class for every incoming HTTP request, and discards it after the response is sent.

This has a direct impact on how we manage our in-memory data. Because a new `RoomResource` or `SensorResource` object is created for each request, we **cannot store data in instance variables** — any data stored in a field would be lost immediately after the request completes. This is why we use a separate `DataStore` class with **static `ConcurrentHashMap` fields**. The static fields belong to the class itself (not to any particular instance), so they persist for the entire lifetime of the application regardless of how many resource instances are created and destroyed.

We chose `ConcurrentHashMap` rather than a plain `HashMap` to handle **thread safety**. Since a web server handles multiple requests concurrently on different threads, and each thread gets its own resource instance but they all access the same shared static maps, we need a data structure that can safely handle simultaneous reads and writes without causing data corruption or race conditions.

**Q1.2 — Why is "Hypermedia" (HATEOAS) considered a hallmark of advanced RESTful design?**

HATEOAS (Hypermedia as the Engine of Application State) is the principle that API responses should include **links** to related resources and possible next actions. Our discovery endpoint at `GET /api/v1` demonstrates this by returning a `resources` map containing URLs for rooms and sensors.

This benefits client developers in several ways compared to static documentation:

1. **Discoverability** — Clients can navigate the API by following links from the root endpoint, rather than hard-coding URLs. If a URL structure changes, the server can update the links without breaking clients.
2. **Self-documentation** — The response itself tells the client what it can do next, reducing the dependency on external documentation that can become outdated.
3. **Loose coupling** — Clients that follow links dynamically are less tightly coupled to the server's URL structure. The server can evolve its URI scheme, and well-written clients will adapt automatically.
4. **Reduced errors** — Developers do not need to manually construct URLs (which is error-prone); they simply follow the links provided.

---

### Part 2: Room Management

**Q2.1 — When returning a list of rooms, what are the implications of returning only IDs versus returning full room objects?**

**Returning only IDs:**
- **Lower bandwidth** — The payload is significantly smaller, which is beneficial when the collection contains hundreds or thousands of rooms.
- **Extra round trips** — Clients must make additional `GET /rooms/{id}` requests for each room they need details about, increasing latency and server load.
- **Best for** — Scenarios where clients only need to reference rooms (e.g., populating a dropdown selector) or when bandwidth is a critical constraint (mobile networks).

**Returning full objects (our approach):**
- **Higher bandwidth** — The response payload is larger as it includes all fields (name, capacity, sensorIds) for every room.
- **Fewer round trips** — The client gets all the data it needs in a single request, which is far more efficient for rendering dashboards or room lists.
- **Best for** — Administrative dashboards where the client needs to display room details immediately.

In our implementation, we return full room objects because the data set is relatively small (campus rooms) and the primary use case is a facilities management dashboard that needs to display all room information at once. For a much larger data set, we might implement pagination and the option to return summary objects.

**Q2.2 — Is the DELETE operation idempotent in your implementation?**

Yes, our DELETE implementation is **idempotent in its observable effect**, which aligns with the HTTP specification.

- **First DELETE request** for an existing room with no sensors: The room is removed and a `204 No Content` is returned.
- **Second DELETE request** for the same room ID: The room no longer exists, so a `404 Not Found` is returned.
- **Third and subsequent DELETE requests**: The same `404 Not Found` is returned every time.

The key point is that the **end state of the server is the same** regardless of how many times the DELETE request is sent — the room does not exist. The response code changes from `204` to `404` after the first successful deletion, but the state of the resource (deleted/non-existent) remains consistent. This is the correct idempotent behaviour. The client can safely retry the DELETE request without causing unintended side effects.

If the room still has sensors assigned, the deletion is blocked with a `409 Conflict` every time, which is also idempotent because the state does not change.

---

### Part 3: Sensor Operations & Linking

**Q3.1 — What happens if a client sends data in `text/plain` or `application/xml` to an endpoint annotated with `@Consumes(MediaType.APPLICATION_JSON)`?**

When a method is annotated with `@Consumes(MediaType.APPLICATION_JSON)`, JAX-RS enforces strict content-type matching. If a client sends a request with `Content-Type: text/plain` or `Content-Type: application/xml`, the JAX-RS runtime will **automatically reject** the request before it ever reaches our method code.

The server returns an **HTTP 415 Unsupported Media Type** response. This response is generated by the Jersey framework itself, not by our application code. The framework inspects the `Content-Type` header of the incoming request and compares it against the media types declared in the `@Consumes` annotation. If there is no match, the request is rejected immediately.

This is an important safety mechanism because:
1. It prevents the JSON deserialiser (Jackson) from attempting to parse non-JSON data, which would cause unpredictable errors.
2. It gives clients a clear, standards-compliant error code (`415`) that explicitly tells them the format of their data is wrong.
3. It acts as a form of input validation at the framework level, reducing the amount of manual validation code we need to write.

**Q3.2 — Why is `@QueryParam` generally considered superior to path segments for filtering collections?**

We implemented filtering using `GET /api/v1/sensors?type=CO2` with `@QueryParam("type")`. The alternative would be a path-based approach like `GET /api/v1/sensors/type/CO2`.

The query parameter approach is superior for filtering for several reasons:

1. **Optional by nature** — Query parameters are inherently optional. If no `?type=` is provided, we return all sensors. With path segments, you need separate route definitions for the filtered and unfiltered versions.
2. **Composable** — Multiple filters can be easily combined: `?type=CO2&status=ACTIVE`. With path segments, this becomes unwieldy: `/sensors/type/CO2/status/ACTIVE`.
3. **RESTful semantics** — The URI path should identify **resources**, not filters. `/api/v1/sensors` identifies the sensor collection. Query parameters modify *how* that collection is presented (a view/filter), not *which resource* is being accessed.
4. **Caching clarity** — Caches and proxies understand that `/sensors?type=CO2` and `/sensors?type=Temperature` are different representations of the same resource collection, while `/sensors/type/CO2` would appear to be a completely different resource.
5. **Convention** — Major APIs (Google, GitHub, Twitter) all use query parameters for filtering, making it the approach that developers expect and understand.

---

### Part 4: Deep Nesting with Sub-Resources

**Q4.1 — Discuss the architectural benefits of the Sub-Resource Locator pattern.**

In our implementation, `SensorResource` contains a sub-resource locator method:

```java
@Path("/{sensorId}/readings")
public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
    return new SensorReadingResource(sensorId);
}
```

This method has **no HTTP method annotation** (`@GET`, `@POST`, etc.). Instead, it returns a new instance of `SensorReadingResource`, and JAX-RS delegates the request handling to that class.

The architectural benefits are significant:

1. **Separation of Concerns** — Sensor CRUD logic lives in `SensorResource`, and reading history logic lives in `SensorReadingResource`. Each class has a single, clear responsibility. Without this pattern, we would have one massive class handling `/sensors`, `/sensors/{id}`, `/sensors/{id}/readings`, and `/sensors/{id}/readings/{rid}` — all mixed together.

2. **Maintainability** — When we need to modify reading-related logic (e.g., adding pagination or date-range filtering), we only need to edit `SensorReadingResource`. There is no risk of accidentally breaking sensor CRUD logic.

3. **Reusability** — The `SensorReadingResource` class could potentially be reused if readings needed to be accessed through a different parent resource in the future.

4. **Scalability** — As the API grows, adding more nested resources (e.g., `/sensors/{id}/alerts`, `/sensors/{id}/calibration`) simply requires creating new sub-resource classes and adding new locator methods. The `SensorResource` class stays manageable.

5. **Dynamic context** — The locator method passes the `sensorId` to the sub-resource constructor, giving it the context it needs to operate. This is cleaner than having the sub-resource class extract path parameters from the full URI.

---

### Part 5: Advanced Error Handling, Exception Mapping & Logging

**Q5.2 — Why is HTTP 422 often considered more semantically accurate than 404 when the issue is a missing reference inside a valid JSON payload?**

When a client sends a `POST /api/v1/sensors` with a `roomId` that does not exist, the request itself is **syntactically valid JSON** and was sent to a **valid endpoint**. A `404 Not Found` would be misleading because:

- **404 means the target resource was not found** — But the target resource (`/api/v1/sensors`) exists and is perfectly reachable. The issue is not with the URL, but with the *content* of the request body.
- **422 Unprocessable Entity** means the server understands the content type, the syntax is valid, but the **semantic content is invalid** — which is exactly our scenario. The JSON is well-formed, but the `roomId` value references something that does not exist, making the request logically impossible to process.

Using `404` in this case would confuse clients into thinking they used the wrong URL, when in fact the problem is entirely with the data they submitted. `422` correctly communicates: "I received your request, I understood the format, but I cannot process it because the data is semantically invalid."

**Q5.4 — From a cybersecurity standpoint, what are the risks of exposing internal Java stack traces to external API consumers?**

Exposing raw stack traces is a serious security vulnerability known as **information leakage**. An attacker can gather the following from a Java stack trace:

1. **Technology stack** — The trace reveals the exact framework (Jersey, Tomcat), Java version, and libraries being used. Attackers can then search for known vulnerabilities (CVEs) specific to those versions.
2. **Internal class structure** — Package names like `com.smartcampus.store.DataStore` reveal the application's internal architecture, class names, and how the code is organised.
3. **File paths** — Stack traces often include file paths that reveal the server's operating system and directory structure.
4. **Business logic** — Method names and line numbers give clues about what the code does, helping attackers understand the application logic and find weaknesses.
5. **Database details** — If the error involves data access, stack traces can reveal table names, column names, query structures, and connection strings.

Our "catch-all" `ExceptionMapper<Throwable>` acts as a **global safety net** that intercepts any unexpected runtime exception and returns a generic `500 Internal Server Error` with a safe message like "An unexpected internal error occurred." The actual error details are logged server-side (visible only to developers) but never exposed to the client.

**Q5.5 — Why is it advantageous to use JAX-RS filters for logging rather than manually inserting `Logger.info()` in every resource method?**

Using JAX-RS filters (implementing `ContainerRequestFilter` and `ContainerResponseFilter`) for logging provides several key advantages over manual logging:

1. **DRY (Don't Repeat Yourself)** — A single filter class handles logging for **every** endpoint automatically. With manual logging, we would need to add `Logger.info()` calls to every single method in `RoomResource`, `SensorResource`, `SensorReadingResource`, and `DiscoveryResource` — that is repetitive, error-prone, and easy to forget.

2. **Cross-cutting concern** — Logging is a classic cross-cutting concern that applies uniformly across all endpoints. Filters are specifically designed for this purpose, keeping logging logic separate from business logic.

3. **Consistency** — The filter guarantees that every request and response is logged in the same format. With manual logging, different developers might log different information in different formats, making log analysis difficult.

4. **Maintainability** — If the logging format needs to change (e.g., adding a timestamp or request ID), we update one class instead of modifying every resource method across the entire application.

5. **Cannot be forgotten** — New endpoints automatically get logging without any extra effort. A developer adding a new resource class does not need to remember to add logging statements — the filter handles it.

---

## 5. Error Response Format

All error responses follow a consistent JSON structure:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Room LIB-301 still has active sensors assigned to it."
}
```

### Error Codes Summary

| HTTP Status | Exception | Scenario |
|-------------|-----------|----------|
| 400 Bad Request | — | Missing required fields in request body |
| 403 Forbidden | `SensorUnavailableException` | Posting a reading to a sensor in MAINTENANCE status |
| 404 Not Found | — | Room or sensor ID does not exist |
| 409 Conflict | `RoomNotEmptyException` | Deleting a room that still has sensors |
| 415 Unsupported Media Type | — (JAX-RS built-in) | Sending non-JSON content type |
| 422 Unprocessable Entity | `LinkedResourceNotFoundException` | Sensor references a non-existent roomId |
| 500 Internal Server Error | `Throwable` (catch-all) | Any unexpected runtime error |

---

## 6. Project Structure

```
SmartCampusAPI/
├── pom.xml
├── README.md
├── STUDY_PLAN.md
├── src/
│   └── main/
│       ├── java/com/smartcampus/
│       │   ├── app/
│       │   │   └── SmartCampusApplication.java
│       │   ├── model/
│       │   │   ├── Room.java
│       │   │   ├── Sensor.java
│       │   │   └── SensorReading.java
│       │   ├── store/
│       │   │   └── DataStore.java
│       │   ├── resource/
│       │   │   ├── DiscoveryResource.java
│       │   │   ├── RoomResource.java
│       │   │   ├── SensorResource.java
│       │   │   └── SensorReadingResource.java
│       │   ├── exception/
│       │   │   ├── RoomNotEmptyException.java
│       │   │   ├── LinkedResourceNotFoundException.java
│       │   │   ├── SensorUnavailableException.java
│       │   │   └── mappers/
│       │   │       ├── RoomNotEmptyExceptionMapper.java
│       │   │       ├── LinkedResourceNotFoundExceptionMapper.java
│       │   │       ├── SensorUnavailableExceptionMapper.java
│       │   │       └── GlobalExceptionMapper.java
│       │   └── filter/
│       │       └── LoggingFilter.java
│       └── webapp/WEB-INF/
│           └── web.xml
```

---

## 7. Technologies Used

| Technology | Purpose |
|------------|---------|
| Java 17 | Programming language |
| JAX-RS (Jersey 2.39.1) | RESTful API framework |
| Jackson | JSON serialisation/deserialisation |
| Apache Tomcat 9 | Servlet container |
| Apache Maven | Build tool and dependency management |
| ConcurrentHashMap | Thread-safe in-memory data storage |
| Postman | API testing |