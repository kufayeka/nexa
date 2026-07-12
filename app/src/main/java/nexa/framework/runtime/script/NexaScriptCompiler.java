package nexa.framework.runtime.script;

import nexa.framework.runtime.compile.ValidationException;
import nexa.framework.runtime.script.nexa.NexaCompiledScript;
import nexa.framework.runtime.script.nexa.NexaParser;
import nexa.framework.runtime.script.nexa.NexaProgram;
import nexa.framework.runtime.script.nexa.NexaScriptException;
import nexa.framework.runtime.script.nexa.NexaTokenizer;

public final class NexaScriptCompiler implements ScriptCompiler {

    @Override
    public CompiledScript compile(String scriptSource, String sourceName) {
        try {
            NexaTokenizer tokenizer = new NexaTokenizer(scriptSource);
            NexaParser parser = new NexaParser(tokenizer.tokenize());
            NexaProgram program = parser.parseProgram();
            return new NexaCompiledScript(sourceName, scriptSource, program);
        } catch (NexaScriptException exception) {
            throw new ValidationException(formatDiagnostic("compile", sourceName, scriptSource, exception));
        }
    }

    public static ValidationException runtimeError(String sourceName, String scriptSource, NexaScriptException exception) {
        return new ValidationException(formatDiagnostic("runtime", sourceName, scriptSource, exception));
    }

    private static String formatDiagnostic(
            String phase,
            String sourceName,
            String scriptSource,
            NexaScriptException exception) {
        String[] parts = parseSourceName(sourceName);
        String sourceLine = resolveSourceLine(scriptSource, exception.line());
        return "[nexa-script-error]"
                + " phase=" + phase
                + " workspace=" + parts[0]
                + " flow=" + parts[1]
                + " node=" + parts[2]
                + " line=" + exception.line()
                + " column=" + exception.column()
                + " sourceLine=" + sourceLine
                + " message=" + exception.getMessage();
    }

    private static String[] parseSourceName(String sourceName) {
        String[] parts = sourceName == null ? new String[0] : sourceName.split(":", 3);
        String workspace = parts.length > 0 ? parts[0] : "unknown";
        String flow = parts.length > 1 ? parts[1] : "unknown";
        String node = parts.length > 2 ? parts[2] : "unknown";
        return new String[] { workspace, flow, node };
    }

    private static String resolveSourceLine(String scriptSource, int lineNumber) {
        if (scriptSource == null || scriptSource.isBlank() || lineNumber < 1) {
            return "";
        }

        String[] lines = scriptSource.split("\\R", -1);
        if (lineNumber > lines.length) {
            return "";
        }
        return lines[lineNumber - 1].trim();
    }
}
