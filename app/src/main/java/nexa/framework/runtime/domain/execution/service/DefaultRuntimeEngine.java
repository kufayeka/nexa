package nexa.framework.runtime.domain.execution.service;

import nexa.framework.runtime.api.OutputConsumer;
import nexa.framework.runtime.api.RuntimeConfiguration;
import nexa.framework.runtime.api.RuntimeEngine;
import nexa.framework.runtime.domain.workspace.WorkspaceModule;
import nexa.framework.runtime.domain.workspace.model.WorkspaceDefinition;
import nexa.framework.runtime.domain.scripting.ScriptingModule;
import nexa.framework.runtime.domain.deployment.DeploymentModule;
import nexa.framework.runtime.domain.deployment.model.CompiledWorkspace;
import nexa.framework.runtime.domain.execution.ExecutionModule;
import nexa.framework.runtime.domain.execution.api.ExecutionService;
import nexa.framework.runtime.domain.execution.model.RuntimeMessage;
import nexa.framework.runtime.domain.scheduler.SchedulerModule;
import nexa.framework.runtime.domain.statistics.StatisticsModule;
import nexa.framework.runtime.domain.statistics.model.RuntimeStatisticsSnapshot;

import java.util.Objects;

/**
 * DefaultRuntimeEngine adalah Composition Root utama yang menyambungkan (wiring)
 * semua Modul Domain (Workspace, Scripting, Deployment, Execution, Scheduler, Statistics)
 * secara eksplisit tanpa framework DI eksternal (Pure DI).
 * 
 * Alur Kerja Perakitan (Wiring Flow):
 * 1. Instansiasi modul daun (WorkspaceModule, ScriptingModule, StatisticsModule)
 * 2. Instansiasi modul Deployment (memerlukan ScriptEngineRegistry dari ScriptingModule)
 * 3. Instansiasi modul Execution (memerlukan konfigurasi global)
 * 4. Instansiasi modul Scheduler (memerlukan ExecutionService untuk men-trigger input)
 * 5. Hubungkan Scheduler inputActivator ke Execution engine untuk memutus siklus dependensi (DIP)
 */
public final class DefaultRuntimeEngine implements RuntimeEngine {

    private final WorkspaceModule workspaceModule;
    private final ScriptingModule scriptingModule;
    private final DeploymentModule deploymentModule;
    private final ExecutionModule executionModule;
    private final SchedulerModule schedulerModule;
    private final StatisticsModule statisticsModule;

    private final ExecutionService executionService;

    public DefaultRuntimeEngine(RuntimeConfiguration configuration, OutputConsumer outputConsumer) {
        Objects.requireNonNull(configuration, "configuration must not be null");
        Objects.requireNonNull(outputConsumer, "outputConsumer must not be null");

        // 1. Perakitan Modul Daun / Tanpa dependensi domain lain
        this.workspaceModule = new WorkspaceModule();
        this.scriptingModule = new ScriptingModule();
        this.statisticsModule = new StatisticsModule();

        // 2. Perakitan Modul dengan Constructor Dependency Injection
        this.deploymentModule = new DeploymentModule(scriptingModule.scriptEngineRegistry());
        this.executionModule = new ExecutionModule(configuration, outputConsumer);
        
        // 3. Perakitan Scheduler Module (memerlukan ExecutionService untuk eksekusi input)
        this.executionService = executionModule.executionService();
        this.schedulerModule = new SchedulerModule(executionService, executionModule.executionEngine().scheduler());

        // 4. Inversi Dependensi (DIP) untuk memutus hubungan melingkar (cyclic dependency)
        // Scheduler menyediakan inputActivator untuk dipasang di Execution engine
        this.executionModule.executionEngine().setInputActivator(schedulerModule.inputActivator());
    }

    @Override
    public void startRuntime() {
        executionService.startRuntime();
    }

    @Override
    public void stopRuntime() {
        executionService.stopRuntime();
    }

    @Override
    public void deploy(WorkspaceDefinition workspaceDefinition) {
        // Compile menggunakan Deployment domain, kemudian pasang di Execution domain
        CompiledWorkspace compiled = deploymentModule.deploymentService().compile(workspaceDefinition);
        executionService.deploy(compiled);
    }

    @Override
    public void undeploy(String workspaceId) {
        executionService.disable(workspaceId);
        executionService.undeploy(workspaceId);
        deploymentModule.deploymentService().invalidateWorkspace(workspaceId);
    }

    @Override
    public void disable(String workspaceId) {
        executionService.disable(workspaceId);
    }

    @Override
    public void enable(String workspaceId) {
        executionService.enable(workspaceId);
    }

    @Override
    public void trigger(String workspaceId, String flowId, String inputNodeId, RuntimeMessage message) {
        executionService.trigger(workspaceId, flowId, inputNodeId, message);
    }

    @Override
    public void setNodeEnabled(String workspaceId, String flowId, String nodeId, boolean enabled) {
        executionService.setNodeEnabled(workspaceId, flowId, nodeId, enabled);
    }

    @Override
    public RuntimeStatisticsSnapshot statistics(String workspaceId, String flowId) {
        return executionService.statistics(workspaceId, flowId);
    }
}
