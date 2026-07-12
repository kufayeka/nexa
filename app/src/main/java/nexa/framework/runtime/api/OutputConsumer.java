package nexa.framework.runtime.api;

import nexa.framework.runtime.execution.ExecutionContext;
import nexa.framework.runtime.message.RuntimeMessage;

@FunctionalInterface
public interface OutputConsumer {

    void consume(ExecutionContext context, String nodeId, RuntimeMessage message);
}
