package nexa.framework.runtime.compile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CompiledWorkspace {

    private final String workspaceId;
    private final boolean enabled;
    private final Map<String, CompiledFlow> flowsById;

    public CompiledWorkspace(String workspaceId, boolean enabled, Map<String, CompiledFlow> flowsById) {
        this.workspaceId = workspaceId;
        this.enabled = enabled;
        this.flowsById = Collections.unmodifiableMap(new LinkedHashMap<>(flowsById));
    }

    public String workspaceId() {
        return workspaceId;
    }

    public boolean enabled() {
        return enabled;
    }

    public Map<String, CompiledFlow> flowsById() {
        return flowsById;
    }
}
