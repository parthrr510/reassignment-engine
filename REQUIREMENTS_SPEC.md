# Requirements Specification: AI Reassignment Engine

## 1. Primary User
Operations Manager (Ops)

## 2. User Goal
To quickly, accurately, and confidently reassign deliveries when an agent unexpectedly becomes unavailable during their shift, minimizing delivery delays and manual cognitive load.

## 3. Main User Journey
1. Agent goes offline unexpectedly.
2. System detects offline status, identifies affected orders, and automatically calculates the best reassignment.
3. Ops user sees a notification/queue of a pending reassignment suggestion.
4. Ops user reviews the AI's recommendation, including the confidence score and plain-English reasoning.
5. Ops user clicks "Approve".
6. The system updates the order's assigned agent and state.

---

## 4. Functional Requirements
**REQ-FUNC-01: Agent Status Update**
- **Description**: System provides an endpoint/mechanism to change an agent's status to OFFLINE.
- **Priority**: MUST HAVE
- **Complexity**: LOW
- **Acceptance Criteria**: Changing an agent's status successfully updates the database and emits an event/trigger for the reassignment loop.

**REQ-FUNC-02: Affected Order Identification**
- **Description**: Automatically query and identify all incomplete orders assigned to an agent who just went offline.
- **Priority**: MUST HAVE
- **Complexity**: LOW
- **Acceptance Criteria**: System retrieves a list of all ASSIGNED orders for the specified agent ID.

## 5. Non-Functional Requirements
**REQ-NFR-01: Response Time**
- **Description**: AI reassignment generation should take no longer than a few seconds per order to ensure Ops is not kept waiting.
- **Priority**: SHOULD HAVE
- **Complexity**: MEDIUM
- **Acceptance Criteria**: The AI API responds within a reasonable timeout; rule-based executes in <100ms.

## 6. Domain Requirements
**REQ-DOM-01: Clean Domain Model**
- **Description**: Create self-contained entities for Agent, Order, and ReassignmentSuggestion.
- **Priority**: MUST HAVE
- **Complexity**: LOW
- **Acceptance Criteria**: Entities have clear boundaries, encapsulate their own state, and do not hold unnecessary external references.

## 7. AI Requirements
**REQ-AI-01: Gemini Integration**
- **Description**: Integrate with Gemini API to generate reassignment recommendations.
- **Priority**: MUST HAVE
- **Complexity**: MEDIUM
- **Acceptance Criteria**: System can send a prompt containing order details and agent roster, and parse a JSON structured response.

**REQ-AI-02: Structured AI Output**
- **Description**: AI must return a specific agent ID, a confidence score (0-100), and plain-English reasoning.
- **Priority**: MUST HAVE
- **Complexity**: MEDIUM
- **Acceptance Criteria**: The parsed AI response maps strictly to the ReassignmentSuggestion entity.

## 8. Routing Requirements
**REQ-ROUT-01: Pluggable Strategy Interface**
- **Description**: Routing logic must be abstracted behind an interface (e.g., `RoutingStrategy`).
- **Priority**: MUST HAVE
- **Complexity**: LOW
- **Acceptance Criteria**: Both Rule-based and AI-based classes implement the same interface.

**REQ-ROUT-02: Rule-Based Fallback**
- **Description**: A basic rule-based strategy (e.g., lowest active order count) to serve as a baseline/alternative.
- **Priority**: MUST HAVE
- **Complexity**: LOW
- **Acceptance Criteria**: Given a list of agents, the system accurately picks the one with the least load.

## 9. Reassignment Requirements
**REQ-REAS-01: Automated Replanning Loop**
- **Description**: The detection of an offline agent automatically triggers the routing strategy for affected orders without human intervention.
- **Priority**: MUST HAVE
- **Complexity**: HIGH
- **Acceptance Criteria**: An offline event immediately generates PENDING ReassignmentSuggestions in the database.

## 10. Operations UI Requirements
**REQ-UI-01: Pending Suggestions Dashboard**
- **Description**: A minimal React 18 UI displaying a queue of pending reassignments.
- **Priority**: MUST HAVE
- **Complexity**: MEDIUM
- **Acceptance Criteria**: UI fetches and displays suggestions showing the order, recommended agent, score, and reasoning.

**REQ-UI-02: Approve/Reject Actions**
- **Description**: Buttons to approve or reject a pending suggestion.
- **Priority**: MUST HAVE
- **Complexity**: LOW
- **Acceptance Criteria**: Clicking approve changes the suggestion status to APPROVED and updates the order's agent.

## 11. State Transition Requirements
**REQ-STAT-01: Entity State Machines**
- **Description**: Use explicit Enums for Agent (AVAILABLE, BUSY, OFFLINE), Order (UNASSIGNED, ASSIGNED, COMPLETED), and Suggestion (PENDING, APPROVED, REJECTED).
- **Priority**: MUST HAVE
- **Complexity**: LOW
- **Acceptance Criteria**: System manages valid state transitions.

## 12. Error/Failure Requirements
**REQ-ERR-01: LLM Fallback**
- **Description**: If the Gemini API fails or times out, the system must either surface an error gracefully or fall back.
- **Priority**: SHOULD HAVE
- **Complexity**: MEDIUM
- **Acceptance Criteria**: An HTTP error from Gemini results in either a logged error and pending retry, or an immediate rule-based suggestion.

## 13. Runtime Strategy-Switching Requirements
**REQ-SWIT-01: Dynamic Strategy Toggle**
- **Description**: Ability to switch the active routing strategy at runtime without restarting the Spring Boot server.
- **Priority**: MUST HAVE
- **Complexity**: MEDIUM
- **Acceptance Criteria**: An API endpoint toggles the active strategy flag; subsequent re-planning loops use the new strategy immediately.

---

## A. Smallest Viable MVP
1. REST endpoint to mark an agent OFFLINE.
2. Synchronous service that finds the agent's orders and generates AI suggestions via Gemini.
3. React UI that polls for pending suggestions and displays them.
4. "Approve" button that updates the order's agent in the database.

## B. Explicitly Excluded Features
- Full dispatch and routing maps.
- Real-time WebSockets (polling is acceptable for 2.5hrs).
- Order creation and lifecycle outside of reassignment.
- Complex user authentication.
- Agent mobile app interfaces.

## C. End-to-End Acceptance Criteria
1. Given 5 agents (1 OFFLINE, 4 AVAILABLE) and 2 orders assigned to the OFFLINE agent.
2. When the agent is marked OFFLINE via API/UI.
3. Then 2 PENDING suggestions are automatically generated by the active strategy.
4. When Ops views the UI, they see the 2 suggestions with AI reasoning.
5. When Ops approves one, the order is updated to the newly assigned agent.

## D. Top 5 Priorities for the Demo
1. **The Automated Loop**: Show an agent going offline and suggestions appearing instantly without manually triggering a "replan" button.
2. **AI Reasoning**: Highlight the plain-English explanation and confidence score provided by Gemini.
3. **Runtime Strategy Switch**: Demonstrate flipping a toggle and showing how the fallback Rule-based strategy picks differently than the AI.
4. **Clean Code & Domain**: Briefly show the `RoutingStrategy` interface and Enum state machines in the IDE.
5. **UI Simplicity**: Show the Ops manager clicking a single "Approve" button to resolve a complex logistical issue.
