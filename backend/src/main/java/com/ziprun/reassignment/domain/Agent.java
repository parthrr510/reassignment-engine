package com.ziprun.reassignment.domain;

import com.ziprun.reassignment.domain.enums.AgentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "agents")
public class Agent {
    @Id
    private String id;
    private String name;

    @Enumerated(EnumType.STRING)
    private AgentStatus status;

    private int activeOrderCount;

    // Constructors, Getters, Setters
    public Agent() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public AgentStatus getStatus() { return status; }
    public void setStatus(AgentStatus status) { this.status = status; }
    public int getActiveOrderCount() { return activeOrderCount; }
    public void setActiveOrderCount(int activeOrderCount) { this.activeOrderCount = activeOrderCount; }
}
