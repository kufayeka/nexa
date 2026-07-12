package nexa.framework.runtime.definition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public final class FlowDefinition {

    private final String id;
    private final String name;
    private final boolean enabled;
    private final List<NodeDefinition> nodes;
    private final List<ConnectionDefinition> connections;

    @JsonCreator
    public FlowDefinition(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("enabled") Boolean enabled,
            @JsonProperty("nodes") List<NodeDefinition> nodes,
            @JsonProperty("connections") List<ConnectionDefinition> connections) {
        this.id = id;
        this.name = name == null || name.isBlank() ? id : name;
        this.enabled = enabled == null || enabled;
        this.nodes = nodes == null ? List.of() : List.copyOf(new ArrayList<>(nodes));
        this.connections = connections == null ? List.of() : List.copyOf(new ArrayList<>(connections));
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public boolean enabled() {
        return enabled;
    }

    public List<NodeDefinition> nodes() {
        return nodes;
    }

    public List<ConnectionDefinition> connections() {
        return connections;
    }
}
