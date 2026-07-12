package nexa.framework.runtime.script.nexa;

public interface NexaHostObject {

    Object member(String name, int line, int column);
}
