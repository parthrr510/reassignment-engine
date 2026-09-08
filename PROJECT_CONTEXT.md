# AI Reassignment Engine - Project Context

## 1. Problem
ZipRun's delivery orders are manually assigned to agents at the beginning of a shift. When an agent becomes unexpectedly unavailable (e.g., sickness, breakdown) mid-shift, the current process requires an operations manager to manually detect the issue, find affected orders, check other agents' capacities, and reassign them. This manual process is slow, heavily dependent on human attention, and prone to silent failures.

## 2. Goal
Build a reactive reassignment engine that automatically detects when an agent goes offline, identifies their affected orders, leverages the Gemini API to recommend the most suitable replacement agent, and queues this recommendation for operations manager approval.

## 3. Scope
- Reactive reassignment loop triggered when an agent becomes unavailable.
- Pluggable routing engine with runtime switching between Rule-based and AI-based strategies.
- AI advisor generating recommendations (agent + confidence + explanation) using order details, agent roster, and situation context.
- Minimal React operations UI to view, approve, or reject suggestions.

## 4. Explicitly Out-of-Scope Functionality
- Full dispatch system capabilities.
- Order creation workflows.
- Geographical zone management.
- SLA dashboards.
- Complex user authentication and authorization.

## 5. Functional Requirements
- **Status Monitoring**: Automatically detect when an agent goes offline/unavailable.
- **Affected Order Identification**: Find all active orders assigned to the offline agent.
- **Agentic Re-planning**: Trigger the active routing strategy automatically to find a new agent for the affected orders without manual operator intervention.
- **AI Recommendation**: Use the Gemini API to recommend a replacement agent, along with a confidence score and a plain-English explanation.
- **Ops Review**: Queue suggestions and display them in a minimal React operations UI.
- **Approval Workflow**: Allow ops to approve or reject reassignment suggestions.

## 6. Non-Functional Requirements
- **Speed & Simplicity**: Optimized for a 2.5-hour solo hackathon (prioritizing working MVP over production completeness).
- **Extensibility**: Clean domain model that anticipates future extensions without structural rework.
- **Runtime Flexibility**: Switch routing strategies (Rule vs. AI) dynamically without restarting the application.

## 7. Domain Entities
- **Agent**: `id`, `name`, `status`, `active_order_count`.
- **Order**: `id`, `description`, `assigned_agent_id`, `status`.
- **ReassignmentSuggestion**: `id`, `order_id`, `proposed_agent_id`, `confidence_score`, `reasoning`, `status`.

## 8. State Machines
- **Agent State**: `AVAILABLE` <-> `BUSY` -> `OFFLINE`
- **Order State**: `UNASSIGNED` -> `ASSIGNED` -> `COMPLETED`
- **Suggestion State**: `PENDING` -> `APPROVED` | `REJECTED`

## 9. Major Features
- Pluggable routing interface.
- Rule-based routing implementation (fallback).
- AI-powered routing implementation via Gemini API.
- Runtime strategy toggle without restart.
- Automated replanning loop triggered by agent status updates.
- Pending Suggestions Dashboard (React Frontend).

## 10. AI Responsibilities
- Analyze the affected order, available agent roster, and contextual situation.
- Return a structured recommendation containing:
  - Recommended agent ID
  - Confidence score (0-100)
  - Plain-English explanation for the choice

## 11. Architecture Principles
- Keep the problem statement as the center of the project; do not invent large features outside the reassignment loop.
- Monolithic Spring Boot backend for simplicity and speed.
- In-memory data management (H2) for rapid iteration.
- Strategy Pattern for the routing engine to enable seamless runtime switching.

## 12. Tech Stack
- **Backend**: Spring Boot 3.x, Java 17+
- **Frontend**: React 18
- **Primary LLM**: Gemini API
- **Database**: H2 (In-memory)

## 13. MVP
A working flow where:
1. An agent is marked as offline/unavailable.
2. The system automatically identifies their active orders.
3. The active strategy (AI or Rule) recommends a replacement and generates reasoning.
4. The React UI displays the queued suggestion.
5. The Ops user approves the suggestion, reassigning the order.

## 14. Development Milestones for a 2.5-Hour Hackathon
1. **Min 0-30**: Project setup, Domain Model, State Machines, Repository layer, Seed Data.
2. **Min 30-60**: Rule-based Routing Strategy, Agent offline detection, Affected orders identification.
3. **Min 60-90**: Gemini API Integration, AI Routing Strategy implementation.
4. **Min 90-120**: Runtime Strategy Toggle, Minimal React UI for suggestions (Approve/Reject).
5. **Min 120-150**: End-to-end testing, refinement, demo recording.

## 15. Future Extensions
- Full dispatch capabilities.
- Order creation workflows.
- Geographical zone management.
- SLA tracking.

## 16. Current Status
- **Phase**: Planning
- **Code**: Not started
- **Next Step**: Bootstrap Spring Boot backend and React frontend.

## 17. Important Design Decisions
- **Polling vs. WebSockets**: The frontend will poll for pending suggestions to save time, given the 2.5-hour constraint.
- **LLM Gateway**: Using direct HTTP client calls for Gemini instead of heavyweight frameworks (Spring AI) to ensure simplicity and avoid abstraction leaks.
- **Enums for State Machines**: Using simple enums to cleanly define and manage state transitions for Agents, Orders, and Suggestions, ensuring robust state management without over-engineering.
