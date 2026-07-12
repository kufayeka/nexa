package nexa.framework.runtime.definition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class InputExecutionPolicyDefinition {

    private final int maxConcurrentExecutions;

    @JsonCreator
    public InputExecutionPolicyDefinition(@JsonProperty("maxConcurrentExecutions") Integer maxConcurrentExecutions) {
        if (maxConcurrentExecutions == null || maxConcurrentExecutions < 1) {
            this.maxConcurrentExecutions = Integer.MAX_VALUE;
            return;
        }

        this.maxConcurrentExecutions = maxConcurrentExecutions;
    }

    public int maxConcurrentExecutions() {
        return maxConcurrentExecutions;
    }
}
