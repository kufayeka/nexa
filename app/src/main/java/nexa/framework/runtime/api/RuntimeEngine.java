package nexa.framework.runtime.api;

import nexa.framework.runtime.definition.WorkspaceDefinition;
import nexa.framework.runtime.message.RuntimeMessage;
import nexa.framework.runtime.stats.RuntimeStatisticsSnapshot;

public interface RuntimeEngine {

    void startRuntime();

    void stopRuntime();

    void deploy(WorkspaceDefinition workspaceDefinition);

    void undeploy(String workspaceId);

    void disable(String workspaceId);

    void enable(String workspaceId);

    void trigger(String workspaceId, String flowId, String inputNodeId, RuntimeMessage message);

    void setNodeEnabled(String workspaceId, String flowId, String nodeId, boolean enabled);

    RuntimeStatisticsSnapshot statistics(String workspaceId, String flowId);
}
