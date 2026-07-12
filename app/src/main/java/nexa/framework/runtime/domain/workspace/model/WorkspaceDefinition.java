package nexa.framework.runtime.domain.workspace.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * WorkspaceDefinition adalah representasi kontainer utama untuk kumpulan flow
 * yang didefinisikan dalam JSON.
 */
public record WorkspaceDefinition(
        String id,
        boolean enabled,
        List<FlowDefinition> flows
) {

    /**
     * Konstruktor kanonik manual untuk menormalisasi list flows agar
     * bersifat immutable dan aman dari NullPointerException.
     */
    public WorkspaceDefinition(
            String id,
            boolean enabled,
            List<FlowDefinition> flows
    ) {
        this.id = id;
        this.enabled = enabled;
        this.flows = flows == null ? List.of() : List.copyOf(new ArrayList<>(flows));
    }

    @JsonCreator
    public static WorkspaceDefinition create(
            @JsonProperty("id") String id,
            @JsonProperty("enabled") Boolean enabled,
            @JsonProperty("flows") List<FlowDefinition> flows) {
        return new WorkspaceDefinition(
                id,
                enabled == null || enabled,
                flows
        );
    }
}

