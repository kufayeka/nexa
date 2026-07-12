package nexa.framework.runtime.engine;

import nexa.framework.runtime.api.OutputConsumer;
import nexa.framework.runtime.api.RuntimeConfiguration;
import nexa.framework.runtime.compile.CompiledNode;
import nexa.framework.runtime.compile.ValidationException;
import nexa.framework.runtime.definition.NodeCategory;
import nexa.framework.runtime.execution.ExecutionContext;
import nexa.framework.runtime.execution.ExecutionStatus;
import nexa.framework.runtime.input.InputNodeActivationPort;
import nexa.framework.runtime.input.InputNodeHandler;
import nexa.framework.runtime.input.InputNodeHandlerRegistry;
import nexa.framework.runtime.input.InputNodeRuntimeState;
import nexa.framework.runtime.message.RuntimeMessage;
import nexa.framework.runtime.script.ScriptExecutionResult;
import nexa.framework.runtime.script.ScriptRuntimeContext;
import nexa.framework.runtime.stats.RuntimeStatisticsSnapshot;
import nexa.framework.runtime.util.DeepCopyUtil;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

final class RuntimeExecutionService {

    private static final String DEFAULT_PORT = "default";

    private final RuntimeConfiguration configuration;
    private final InputNodeHandlerRegistry inputNodeHandlerRegistry;
    private final OutputConsumer outputConsumer;
    private final ScheduledExecutorService scheduler;
    private final ExecutorService workerExecutor;

    RuntimeExecutionService(
            RuntimeConfiguration configuration,
            InputNodeHandlerRegistry inputNodeHandlerRegistry,
            OutputConsumer outputConsumer) {
        this.configuration = configuration;
        this.inputNodeHandlerRegistry = inputNodeHandlerRegistry;
        this.outputConsumer = outputConsumer;
        this.scheduler = Executors.newScheduledThreadPool(2,
                Thread.ofPlatform().daemon(true).name("nexa-scheduler-", 0).factory());
        this.workerExecutor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("nexa-worker-", 0).factory());
    }

    void startRuntime(AtomicBoolean runtimeStarted, ConcurrentMap<String, WorkspaceRuntime> workspaces) {
        if (!runtimeStarted.compareAndSet(false, true)) {
            return;
        }

        for (WorkspaceRuntime workspaceRuntime : workspaces.values()) {
            if (workspaceRuntime.enabled()) {
                activateWorkspaceInputs(workspaceRuntime, runtimeStarted);
            }
        }
    }

    void stopRuntime(AtomicBoolean runtimeStarted, ConcurrentMap<String, WorkspaceRuntime> workspaces) {
        if (!runtimeStarted.compareAndSet(true, false)) {
            return;
        }

        for (WorkspaceRuntime workspaceRuntime : workspaces.values()) {
            stopWorkspaceRuntime(workspaceRuntime);
        }
    }

    RuntimeStatisticsSnapshot statistics(ConcurrentMap<String, WorkspaceRuntime> workspaces, String workspaceId,
            String flowId) {
        WorkspaceRuntime workspaceRuntime = requireWorkspace(workspaces, workspaceId);
        FlowRuntime flowRuntime = requireFlow(workspaceRuntime, flowId);
        return flowRuntime.statistics().snapshot();
    }

    void setNodeEnabled(
            ConcurrentMap<String, WorkspaceRuntime> workspaces,
            AtomicBoolean runtimeStarted,
            String workspaceId,
            String flowId,
            String nodeId,
            boolean enabled) {
        WorkspaceRuntime workspaceRuntime = requireWorkspace(workspaces, workspaceId);
        FlowRuntime flowRuntime = requireFlow(workspaceRuntime, flowId);

        flowRuntime.compiledFlow().setNodeEnabled(nodeId, enabled);
        flowRuntime.refreshNodeRuntime(nodeId);

        if (!enabled) {
            InputNodeRuntimeState inputState = flowRuntime.inputStateByNodeId().get(nodeId);
            if (inputState != null) {
                inputState.cancelAllScheduledTriggers();
            }
        }

        if (enabled && runtimeStarted.get() && workspaceRuntime.enabled()) {
            activateInputNode(workspaceRuntime, flowRuntime, nodeId, runtimeStarted);
        }
    }

    void trigger(
            ConcurrentMap<String, WorkspaceRuntime> workspaces,
            String workspaceId,
            String flowId,
            String inputNodeId,
            RuntimeMessage message) {
        WorkspaceRuntime workspaceRuntime = requireWorkspace(workspaces, workspaceId);
        if (!workspaceRuntime.enabled()) {
            return;
        }

        FlowRuntime flowRuntime = requireFlow(workspaceRuntime, flowId);
        if (!flowRuntime.compiledFlow().enabled()) {
            return;
        }

        CompiledNode inputNode = flowRuntime.compiledFlow().node(inputNodeId);
        if (inputNode == null || inputNode.category() != NodeCategory.INPUT || !inputNode.enabled()) {
            throw new ValidationException("Invalid input node " + inputNodeId + " for flow " + flowId);
        }

        RuntimeMessage seed = message == null ? new RuntimeMessage() : message.deepCopy();
        executeTriggeredInput(workspaceRuntime, flowRuntime, inputNode, seed);
    }

    void activateWorkspaceInputs(WorkspaceRuntime workspaceRuntime, AtomicBoolean runtimeStarted) {
        for (FlowRuntime flowRuntime : workspaceRuntime.flowsById().values()) {
            if (!flowRuntime.compiledFlow().enabled()) {
                continue;
            }

            for (String inputNodeId : flowRuntime.compiledFlow().inputNodeIds()) {
                activateInputNode(workspaceRuntime, flowRuntime, inputNodeId, runtimeStarted);
            }
        }
    }

    void stopWorkspaceRuntime(WorkspaceRuntime workspaceRuntime) {
        for (FlowRuntime flowRuntime : workspaceRuntime.flowsById().values()) {
            for (InputNodeRuntimeState inputState : flowRuntime.inputStateByNodeId().values()) {
                inputState.cancelAllScheduledTriggers();
            }

            for (UUID executionId : flowRuntime.activeExecutions().keySet()) {
                cancelExecution(flowRuntime, executionId, false);
            }
        }
    }

    static WorkspaceRuntime requireWorkspace(ConcurrentMap<String, WorkspaceRuntime> workspaces, String workspaceId) {
        WorkspaceRuntime workspaceRuntime = workspaces.get(workspaceId);
        if (workspaceRuntime == null) {
            throw new ValidationException("Workspace " + workspaceId + " not deployed");
        }
        return workspaceRuntime;
    }

    private static FlowRuntime requireFlow(WorkspaceRuntime workspaceRuntime, String flowId) {
        FlowRuntime flowRuntime = workspaceRuntime.flowsById().get(flowId);
        if (flowRuntime == null) {
            throw new ValidationException(
                    "Flow " + flowId + " not found in workspace " + workspaceRuntime.workspaceId());
        }
        return flowRuntime;
    }

    private void activateInputNode(
            WorkspaceRuntime workspaceRuntime,
            FlowRuntime flowRuntime,
            String inputNodeId,
            AtomicBoolean runtimeStarted) {
        CompiledNode inputNode = flowRuntime.compiledFlow().node(inputNodeId);
        if (inputNode == null || inputNode.category() != NodeCategory.INPUT || !inputNode.enabled()) {
            return;
        }

        InputNodeHandler handler = inputNodeHandlerRegistry.requireHandler(inputNode.type());
        handler.activate(inputNode, newInputActivationPort(workspaceRuntime, flowRuntime, runtimeStarted));
    }

    private InputNodeActivationPort newInputActivationPort(
            WorkspaceRuntime workspaceRuntime,
            FlowRuntime flowRuntime,
            AtomicBoolean runtimeStarted) {
        return new InputNodeActivationPort() {
            @Override
            public String flowId() {
                return flowRuntime.compiledFlow().flowId();
            }

            @Override
            public boolean isRuntimeStarted() {
                return runtimeStarted.get();
            }

            @Override
            public boolean isWorkspaceEnabled() {
                return workspaceRuntime.enabled();
            }

            @Override
            public InputNodeRuntimeState getOrCreateState(CompiledNode inputNode) {
                return flowRuntime.inputStateByNodeId().computeIfAbsent(
                        inputNode.id(),
                        ignored -> new InputNodeRuntimeState(inputNode.inputPolicy().maxConcurrentExecutions()));
            }

            @Override
            public void scheduleAtFixedRate(InputNodeRuntimeState state, Duration interval, Runnable task) {
                ScheduledFuture<?> scheduled = scheduler.scheduleAtFixedRate(
                        task,
                        0,
                        interval.toMillis(),
                        TimeUnit.MILLISECONDS);

                state.addScheduledTrigger(scheduled);
            }

            @Override
            public RuntimeMessage seedMessageForInput(CompiledNode inputNode) {
                return RuntimeExecutionService.this.seedMessageForInput(inputNode);
            }

            @Override
            public void executeTriggeredInput(CompiledNode inputNode, RuntimeMessage message) {
                RuntimeExecutionService.this.executeTriggeredInput(workspaceRuntime, flowRuntime, inputNode, message);
            }
        };
    }

    private RuntimeMessage seedMessageForInput(CompiledNode inputNode) {
        Object payloadRaw = inputNode.config().get("payload");
        if (payloadRaw instanceof Map<?, ?> payloadMap) {
            Map<String, Object> converted = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : payloadMap.entrySet()) {
                converted.put(String.valueOf(entry.getKey()), DeepCopyUtil.deepCopyValue(entry.getValue()));
            }
            return new RuntimeMessage(converted);
        }

        return new RuntimeMessage();
    }

    private void executeTriggeredInput(
            WorkspaceRuntime workspaceRuntime,
            FlowRuntime flowRuntime,
            CompiledNode inputNode,
            RuntimeMessage seedMessage) {
        InputNodeRuntimeState inputState = flowRuntime.inputStateByNodeId().computeIfAbsent(
                inputNode.id(),
                ignored -> new InputNodeRuntimeState(inputNode.inputPolicy().maxConcurrentExecutions()));

        boolean acquired = inputState.executionGate().tryAcquire();
        if (!acquired) {
            flowRuntime.statistics().incrementRejected();
            return;
        }

        Instant createdAt = Instant.now();
        Instant deadline = createdAt.plus(configuration.maxExecutionLifetime());

        ExecutionContext context = new ExecutionContext(workspaceRuntime.workspaceId(),
                flowRuntime.compiledFlow().flowId(),
                createdAt, deadline);
        flowRuntime.activeExecutions().put(context.executionId(), new ActiveExecution(context, inputNode.id()));

        flowRuntime.statistics().incrementRunning();
        context.retainTask();

        ScheduledFuture<?> timeoutTask = scheduler.schedule(
                () -> cancelExecution(flowRuntime, context.executionId(), true),
                configuration.maxExecutionLifetime().toMillis(),
                TimeUnit.MILLISECONDS);
        context.setTimeoutTask(timeoutTask);

        submitNodeRoutes(flowRuntime, context.executionId(), inputNode.id(), DEFAULT_PORT, seedMessage);
        completeTask(flowRuntime, context.executionId());
    }

    private void submitNodeRoutes(
            FlowRuntime flowRuntime,
            UUID executionId,
            String sourceNodeId,
            String sourcePort,
            RuntimeMessage message) {
        ActiveExecution activeExecution = flowRuntime.activeExecutions().get(executionId);
        if (activeExecution == null) {
            return;
        }

        if (activeExecution.context().isCancellationRequested()) {
            return;
        }

        List<NodeRuntime> targets = flowRuntime.targets(sourceNodeId, sourcePort);
        int totalTargets = targets.size();
        for (int index = 0; index < totalTargets; index++) {
            NodeRuntime targetNodeRuntime = targets.get(index);
            RuntimeMessage branchMessage = totalTargets <= 1 ? message : message.deepCopy();
            activeExecution.context().retainTask();

            FutureTask<Void> futureTask = new FutureTask<>(() -> {
                try {
                    executeNode(flowRuntime, activeExecution, targetNodeRuntime, branchMessage);
                    return null;
                } finally {
                    completeTask(flowRuntime, executionId);
                }
            }) {
                @Override
                protected void done() {
                    activeExecution.futures().remove(this);
                }
            };

            activeExecution.futures().add(futureTask);
            workerExecutor.execute(futureTask);
        }
    }

    private void executeNode(FlowRuntime flowRuntime, ActiveExecution activeExecution, NodeRuntime nodeRuntime,
            RuntimeMessage message) {
        ExecutionContext context = activeExecution.context();
        if (context.isCancellationRequested()) {
            return;
        }

        CompiledNode node = nodeRuntime.compiledNode();

        if (!node.enabled()) {
            return;
        }

        try {
            if (node.category() == NodeCategory.EXECUTOR) {
                executeExecutorNode(flowRuntime, context, node, message);
                return;
            }

            if (node.category() == NodeCategory.OUTPUT) {
                outputConsumer.consume(context, node.id(), message.deepCopy());
                return;
            }

            throw new ValidationException("Input node " + node.id() + " cannot be downstream target");
        } catch (Throwable throwable) {
            System.err.println("[NODE EXECUTION ERROR][" + node.id() + "] "
                    + throwable.getClass().getSimpleName()
                    + ": " + throwable.getMessage());
            throwable.printStackTrace();

            if (!context.isCancellationRequested()) {
                context.markFailed(throwable);
                context.requestCancellation();
            }
        }
    }

    private void executeExecutorNode(FlowRuntime flowRuntime, ExecutionContext context, CompiledNode node,
            RuntimeMessage inputMessage) {
        if (node.compiledScript() == null) {
            throw new ValidationException("Executor node " + node.id() + " is missing compiled script");
        }

        ScriptRuntimeContext runtimeContext = new ScriptRuntimeContext(
                context.workspaceId(),
                context.flowId(),
                node.id(),
                context.executionId().toString(),
                context.createdAt(),
                context.deadline(),
                context.data());

        ScriptExecutionResult result = node.compiledScript().execute(inputMessage, runtimeContext);
        if (result.stopped()) {
            return;
        }

        for (Map.Entry<String, List<RuntimeMessage>> entry : result.emittedByPort().entrySet()) {
            String sourcePort = entry.getKey();
            for (RuntimeMessage emittedMessage : entry.getValue()) {
                submitNodeRoutes(flowRuntime, context.executionId(), node.id(), sourcePort, emittedMessage);
            }
        }
    }

    private void completeTask(FlowRuntime flowRuntime, UUID executionId) {
        ActiveExecution activeExecution = flowRuntime.activeExecutions().get(executionId);
        if (activeExecution == null) {
            return;
        }

        if (activeExecution.context().releaseTask() > 0) {
            return;
        }

        finalizeExecution(flowRuntime, activeExecution);
    }

    private void finalizeExecution(FlowRuntime flowRuntime, ActiveExecution activeExecution) {
        ExecutionContext context = activeExecution.context();

        if (context.status() == ExecutionStatus.RUNNING) {
            if (context.isCancellationRequested()) {
                context.markCancelled();
            } else {
                context.markCompleted();
            }
        }

        if (context.timeoutTask() != null) {
            context.timeoutTask().cancel(false);
        }

        if (context.status() == ExecutionStatus.FAILED) {
            flowRuntime.statistics().incrementFailed();
        } else if (context.status() == ExecutionStatus.CANCELLED || context.status() == ExecutionStatus.TIMED_OUT) {
            flowRuntime.statistics().incrementCancelled();
        } else if (context.status() == ExecutionStatus.COMPLETED) {
            flowRuntime.statistics().incrementCompleted();
        }

        flowRuntime.statistics().addDurationNanos(Duration.between(context.createdAt(), Instant.now()).toNanos());
        flowRuntime.statistics().decrementRunning();

        InputNodeRuntimeState inputState = flowRuntime.inputStateByNodeId().get(activeExecution.inputNodeId());
        if (inputState != null) {
            inputState.executionGate().release();
        }

        flowRuntime.activeExecutions().remove(context.executionId());

        List<Future<?>> futuresSnapshot = snapshotFutures(activeExecution.futures());
        for (Future<?> future : futuresSnapshot) {
            if (!future.isDone()) {
                future.cancel(true);
            }
        }

        context.cleanup();
    }

    private void cancelExecution(FlowRuntime flowRuntime, UUID executionId, boolean timeoutTriggered) {
        ActiveExecution activeExecution = flowRuntime.activeExecutions().get(executionId);
        if (activeExecution == null) {
            return;
        }

        ExecutionContext context = activeExecution.context();
        if (!context.requestCancellation()) {
            return;
        }

        if (timeoutTriggered) {
            context.markTimedOut();
        } else {
            context.markCancelled();
        }

        List<Future<?>> futuresSnapshot = snapshotFutures(activeExecution.futures());
        for (Future<?> future : futuresSnapshot) {
            if (!future.isDone()) {
                future.cancel(true);
            }
        }
    }

    private List<Future<?>> snapshotFutures(List<Future<?>> futures) {
        synchronized (futures) {
            return new ArrayList<>(futures);
        }
    }
}