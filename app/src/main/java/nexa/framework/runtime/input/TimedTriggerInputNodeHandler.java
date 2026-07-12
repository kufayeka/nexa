package nexa.framework.runtime.input;

import nexa.framework.runtime.compile.CompiledNode;
import nexa.framework.runtime.compile.ValidationException;
import nexa.framework.runtime.message.RuntimeMessage;
import nexa.framework.runtime.util.DurationParser;

import java.time.Duration;

public final class TimedTriggerInputNodeHandler implements InputNodeHandler {

    @Override
    public String nodeType() {
        return "timed-trigger";
    }

    @Override
    public void activate(CompiledNode inputNode, InputNodeActivationPort activationPort) {
        Object intervalRaw = inputNode.config().get("interval");
        if (!(intervalRaw instanceof String intervalValue)) {
            throw new ValidationException(
                    "Input node " + inputNode.id() + " in flow " + activationPort.flowId()
                            + " requires string config.interval");
        }

        Duration interval = DurationParser.parseWithMillisecondPrecision(intervalValue);
        InputNodeRuntimeState inputState = activationPort.getOrCreateState(inputNode);

        if (inputState.hasScheduledTrigger()) {
            return;
        }

        activationPort.scheduleAtFixedRate(inputState, interval, () -> {
            if (!activationPort.isRuntimeStarted() || !activationPort.isWorkspaceEnabled()) {
                return;
            }

            RuntimeMessage message = activationPort.seedMessageForInput(inputNode);
            message.writeValue("payload.tickCount", inputState.nextTickCount());

            activationPort.executeTriggeredInput(inputNode, message);
        });
    }
}
