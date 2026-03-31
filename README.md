# Notification Microservice

A Spring Boot microservice that handles sending notifications to users through multiple channels — Email, SMS, Push, and In-App. It stores notification records in AWS DynamoDB and uses a scheduler to automatically process and deliver pending notifications.

---

## Table of Contents

1. [Tech Stack](#tech-stack)
2. [Project Structure](#project-structure)
3. [How It Works](#how-it-works)
4. [Data Models](#data-models)
5. [API Endpoints](#api-endpoints)
6. [Channels](#channels)
7. [Scheduler](#scheduler)
8. [Configuration](#configuration)
9. [Running the App](#running-the-app)
10. [Running Tests](#running-tests)

---

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 17 | Programming language |
| Spring Boot 2.5.14 | Application framework |
| AWS DynamoDB (SDK v1) | Database |
| spring-data-dynamodb 5.2.5 | DynamoDB repository support |
| JavaMailSender | Email delivery |
| Twilio SDK 9.14.0 | SMS delivery |
| Lombok | Reduces boilerplate code |
| JUnit 5 + Mockito | Unit testing |

---

## Project Structure

```
src/main/java/
│
├── org/Enrollment/
│   └── Main.java                     # Entry point — starts the Spring Boot app
│
├── model/
│   ├── Notification.java             # Main notification entity (stored in DynamoDB)
│   ├── InAppNotification.java        # In-app specific notification entity
│   └── enums/
│       ├── ChannelType.java          # EMAIL, SMS, PUSH, IN_APP
│       ├── RoleType.java             # LEARNER, TRAINER, ADMIN
│       └── NotificationType.java     # COURSE_ALERT, BADGE_UPDATE, etc.
│
├── controller/
│   ├── NotificationController.java   # REST API for creating and fetching notifications
│   ├── InAppController.java          # REST API for in-app notification management
│   └── GlobalExceptionHandler.java   # Handles validation errors globally
│
├── service/
│   ├── NotificationService.java      # Business logic — create and fetch notifications
│   └── ChannelDispatcherService.java # Routes notification to the correct channel
│
├── channel/
│   ├── NotificationChannel.java      # Interface all channels implement
│   ├── EmailChannel.java             # Sends email via JavaMailSender
│   ├── SmsChannel.java               # Sends SMS via Twilio
│   ├── PushChannel.java              # Sends push notification via device token
│   └── InAppChannel.java             # Saves in-app notification to DynamoDB
│
├── repository/
│   ├── NotificationRepository.java   # DynamoDB repository for Notification
│   └── InAppRepository.java          # DynamoDB repository for InAppNotification
│
├── scheduler/
│   └── NotificationScheduler.java    # Polls DynamoDB and dispatches PENDING notifications
│
├── config/
│   └── DynamoDBConfig.java           # Configures and creates AmazonDynamoDB bean
│
└── dto/
    ├── NotificationRequestDTO.java   # Request body for creating a notification
    └── NotificationResponseDTO.java  # Response body returned to client

src/main/resources/
└── application.yml                   # All app configuration (AWS, mail, Twilio, scheduler)

src/test/java/                        # Unit tests for all layers
```

---

## How It Works

```
Client
  │
  ▼
NotificationController  ──►  NotificationService  ──►  NotificationRepository (DynamoDB)
                                                              │
                                                              ▼
                                                    NotificationScheduler (every 10s)
                                                              │
                                                              ▼
                                                  ChannelDispatcherService
                                                    /      |      |      \
                                                EMAIL    SMS    PUSH   IN_APP
                                                  │       │       │       │
                                             JavaMail  Twilio  Device  DynamoDB
                                                               Token  (InAppNotification)
```

Step by step:

1. Client sends a `POST /api/notifications` request with notification details
2. `NotificationService` generates a unique ID, sets status to `PENDING`, saves to DynamoDB
3. `NotificationScheduler` runs every 10 seconds, picks up all `PENDING` notifications
4. For each pending notification, it calls `ChannelDispatcherService.dispatch()`
5. Dispatcher looks up the correct channel (EMAIL, SMS, PUSH, IN_APP) and calls `send()`
6. On success — status is updated to `SENT`
7. On failure — retry count increments. After max retries (3), status is set to `FAILED`

---

## Data Models

### Notification (DynamoDB table: `notifications`)

| Field | Type | Required | Description |
|---|---|---|---|
| notificationId | String | Yes (PK) | Auto-generated UUID, partition key |
| userId | String | Yes | Target user ID |
| channel | ChannelType | Yes | EMAIL, SMS, PUSH, IN_APP |
| role | RoleType | Yes | LEARNER, TRAINER, ADMIN |
| type | NotificationType | No | Type of notification event |
| message | String | Yes | Notification content |
| status | String | Yes | PENDING, SENT, FAILED |
| email | String | No | Required if channel is EMAIL |
| phoneNumber | String | No | Required if channel is SMS |
| deviceToken | String | No | Required if channel is PUSH |
| isScheduled | Boolean | No | Whether delivery is scheduled for later |
| scheduledTime | Long | No | Epoch ms — when to deliver if scheduled |
| retryCount | int | No | Number of failed delivery attempts |
| createdAt | Long | No | Epoch ms when notification was created |
| isRead | Boolean | No | Whether notification has been read |

### InAppNotification (DynamoDB table: `InAppNotification`)

| Field | Type | Description |
|---|---|---|
| id | String (PK) | Auto-generated UUID |
| userId | String | Target user ID |
| message | String | Notification content |
| status | String | SENT |
| createdAt | long | Epoch ms timestamp |
| isRead | boolean | Read status, defaults to false |

### Enums

**ChannelType** — `EMAIL`, `SMS`, `PUSH`, `IN_APP`

**RoleType** — `LEARNER`, `TRAINER`, `ADMIN`

**NotificationType** — `COURSE_ALERT`, `TRAINER_LIVE`, `STREAK_ALERT`, `FEEDBACK_ALERT`, `SESSION_REMINDER`, `BADGE_UPDATE`, `ADMIN_BROADCAST`, `REWARD_REMINDER`

---

## API Endpoints

### Notification Endpoints

| Method | URL | Description |
|---|---|---|
| POST | `/api/notifications` | Create a new notification |
| GET | `/api/notifications/{userId}` | Get all notifications for a user |

#### POST `/api/notifications` — Request Body

```json
{
  "userId": "user123",
  "channel": "EMAIL",
  "role": "LEARNER",
  "type": "COURSE_ALERT",
  "message": "Your course has been completed",
  "email": "user@example.com",
  "status": "PENDING"
}
```

#### POST `/api/notifications` — Response (201 Created)

```json
{
  "notificationId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "user123",
  "channel": "EMAIL",
  "status": "PENDING",
  "createdAt": 1711234567890
}
```

#### Validation Rules

If any required field is missing, the API returns `400 Bad Request`:

```json
{
  "userId": "userId is required",
  "channel": "channel is required"
}
```

---

### In-App Notification Endpoints

| Method | URL | Description |
|---|---|---|
| GET | `/api/inapps/{userId}` | Get all in-app notifications for a user (sorted latest first) |
| GET | `/api/inapps/one/{id}` | Get a single in-app notification by ID |
| GET | `/api/inapps/unread/{userId}` | Get all unread notifications for a user |
| GET | `/api/inapps/unread/count/{userId}` | Get count of unread notifications |
| PUT | `/api/inapps/read/{id}` | Mark a notification as read |
| DELETE | `/api/inapps/{id}` | Delete a notification |

---

## Channels

Each channel implements the `NotificationChannel` interface with two methods:
- `getChannelName()` — returns the channel identifier
- `send(Notification n)` — delivers the notification

### EmailChannel
- Uses Spring's `JavaMailSender`
- Requires `email` field to be set on the notification
- Throws `RuntimeException` if email is null or empty

### SmsChannel
- Uses Twilio SDK
- Requires `phoneNumber` field to be set on the notification
- Throws `RuntimeException` if phone number is null or empty

### PushChannel
- Sends push notification using device token
- Requires `deviceToken` field to be set on the notification
- Throws `RuntimeException` if device token is null or empty

### InAppChannel
- Saves an `InAppNotification` record to DynamoDB
- Requires `userId` to be set on the notification
- Sets `isRead = false` and `status = SENT` on save

---

## Scheduler

`NotificationScheduler` runs automatically every 10 seconds (configurable).

Logic:
1. Fetches all notifications from DynamoDB
2. Filters for `status = PENDING`
3. If `isScheduled = false` — dispatches immediately
4. If `isScheduled = true` — only dispatches if `scheduledTime <= now`
5. On success — sets `status = SENT`
6. On failure — increments `retryCount`. Once `retryCount >= maxRetry (3)`, sets `status = FAILED`

`ChannelDispatcherService` uses the Strategy pattern — it builds a map of channel name to channel implementation at startup, then looks up and calls the right one at dispatch time. It also has built-in retry logic with a 2 second delay between attempts.

---

## Configuration

All configuration is in `src/main/resources/application.yml`:

```yaml
aws:
  region: us-east-1
  accessKeyId: your-key
  secretKey: your-secret
  dynamodb:
    endpoint: http://localhost:8000   # remove for production AWS

spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email
    password: your-password

twilio:
  account-sid: your-sid
  auth-token: your-token
  phone-number: +1xxxxxxxxxx

notification:
  scheduler:
    interval: 10000     # milliseconds between scheduler runs
    max-retry: 3        # max delivery attempts before marking FAILED
```

---

## Running the App

Make sure DynamoDB Local is running on port 8000 (or update the endpoint in yml), then run:

```bash
mvn spring-boot:run
```

App starts on `http://localhost:8080`

---

## Running Tests

```bash
mvn test
```

### Test Coverage

| Layer | Test File | What is Tested |
|---|---|---|
| Service | NotificationServiceTest | create sets fields, unique IDs, getByUser |
| Dispatcher | ChannelDispatcherServiceTest | all 4 channels route correctly, retry on failure |
| Controller | NotificationControllerTest | POST 201, GET list, GET empty |
| InApp Controller | InAppControllerTest | GET sorted, mark read, unread, delete, get one |
| InApp Channel | InAppChannelTest | saves correctly, throws on null userId, unique IDs |
| Email Channel | EmailChannelTest | sends email, throws on null/empty email |
| Push Channel | PushChannelTest | throws on null/empty device token |
| SMS Channel | SmsChannelTest | throws on null/empty phone number |
| Scheduler | NotificationSchedulerTest | dispatch, skip future, mark failed, retry count |
| Notification Model | NotificationTest | builder, setters, equals |
| InApp Model | InAppNotificationTest | getters/setters, isRead default |
| Notification Repo | NotificationRepositoryTest | save, findByUserId, findById, delete |
| InApp Repo | InAppRepositoryTest | save, findByUserId, findByUserIdAndIsRead, findById, delete |
