package nexa.framework.runtime.definition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public final class WorkspaceDefinition {

    private final String id;
    private final boolean enabled;
    private final List<FlowDefinition> flows;

    @JsonCreator
    public WorkspaceDefinition(
            @JsonProperty("id") String id,
            @JsonProperty("enabled") Boolean enabled,
            @JsonProperty("flows") List<FlowDefinition> flows) {
        this.id = id;
        this.enabled = enabled == null || enabled;
        this.flows = flows == null ? List.of() : List.copyOf(new ArrayList<>(flows));
    }

    public String id() {
        return id;
    }

    public boolean enabled() {
        return enabled;
    }

    public List<FlowDefinition> flows() {
        return flows;
    }
}
