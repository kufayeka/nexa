package nexa.plugin.mqtt.node;

import nexa.framework.runtime.api.plugin.NexaSourcePlugin;
import nexa.framework.runtime.api.plugin.NexaPluginContext;
import nexa.framework.runtime.api.model.RuntimeMessage;
import nexa.plugin.mqtt.manager.MqttBrokerManager;
import org.eclipse.paho.client.mqttv3.MqttClient;
import java.util.Map;
import java.util.function.Consumer;

public final class MqttSharedInputPlugin implements NexaSourcePlugin {
    private Consumer<RuntimeMessage> emitter;
    private MqttClient mqttClient;
    private String brokerUrl;
    private String topic;
    private int keepAlive;

    @Override
    public String getPluginType() {
        return "mqtt-shared-input"; 
    }

    @Override
    public void setEmitter(Consumer<RuntimeMessage> emitter) {
        this.emitter = emitter;
    }

    @Override
    public void onInit(String targetId, Map<String, Object> config, NexaPluginContext context) throws Exception {
        this.brokerUrl = (String) config.getOrDefault("brokerUrl", "tcp://localhost:1883");
        this.topic = (String) config.getOrDefault("topic", "sensor/data");
        this.keepAlive = ((Number) config.getOrDefault("keepAlive", 60)).intValue();
    }

    @Override
    public void onStart() throws Exception {
        this.mqttClient = MqttBrokerManager.getOrCreateClient(this.brokerUrl, this.keepAlive);
        this.mqttClient.subscribe(this.topic, (receivedTopic, mqttMessage) -> {
            RuntimeMessage nexaMsg = new RuntimeMessage();
            nexaMsg.writeValue("payload.rawData", new String(mqttMessage.getPayload()));
            nexaMsg.writeValue("payload.topic", receivedTopic);
            
            if (this.emitter != null) {
                this.emitter.accept(nexaMsg); 
            }
        });
    }

    @Override
    public void onStop() {
        try {
            if (this.mqttClient != null && this.mqttClient.isConnected()) {
                this.mqttClient.unsubscribe(this.topic);
            }
        } catch (Exception ignored) {}
    }
}
