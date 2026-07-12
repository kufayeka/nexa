package nexa.framework.runtime.input;

import nexa.framework.runtime.compile.CompiledNode;
import nexa.framework.runtime.message.RuntimeMessage;

import java.time.Duration;

public interface InputNodeActivationPort {

    String flowId();

    boolean isRuntimeStarted();

    boolean isWorkspaceEnabled();

    InputNodeRuntimeState getOrCreateState(CompiledNode inputNode);

    void scheduleAtFixedRate(InputNodeRuntimeState state, Duration interval, Runnable task);

    RuntimeMessage seedMessageForInput(CompiledNode inputNode);

    void executeTriggeredInput(CompiledNode inputNode, RuntimeMessage message);
}
