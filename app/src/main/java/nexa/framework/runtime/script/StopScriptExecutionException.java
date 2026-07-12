package nexa.framework.runtime.script;

public final class StopScriptExecutionException extends RuntimeException {

    public StopScriptExecutionException() {
        super("Node execution stopped by script");
    }
}
