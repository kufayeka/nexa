package nexa.framework.runtime.engine;

import nexa.framework.runtime.execution.ExecutionContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

final class ActiveExecution {

    private final ExecutionContext context;
    private final String inputNodeId;
    private final List<Future<?>> futures;

    ActiveExecution(ExecutionContext context, String inputNodeId) {
        this.context = context;
        this.inputNodeId = inputNodeId;
        this.futures = Collections.synchronizedList(new ArrayList<>());
    }

    ExecutionContext context() {
        return context;
    }

    String inputNodeId() {
        return inputNodeId;
    }

    List<Future<?>> futures() {
        return futures;
    }
}