package nexa.framework.runtime.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import nexa.framework.runtime.definition.WorkspaceDefinition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WorkspaceJsonLoader {

    private final ObjectMapper objectMapper;

    public WorkspaceJsonLoader() {
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public WorkspaceDefinition fromJson(String json) {
        try {
            return objectMapper.readValue(json, WorkspaceDefinition.class);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to parse workspace JSON", ex);
        }
    }

    public WorkspaceDefinition fromFile(Path path) {
        try {
            String json = Files.readString(path);
            return fromJson(json);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read workspace JSON file " + path, ex);
        }
    }
}
