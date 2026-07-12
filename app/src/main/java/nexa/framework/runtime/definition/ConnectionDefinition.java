package nexa.framework.runtime.definition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class ConnectionDefinition {

    private final String sourceNodeId;
    private final String sourcePort;
    private final String targetNodeId;

    @JsonCreator
    public ConnectionDefinition(
            @JsonProperty("sourceNodeId") String sourceNodeId,
            @JsonProperty("sourcePort") String sourcePort,
            @JsonProperty("targetNodeId") String targetNodeId) {
        this.sourceNodeId = sourceNodeId;
        this.sourcePort = sourcePort == null || sourcePort.isBlank() ? "default" : sourcePort;
        this.targetNodeId = targetNodeId;
    }

    public String sourceNodeId() {
        return sourceNodeId;
    }

    public String sourcePort() {
        return sourcePort;
    }

    public String targetNodeId() {
        return targetNodeId;
    }
}
