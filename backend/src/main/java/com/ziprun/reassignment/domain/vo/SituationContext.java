package com.ziprun.reassignment.domain.vo;

import java.time.LocalDateTime;

public class SituationContext {
    private String offlineAgentId;
    private String triggerReason;
    private LocalDateTime timestamp;

    public SituationContext(String offlineAgentId, String triggerReason, LocalDateTime timestamp) {
        this.offlineAgentId = offlineAgentId;
        this.triggerReason = triggerReason;
        this.timestamp = timestamp;
    }

    public String getOfflineAgentId() { return offlineAgentId; }
    public String getTriggerReason() { return triggerReason; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
