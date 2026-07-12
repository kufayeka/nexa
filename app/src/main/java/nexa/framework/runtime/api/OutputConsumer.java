package nexa.framework.runtime.api;

import nexa.framework.runtime.domain.execution.model.ExecutionContext;
import nexa.framework.runtime.domain.execution.model.RuntimeMessage;

@FunctionalInterface
public interface OutputConsumer {

    void consume(ExecutionContext context, String nodeId, RuntimeMessage message);
}

