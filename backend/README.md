# AI Reassignment Engine - Backend

This is the Spring Boot backend for the AI Reassignment Engine. It is built using Java 17 and Spring Boot 3.x, and handles state management, routing strategies (Rule-Based & AI), and exposes a REST API for the frontend.

## Prerequisites
- Java 17+ installed.
- Maven (a wrapper `./mvnw` is included).

## Setup Configuration
Before running the application, you **must** configure your LLM API Key.

1. In the `backend/` directory, create a new file named `application-secret.properties`. (This file is ignored by git).
2. Add your Gemini API key:
   ```properties
   llm.api-key=YOUR_ACTUAL_API_KEY
   ```

*Note: The application is pre-configured to use the `gemini-3.6-flash` model which requires an API key.*

## Running the Application
To run the Spring Boot server, use the included Maven wrapper from the `backend/` directory:

```bash
./mvnw spring-boot:run
```

The server will start on port `8080`.

## Database Access
The backend uses an in-memory **H2 Database**, seeded with mock data on startup. The data resets every time the application restarts.

You can view the raw tables and data using the built-in H2 Web Console:
- **URL**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:ziprun`
- **User Name**: `SA`
- **Password**: *(leave completely blank)*

## API Endpoints

### Agents
- `GET /api/agents` : List all agents and their active capacities.
- `PUT /api/agents/{id}/offline` : Mark an agent offline (Requires a JSON body: `{"reason": "string"}`). This triggers the reassignment logic.

### Suggestions
- `GET /api/suggestions/pending` : Fetch all actionable pending suggestions.
- `POST /api/suggestions/{id}/approve` : Approve a suggestion. Reassigns the order and updates agent active counts.
- `POST /api/suggestions/{id}/reject` : Reject a suggestion.

### Strategy Routing
- `GET /api/strategy/active` : Get the currently active routing strategy (`RULE_BASED` or `AI`).
- `PUT /api/strategy/active` : Update the strategy. Body: `{"strategy": "AI"}` or `{"strategy": "RULE_BASED"}`.
