package nexa.framework.runtime.engine;

import nexa.framework.runtime.compile.CompiledFlow;
import nexa.framework.runtime.compile.CompiledWorkspace;
import nexa.framework.runtime.compile.FlowCompiler;
import nexa.framework.runtime.definition.WorkspaceDefinition;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

final class RuntimeDeploymentService {

    private final FlowCompiler compiler;
    private final RuntimeExecutionService executionService;

    RuntimeDeploymentService(FlowCompiler compiler, RuntimeExecutionService executionService) {
        this.compiler = compiler;
        this.executionService = executionService;
    }

    void deploy(
            WorkspaceDefinition workspaceDefinition,
            ConcurrentMap<String, WorkspaceRuntime> workspaces,
            AtomicBoolean runtimeStarted) {
        CompiledWorkspace compiledWorkspace = compiler.compileWorkspace(workspaceDefinition);

        WorkspaceRuntime newRuntime = new WorkspaceRuntime(compiledWorkspace.workspaceId(),
                compiledWorkspace.enabled());
        for (CompiledFlow compiledFlow : compiledWorkspace.flowsById().values()) {
            newRuntime.flowsById().put(compiledFlow.flowId(),
                    new FlowRuntime(compiledWorkspace.workspaceId(), compiledFlow));
        }

        WorkspaceRuntime previous = workspaces.put(compiledWorkspace.workspaceId(), newRuntime);
        if (previous != null) {
            executionService.stopWorkspaceRuntime(previous);
            compiler.invalidateWorkspaceScripts(compiledWorkspace.workspaceId());
        }

        if (runtimeStarted.get() && newRuntime.enabled()) {
            executionService.activateWorkspaceInputs(newRuntime, runtimeStarted);
        }
    }

    void undeploy(String workspaceId, ConcurrentMap<String, WorkspaceRuntime> workspaces) {
        WorkspaceRuntime removed = workspaces.remove(workspaceId);
        if (removed != null) {
            executionService.stopWorkspaceRuntime(removed);
            compiler.invalidateWorkspaceScripts(workspaceId);
        }
    }
}