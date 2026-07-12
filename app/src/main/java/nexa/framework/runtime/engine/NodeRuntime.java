package nexa.framework.runtime.engine;

import nexa.framework.runtime.compile.CompiledNode;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

final class NodeRuntime {

    private final AtomicReference<CompiledNode> compiledNode;

    NodeRuntime(CompiledNode compiledNode) {
        this.compiledNode = new AtomicReference<>(
                Objects.requireNonNull(compiledNode, "compiledNode must not be null"));
    }

    String nodeId() {
        return compiledNode().id();
    }

    CompiledNode compiledNode() {
        return compiledNode.get();
    }

    void setCompiledNode(CompiledNode updatedNode) {
        compiledNode.set(Objects.requireNonNull(updatedNode, "updatedNode must not be null"));
    }
}