# Architecture Decision Records (ADR)

This document captures the architectural decisions made for the Reassignment Engine during the 2.5-hour hackathon. 

## 1. Modular Monolith

**Context:** The application needs a backend to handle order reassignment logic, REST APIs, and database interactions within a constrained timeframe.
**Decision:** We chose a modular monolith architecture using Spring Boot (single application).
**Alternatives:** Microservices architecture, Serverless functions.
**Why:** A monolith minimizes deployment complexity, eliminates network latency between internal components, and allows for the fastest possible iteration speed during a hackathon. Microservices would introduce unnecessary overhead for cross-service communication and infrastructure setup.
**Consequences:** The codebase is easy to navigate and test as a single unit. Scaling specific parts independently is harder, but this is acceptable for an MVP/hackathon.

## 2. Domain/State Model

**Context:** The engine needs to track the state of agents, orders, and reassignment suggestions.
**Decision:** We implemented a rich domain model using JPA/Hibernate (`Agent`, `Order`, `ReassignmentSuggestion`) with enums for state (`AgentStatus`, `OrderStatus`, `SuggestionStatus`).
**Alternatives:** Event Sourcing, Anemic domain model with raw SQL.
**Why:** JPA allows for rapid object-relational mapping and leverages Spring Data repositories to reduce boilerplate code. The domain model provides a clear structure for the reassignment lifecycle (e.g., tracking a suggestion from `PENDING` to `APPROVED` or `REJECTED`).
**Consequences:** Rapid development of database queries. However, JPA can introduce overhead and complexity with lazy loading and transaction management, which requires careful handling in the service layer.

## 3. RoutingStrategy Abstraction

**Context:** The system needs a way to evaluate which agent should receive a reassigned order. We want to support both simple rules and AI.
**Decision:** We introduced a `RoutingStrategy` interface with a `route(RoutingRequest)` method, returning a `RoutingRecommendation`.
**Alternatives:** Hardcoding routing logic within the `ReassignmentService`.
**Why:** The Strategy pattern decouples the core reassignment workflow from the specific algorithm used to pick an agent. It enables easy swapping, testing, and side-by-side comparison of different routing mechanisms.
**Consequences:** Adding new routing algorithms requires creating a new class implementing the interface, leaving the core service untouched.

## 4. Rule-Based Baseline

**Context:** We need a fallback and a baseline for comparing the AI's performance.
**Decision:** We implemented a `RuleBasedRoutingStrategy` that simply assigns the order to the available agent with the lowest active order count (resolving ties by ID).
**Alternatives:** Random assignment, geographic proximity assignment (too complex for MVP).
**Why:** It provides a deterministic, easy-to-understand, and fast fallback mechanism. It sets a baseline to see if the AI can make "smarter" decisions than a simple greedy approach.
**Consequences:** Extremely fast execution and 100% reliability. However, it lacks the contextual understanding that an AI might provide (e.g., considering the specific type of order or failure reason).

## 5. AI Routing

**Context:** The core goal is to leverage LLMs to make intelligent reassignment decisions based on the situation context.
**Decision:** We implemented an `AIRoutingStrategy` that delegates to an `AIAdvisorService` to build a contextual prompt and fetch a recommendation from Gemini.
**Alternatives:** Training a custom ML model, using a rules engine.
**Why:** LLMs can process unstructured situational context (like the reason an agent went offline) and reason about the best choice without rigid rule definitions. It fits the hackathon's AI-driven focus.
**Consequences:** Reassignment decisions are non-deterministic and slower due to network calls. The system relies heavily on prompt engineering and the LLM's availability.

## 6. Runtime Strategy Switching

**Context:** We need the ability to toggle between the rule-based baseline and the AI routing without redeploying.
**Decision:** We implemented a `RoutingEngineManager` that holds a map of strategies and an `activeStrategyKey`, allowing dynamic switching.
**Alternatives:** Feature flags via configuration files or environment variables (requiring restart).
**Why:** Runtime switching enables live demonstrations, A/B testing, and immediate fallback if the AI strategy starts failing or behaving unexpectedly in a production-like environment.
**Consequences:** Requires state management (`volatile` variable) within the singleton manager.

## 7. Gemini Integration Boundary

**Context:** We need to communicate with the Google Gemini API.
**Decision:** We isolated the Gemini API interaction in a dedicated `LLMGateway` component using Spring's modern `RestClient`.
**Alternatives:** Using a heavyweight SDK, embedding API calls directly in the routing strategy.
**Why:** Isolating the API call makes it easier to mock for testing and swap out the underlying HTTP client if needed. `RestClient` provides a fluent, modern API for HTTP calls in Spring 3.2+.
**Consequences:** Keeps the domain and service layers free of HTTP-specific logic.

## 8. AI Response Validation

**Context:** LLMs can hallucinate or return improperly formatted data (e.g., adding markdown blocks around JSON).
**Decision:** The `AIAdvisorService` explicitly strips markdown formatting and parses the raw string into JSON. It then strictly validates the presence of required fields (`recommendedAgentId`, `confidence`, `reasoning`), checks confidence boundaries [0, 100], and verifies the chosen agent actually exists in the provided `availableAgents` list.
**Alternatives:** Trusting the LLM output directly, using complex JSON schema validation libraries.
**Why:** Robustness. The system will fail fast if the LLM hallucinates an agent ID or breaks the JSON contract, preventing invalid state changes in the database.
**Consequences:** Increased safety at the cost of some custom parsing logic.

## 9. Automatic Re-planning

**Context:** When a user rejects a `ReassignmentSuggestion`, the system ideally should propose another agent.
**Decision:** *Not implemented / Deferred.* Currently, rejecting a suggestion simply sets its state to `REJECTED`. 
**Alternatives:** Automatically triggering a new `RoutingRequest` excluding the rejected agent.
**Why:** In the constraints of a 2.5-hour hackathon, focusing on the core loop (trigger -> suggest -> approve) was the priority. Implementing recursive re-planning introduces state complexity and risk of infinite loops.
**Consequences:** The user must manually trigger a new reassignment or handle the remaining unassigned order themselves if they reject the AI's suggestion.

## 10. Persistence

**Context:** The engine needs a database to store domain objects.
**Decision:** We used an H2 in-memory database with Spring Data JPA `create-drop` DDL.
**Alternatives:** PostgreSQL, MySQL, MongoDB.
**Why:** Zero setup time. It allows developers and reviewers to run the application instantly without Docker or local database installations, which is perfect for a hackathon.
**Consequences:** Data is lost on restart. Not suitable for production, but exactly right for rapid prototyping.

## 11. React Boundary

**Context:** The frontend needs to present the reassignment suggestions to a dispatcher.
**Decision:** We built a decoupled React SPA (`frontend/src`) that communicates with the Spring Boot backend via REST APIs.
**Alternatives:** Server-side rendered templates (Thymeleaf, JSP), integrated React within Spring Boot.
**Why:** Decoupling provides a clear separation of concerns, allowing parallel development of frontend and backend. It represents a modern tech stack typical of such applications.
**Consequences:** Requires running two separate development servers (Spring Boot and React dev server) and configuring CORS.

## 12. Failure Handling

**Context:** The AI strategy relies on an external API which might timeout, fail, or return invalid data.
**Decision:** The `RoutingEngineManager` wraps the strategy execution in a try-catch block. If the `activeStrategy` (e.g., AI) throws an exception, it automatically falls back to the `RULE_BASED` strategy to ensure an order is always routed.
**Alternatives:** Letting the exception propagate and failing the reassignment entirely.
**Why:** Reliability. The system must degrade gracefully. A sub-optimal rule-based assignment is better than a stalled order in a delivery system.
**Consequences:** The user might receive a rule-based suggestion even when they requested AI, but the system remains resilient.
