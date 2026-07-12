package nexa.framework.runtime.input;

import nexa.framework.runtime.compile.CompiledNode;

public final class ManualInputNodeHandler implements InputNodeHandler {

    @Override
    public String nodeType() {
        return "manual-input";
    }

    @Override
    public void activate(CompiledNode inputNode, InputNodeActivationPort activationPort) {
        activationPort.getOrCreateState(inputNode);
    }
}
