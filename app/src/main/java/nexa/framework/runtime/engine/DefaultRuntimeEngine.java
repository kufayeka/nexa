package nexa.framework.runtime.engine;

import nexa.framework.runtime.api.OutputConsumer;
import nexa.framework.runtime.api.RuntimeConfiguration;
import nexa.framework.runtime.api.RuntimeEngine;
import nexa.framework.runtime.compile.FlowCompiler;
import nexa.framework.runtime.compile.FlowValidator;
import nexa.framework.runtime.input.InputNodeHandlerRegistry;
import nexa.framework.runtime.input.ManualInputNodeHandler;
import nexa.framework.runtime.input.TimedTriggerInputNodeHandler;
import nexa.framework.runtime.message.RuntimeMessage;
import nexa.framework.runtime.stats.RuntimeStatisticsSnapshot;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DefaultRuntimeEngine implements RuntimeEngine {

    private final FlowCompiler compiler;
    private final RuntimeExecutionService executionService;
    private final RuntimeDeploymentService deploymentService;

    private final ConcurrentMap<String, WorkspaceRuntime> workspaces;
    private final AtomicBoolean runtimeStarted;

    public DefaultRuntimeEngine(RuntimeConfiguration configuration, OutputConsumer outputConsumer) {
        Objects.requireNonNull(configuration, "configuration must not be null");
        this.compiler = new FlowCompiler(new FlowValidator());
        InputNodeHandlerRegistry inputNodeHandlerRegistry = new InputNodeHandlerRegistry(List.of(
                new ManualInputNodeHandler(),
                new TimedTriggerInputNodeHandler()));
        OutputConsumer resolvedOutputConsumer = Objects.requireNonNull(outputConsumer,
                "outputConsumer must not be null");
        this.executionService = new RuntimeExecutionService(configuration, inputNodeHandlerRegistry,
                resolvedOutputConsumer);
        this.deploymentService = new RuntimeDeploymentService(compiler, executionService);

        this.workspaces = new ConcurrentHashMap<>();
        this.runtimeStarted = new AtomicBoolean(false);
    }

    @Override
    public void startRuntime() {
        executionService.startRuntime(runtimeStarted, workspaces);
    }

    @Override
    public void stopRuntime() {
        executionService.stopRuntime(runtimeStarted, workspaces);
    }

    @Override
    public void deploy(nexa.framework.runtime.definition.WorkspaceDefinition workspaceDefinition) {
        deploymentService.deploy(workspaceDefinition, workspaces, runtimeStarted);
    }

    @Override
    public void undeploy(String workspaceId) {
        deploymentService.undeploy(workspaceId, workspaces);
    }

    @Override
    public void disable(String workspaceId) {
        WorkspaceRuntime workspaceRuntime = RuntimeExecutionService.requireWorkspace(workspaces, workspaceId);
        workspaceRuntime.setEnabled(false);
        executionService.stopWorkspaceRuntime(workspaceRuntime);
    }

    @Override
    public void enable(String workspaceId) {
        WorkspaceRuntime workspaceRuntime = RuntimeExecutionService.requireWorkspace(workspaces, workspaceId);
        workspaceRuntime.setEnabled(true);

        if (runtimeStarted.get()) {
            executionService.activateWorkspaceInputs(workspaceRuntime, runtimeStarted);
        }
    }

    @Override
    public void trigger(String workspaceId, String flowId, String inputNodeId, RuntimeMessage message) {
        executionService.trigger(workspaces, workspaceId, flowId, inputNodeId, message);
    }

    @Override
    public void setNodeEnabled(String workspaceId, String flowId, String nodeId, boolean enabled) {
        executionService.setNodeEnabled(workspaces, runtimeStarted, workspaceId, flowId, nodeId, enabled);
    }

    @Override
    public RuntimeStatisticsSnapshot statistics(String workspaceId, String flowId) {
        return executionService.statistics(workspaces, workspaceId, flowId);
    }
}
