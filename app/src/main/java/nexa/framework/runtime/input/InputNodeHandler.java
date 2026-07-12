package nexa.framework.runtime.input;

import nexa.framework.runtime.compile.CompiledNode;

public interface InputNodeHandler {

    String nodeType();

    void activate(CompiledNode inputNode, InputNodeActivationPort activationPort);
}
