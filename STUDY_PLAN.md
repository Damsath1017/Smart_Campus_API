# 📅 Smart Campus API — 5-Day Coursework Plan

**Module:** 5COSC022W — Client-Server Architectures  
**Due:** 24th April 2026, 13:00  
**Stack:** Java · JAX-RS (Jersey) · Apache Tomcat · Maven · In-Memory (HashMap/ArrayList)  
**Testing:** Postman  
**Submission:** Public GitHub repo + PDF Report + Video Demo (Blackboard)

---

## 🗺️ Overview

| Day | Date        | Focus                                         | Marks Covered |
|-----|-------------|-----------------------------------------------|---------------|
| 1   | 17 Apr (Thu)| Project Setup + Part 1 (Architecture & Discovery) | 10 marks  |
| 2   | 18 Apr (Fri)| Part 2 — Room Management (CRUD + Delete Safety) | 20 marks   |
| 3   | 19 Apr (Sat)| Part 3 — Sensor Operations & Filtering         | 20 marks      |
| 4   | 20 Apr (Sun)| Part 4 — Sub-Resources & Sensor Readings        | 20 marks      |
| 5   | 21 Apr (Mon)| Part 5 — Error Handling, Logging + README + Polish | 30 marks  |

> ⚠️ Days 22–24 Apr are kept as buffer for fixing bugs, recording the video demo, and writing the PDF report.

---

## 📁 Final Project Structure (Target)

```
SmartCampusAPI/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/smartcampus/
│       │       ├── app/
│       │       │   └── SmartCampusApplication.java    ← @ApplicationPath("/api/v1")
│       │       ├── model/
│       │       │   ├── Room.java
│       │       │   ├── Sensor.java
│       │       │   └── SensorReading.java
│       │       ├── store/
│       │       │   └── DataStore.java                 ← Singleton HashMap store
│       │       ├── resource/
│       │       │   ├── DiscoveryResource.java
│       │       │   ├── RoomResource.java
│       │       │   ├── SensorResource.java
│       │       │   └── SensorReadingResource.java
│       │       ├── exception/
│       │       │   ├── RoomNotEmptyException.java
│       │       │   ├── LinkedResourceNotFoundException.java
│       │       │   ├── SensorUnavailableException.java
│       │       │   └── mappers/
│       │       │       ├── RoomNotEmptyExceptionMapper.java
│       │       │       ├── LinkedResourceNotFoundExceptionMapper.java
│       │       │       ├── SensorUnavailableExceptionMapper.java
│       │       │       └── GlobalExceptionMapper.java
│       │       └── filter/
│       │           └── LoggingFilter.java
│       └── webapp/
│           └── WEB-INF/
│               └── web.xml
├── pom.xml
├── README.md
└── STUDY_PLAN.md
```

---

## 🗓️ Day 1 — Thursday, 17 April
### Part 1: Project Setup & Service Architecture (10 Marks)

**Goal:** Get a working Maven + Jersey WAR project that deploys on Tomcat and returns JSON from the discovery endpoint.

### ✅ Tasks

#### 1. Maven Project Setup (`pom.xml`)
- Create a Maven WAR project
- Add dependencies:
  - `jersey-container-servlet` (JAX-RS implementation)
  - `jersey-media-json-jackson` (JSON support via Jackson)
  - `javax.ws.rs-api` (JAX-RS API)
- Set `packaging` to `war`
- Set Java version to 11+

#### 2. `web.xml` Configuration
- Register Jersey's `ServletContainer` as the servlet
- Point it to your `@ApplicationPath` class
- This is how Tomcat knows to route requests through JAX-RS

#### 3. `SmartCampusApplication.java`
- Extend `javax.ws.rs.core.Application`
- Annotate with `@ApplicationPath("/api/v1")`
- Register all resource classes

#### 4. Data Models (POJOs)
Create three model classes with **private fields + getters/setters**:
- `Room` — id, name, capacity, sensorIds (List\<String\>)
- `Sensor` — id, type, status, currentValue, roomId
- `SensorReading` — id, timestamp, value

#### 5. `DataStore.java` — Singleton In-Memory Store
- Use a **static** class (or enum singleton) with:
  - `Map<String, Room> rooms = new ConcurrentHashMap<>()`
  - `Map<String, Sensor> sensors = new ConcurrentHashMap<>()`
  - `Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>()`
- Pre-populate with 2–3 sample rooms and sensors for testing

#### 6. `DiscoveryResource.java`
- `GET /api/v1` → returns JSON with:
  - API version
  - Admin contact
  - Links map: `{"rooms": "/api/v1/rooms", "sensors": "/api/v1/sensors"}`

#### 7. Deploy & Smoke Test
- Run `mvn clean package` → produces `SmartCampusAPI.war`
- Copy WAR to `<TOMCAT_HOME>/webapps/`
- Start Tomcat, hit `GET http://localhost:8080/SmartCampusAPI/api/v1` in Postman
- Confirm JSON response ✅

### 📝 Report Question (Part 1)
Answer in README.md:
- **Q1.1:** Default JAX-RS lifecycle (per-request vs singleton) and how it affects in-memory data management
- **Q1.2:** What is HATEOAS and why does it benefit API clients over static docs?

### 💾 Git Commit
```
git add .
git commit -m "Day 1: Maven setup, models, DataStore, discovery endpoint"
git push
```

---

## 🗓️ Day 2 — Friday, 18 April
### Part 2: Room Management (20 Marks)

**Goal:** Full CRUD for rooms with safety logic preventing deletion of rooms with sensors.

### ✅ Tasks

#### 1. `RoomResource.java` at `/api/v1/rooms`

| Method | Path           | Description                              | Status Codes    |
|--------|----------------|------------------------------------------|-----------------|
| GET    | `/`            | Return all rooms as JSON array           | 200             |
| POST   | `/`            | Create a new room (body: JSON Room)      | 201 Created     |
| GET    | `/{roomId}`    | Return a specific room by ID             | 200 / 404       |
| DELETE | `/{roomId}`    | Delete room (blocked if sensors exist)   | 204 / 404 / 409 |

#### 2. DELETE Safety Logic
```
if room has sensors in sensorIds list:
    throw new RoomNotEmptyException(roomId)
else:
    remove from DataStore
    return 204 No Content
```

#### 3. POST Room
- Read `Room` from JSON body (`@Consumes(MediaType.APPLICATION_JSON)`)
- Validate id is unique
- Add to `DataStore.rooms`
- Return `201 Created` with `Location` header pointing to `/api/v1/rooms/{id}`

### 📝 Report Question (Part 2)
- **Q2.1:** IDs vs full objects in list responses — tradeoffs (bandwidth vs client processing)
- **Q2.2:** Is DELETE idempotent? What happens on second DELETE of same room?

### 💾 Git Commit
```
git add .
git commit -m "Day 2: Room resource - GET, POST, GET by ID, DELETE with safety check"
git push
```

---

## 🗓️ Day 3 — Saturday, 19 April
### Part 3: Sensor Operations & Filtering (20 Marks)

**Goal:** Sensor CRUD with room-link validation and type-based filtering.

### ✅ Tasks

#### 1. `SensorResource.java` at `/api/v1/sensors`

| Method | Path    | Description                                         | Status Codes     |
|--------|---------|-----------------------------------------------------|------------------|
| GET    | `/`     | List all sensors (optional `?type=CO2` filter)      | 200              |
| POST   | `/`     | Register sensor — validate roomId exists first       | 201 / 422        |
| GET    | `/{id}` | Get a specific sensor                               | 200 / 404        |

#### 2. POST Sensor — Room Validation
```
if DataStore.rooms does NOT contain sensor.roomId:
    throw new LinkedResourceNotFoundException(sensor.roomId)
else:
    save sensor
    add sensor.id to room.sensorIds
    return 201 Created
```

#### 3. GET with `?type=` Filter
```java
@QueryParam("type") String type
// if type != null → filter sensors by type
// if type == null → return all
```

### 📝 Report Question (Part 3)
- **Q3.1:** What happens when client sends `Content-Type: text/plain` to a `@Consumes(APPLICATION_JSON)` endpoint?
- **Q3.2:** `@QueryParam` vs path segment (`/sensors/type/CO2`) — why is query param better for filtering?

### 💾 Git Commit
```
git add .
git commit -m "Day 3: Sensor resource - CRUD, room validation, type filtering"
git push
```

---

## 🗓️ Day 4 — Sunday, 20 April
### Part 4: Sub-Resources & Sensor Readings (20 Marks)

**Goal:** Implement the sub-resource locator pattern and full readings history management.

### ✅ Tasks

#### 1. Sub-Resource Locator in `SensorResource.java`
```java
@Path("{sensorId}/readings")
public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
    return new SensorReadingResource(sensorId);
}
```
> ⚠️ No HTTP method annotation on this method — that's the locator pattern!

#### 2. `SensorReadingResource.java`
Manages `/api/v1/sensors/{sensorId}/readings`

| Method | Path    | Description                                          | Status Codes  |
|--------|---------|------------------------------------------------------|---------------|
| GET    | `/`     | Fetch all historical readings for this sensor        | 200 / 404     |
| POST   | `/`     | Append a new reading + update sensor's `currentValue`| 201 / 403/404 |

#### 3. POST Reading — Side Effects & Validation
```
1. Check sensor exists → 404 if not
2. Check sensor.status == "MAINTENANCE" → throw SensorUnavailableException (403)
3. Generate UUID for reading.id, set timestamp = System.currentTimeMillis()
4. Add reading to DataStore.readings.get(sensorId)
5. Update sensor.currentValue = reading.value   ← IMPORTANT SIDE EFFECT
6. Return 201 Created
```

### 📝 Report Question (Part 4)
- **Q4.1:** Benefits of Sub-Resource Locator pattern vs putting all nested paths in one massive resource class

### 💾 Git Commit
```
git add .
git commit -m "Day 4: Sub-resource locator, SensorReadingResource, currentValue update"
git push
```

---

## 🗓️ Day 5 — Monday, 21 April
### Part 5: Error Handling, Exception Mapping & Logging (30 Marks)

**Goal:** Make the API bulletproof — no raw stack traces ever, proper HTTP error codes, and request/response logging.

### ✅ Tasks

#### 1. Custom Exceptions
Create these exception classes (they just extend `RuntimeException`):
- `RoomNotEmptyException` — thrown when DELETE room has sensors
- `LinkedResourceNotFoundException` — thrown when sensor's roomId not found
- `SensorUnavailableException` — thrown when posting reading to MAINTENANCE sensor

#### 2. Exception Mappers
Each mapper implements `ExceptionMapper<YourException>` and is annotated `@Provider`:

| Exception                        | HTTP Status | JSON Body Message                                  |
|----------------------------------|-------------|----------------------------------------------------|
| `RoomNotEmptyException`          | 409 Conflict| "Room {id} still has active sensors assigned."     |
| `LinkedResourceNotFoundException`| 422 Unprocessable Entity | "Referenced room '{id}' does not exist." |
| `SensorUnavailableException`     | 403 Forbidden | "Sensor is under MAINTENANCE. Cannot post reading." |
| `Throwable` (catch-all)          | 500 Internal Server Error | "An unexpected error occurred."      |

JSON error body format:
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Room LIB-301 still has active sensors assigned."
}
```

#### 3. `LoggingFilter.java`
- Implements `ContainerRequestFilter` AND `ContainerResponseFilter`
- Annotate with `@Provider`
- Log on **request**: `[REQUEST] METHOD URI`
- Log on **response**: `[RESPONSE] Status: 201`

```java
private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

@Override
public void filter(ContainerRequestContext req) {
    LOGGER.info("[REQUEST] " + req.getMethod() + " " + req.getUriInfo().getRequestUri());
}

@Override
public void filter(ContainerRequestContext req, ContainerResponseContext res) {
    LOGGER.info("[RESPONSE] Status: " + res.getStatus());
}
```

#### 4. Final Postman Testing
Test every endpoint, confirm:
- ✅ All success cases return correct status codes
- ✅ All error cases return JSON (never HTML stack traces)
- ✅ Logs appear in Tomcat console

#### 5. README.md — Complete It
Must include:
- API overview
- Build & run instructions (mvn package → copy WAR → start Tomcat)
- At least 5 `curl` example commands

### 📝 Report Questions (Part 5)
- **Q5.2:** Why is 422 more semantically accurate than 404 for a missing roomId reference?
- **Q5.4:** Security risks of exposing Java stack traces to clients
- **Q5.5:** Why use JAX-RS filters for logging vs manual Logger calls in every method?

### 💾 Git Commit
```
git add .
git commit -m "Day 5: Exception mappers (409/422/403/500), logging filter, README complete"
git push
```

---

## 🛡️ Buffer Days — 22–23 April

| Day | Task |
|-----|------|
| 22 Apr (Wed) | Final end-to-end Postman testing, fix any remaining bugs, clean up code |
| 23 Apr (Thu) | Record 10-min video demo (Postman walkthrough, speak clearly, camera on), write PDF report |
| 24 Apr (Fri) | Submit by **13:00** — GitHub link + video + PDF on Blackboard |

---

## 🔧 Tomcat Deployment Workflow (Every Day)

```bash
# 1. Build the WAR
mvn clean package

# 2. Copy to Tomcat
copy target\SmartCampusAPI.war C:\tomcat\webapps\

# 3. Start Tomcat (if not running)
C:\tomcat\bin\startup.bat

# 4. Test base URL
GET http://localhost:8080/SmartCampusAPI/api/v1
```

---

## 📌 Key Reminders

- ❌ No Spring Boot — immediate zero
- ❌ No databases (SQL/MongoDB) — immediate zero
- ❌ No ZIP file submission — immediate zero  
- ✅ Only JAX-RS (Jersey) + Tomcat + In-memory (HashMap/ArrayList)
- ✅ GitHub must be **public**
- ✅ Video: max 10 min, camera + mic on, Postman demo
- ✅ Report: PDF, only answers to questions, in README.md on GitHub

---

## 📊 Mark Allocation Summary

| Part | Topic | Coding (50%) | Video (30%) | Report Q (20%) |
|------|-------|-------------|-------------|----------------|
| 1    | Setup & Discovery | 5 | 3 | 2 |
| 2    | Room Management | 10 | 6 | 4 |
| 3    | Sensor Operations | 10 | 6 | 4 |
| 4    | Sub-Resources | 10 | 6 | 4 |
| 5    | Error Handling & Logging | 15 | 9 | 6 |
| **Total** | | **50** | **30** | **20** |
