package nexa.framework.runtime.engine;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

final class WorkspaceRuntime {

    private final String workspaceId;
    private final ConcurrentMap<String, FlowRuntime> flowsById;
    private final AtomicBoolean enabled;

    WorkspaceRuntime(String workspaceId, boolean enabled) {
        this.workspaceId = workspaceId;
        this.flowsById = new ConcurrentHashMap<>();
        this.enabled = new AtomicBoolean(enabled);
    }

    String workspaceId() {
        return workspaceId;
    }

    ConcurrentMap<String, FlowRuntime> flowsById() {
        return flowsById;
    }

    boolean enabled() {
        return enabled.get();
    }

    void setEnabled(boolean value) {
        enabled.set(value);
    }
}