package nexa.framework.runtime.script.nexa;

public record NexaToken(
        NexaTokenType type,
        String text,
        int line,
        int column) {
}
