# AI Reassignment Engine - Final Project Context

## 1. Final Implemented Features
- **Automatic Offline Detection**: Idempotent agent offline API triggers reassignment loop.
- **Reactive Re-planning**: Identifies affected active orders and safely generates assignment recommendations.
- **Strategy Engine**: Supports dynamic runtime switching between `RULE_BASED` and `AI` strategies via API.
- **AI Advisor**: Integrates with Gemini (`gemini-3.6-flash`) for intelligent recommendations with confidence scores and reasoning.
- **Approval Workflow**: Suggestions are persisted and can be Approved/Rejected, accurately mutating agent workload and order state.
- **Safe Fallback**: Transparently falls back to rule-based logic if AI fails or capacity is reached.

## 2. Final Architecture Summary
- **Backend**: Spring Boot 3 monolithic application using Spring Web, Spring Data JPA, and H2 in-memory DB. 
- **RoutingEngineManager**: Central strategy executor managing `RuleBasedRoutingStrategy` and `AIRoutingStrategy`.
- **LLMGateway**: Simple, dependency-free `RestClient`-based HTTP client for direct Gemini communication.
- **Event Flow**: Synchronous reactive pipeline triggering on `AgentService.markAgentOffline` -> `ReassignmentService`.
- **Frontend**: React 18 SPA interacting with standard REST JSON endpoints.

## 3. Final API Summary
- `GET /api/agents` - List all agents and their active capacities.
- `PUT /api/agents/{id}/offline` - Mark an agent offline (Triggers Reassignment).
- `GET /api/suggestions/pending` - Fetch all actionable suggestions.
- `POST /api/suggestions/{id}/approve` - Approve a suggestion, reassign order, update agent loads.
- `POST /api/suggestions/{id}/reject` - Reject a suggestion.
- `GET /api/strategy/active` - Get the current routing strategy.
- `PUT /api/strategy/active` - Update the strategy (`{"strategy": "AI"}`).

## 4. React UI Summary
- **Dashboard**: Displays a real-time list of Pending Suggestions alongside available Agents.
- **Actions**: Includes "Approve" and "Reject" buttons for suggestions.
- **Interaction**: Relies on API endpoints to fetch data and trigger state changes. Currently optimized for manual polling/refresh during demo.

## 5. Tests Passing
- `AutomaticReassignmentTest` (E2E Integration Test)
  - Successfully simulates agent going offline, order mapping, suggestion creation, and strategy enforcement.
- Unit tests for domain models, validation logic, and isolated service logic (where applicable).

## 6. Known Limitations
- **Synchronous AI Processing**: The `markAgentOffline` API call blocks synchronously while waiting for the LLM. In production, this should be async or event-driven.
- **Polling UI**: The frontend does not use WebSockets/SSE for live updates, requiring manual or timer-based refreshes.
- **Security**: Hardcoded DB logic; no auth/authorization built-in yet.

## 7. AI Failure/Fallback Behavior
- If `AIRoutingStrategy` throws an `AIAdvisorException` (e.g., API timeout, invalid format, missing model, quota exceeded), the `RoutingEngineManager` catches the exception.
- It logs a clear `WARN` message.
- It immediately falls back to executing the `RULE_BASED` strategy, guaranteeing the operations team still receives a deterministic suggestion without system crash.

## 8. Important Implementation Decisions
- **No Kafka/Brokers**: Kept the architecture strictly bounded to Spring synchronous events/transactions to minimize overhead for the hackathon.
- **API Key Security**: Moved the LLM key into a local-only `.gitignore` file (`application-secret.properties`).
- **REST vs SDK**: Chose `RestClient` for direct Gemini calls to maintain explicit control over prompt parsing and JSON safety without abstract frameworks (like Spring AI).

## 9. Remaining Risks
- **Race Conditions**: If two operators try to approve overlapping suggestions, the system currently lacks optimistic locking (`@Version`) on `Agent` capacity.
- **LLM Hallucination**: AI might provide highly confident reasoning for sub-optimal agent assignment if prompt bounds aren't continually refined.

## 10. Exact Demo Flow
1. **Setup Secrets**: Ensure `backend/application-secret.properties` contains `llm.api-key=YOUR_KEY`.
2. **Start Apps**: Run backend (`./mvnw spring-boot:run`) and frontend (`npm start`).
3. **Trigger Rule-Based**: With default `RULE_BASED` strategy active, hit `PUT /api/agents/AGT-005/offline` with `{"reason": "Flat tire"}`. 
4. **View Suggestions**: Check the UI/API for new rule-based suggestions with generic deterministic reasoning.
5. **Switch to AI**: Run `PUT /api/strategy/active` sending `{"strategy": "AI"}`.
6. **Trigger AI Reassignment**: Mark `AGT-001` offline with `PUT /api/agents/AGT-001/offline`.
7. **View AI Output**: Notice the newly generated suggestion with a rich LLM-generated `reasoning` text and specific `confidenceScore`.
8. **Approve**: Post to `POST /api/suggestions/{id}/approve` and verify the new agent's `activeOrderCount` goes up.
