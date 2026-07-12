package nexa.plugin.mqtt.node;

import nexa.framework.runtime.api.plugin.NexaSinkPlugin;
import nexa.framework.runtime.api.plugin.NexaPluginContext;
import nexa.framework.runtime.api.model.RuntimeMessage;
import nexa.plugin.mqtt.manager.MqttBrokerManager;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.Map;

public final class MqttSharedSinkPlugin implements NexaSinkPlugin {
    private MqttClient mqttClient;
    private String brokerUrl;
    private String topic;
    private int keepAlive;

    @Override
    public String getPluginType() {
        return "mqtt-shared-sink";
    }

    @Override
    public void onInit(String targetId, Map<String, Object> config, NexaPluginContext context) throws Exception {
        this.brokerUrl = (String) config.getOrDefault("brokerUrl", "tcp://localhost:1883");
        this.topic = (String) config.getOrDefault("topic", "sensor/processed");
        this.keepAlive = ((Number) config.getOrDefault("keepAlive", 60)).intValue();
    }

    @Override
    public void onStart() throws Exception {
        this.mqttClient = MqttBrokerManager.getOrCreateClient(this.brokerUrl, this.keepAlive);
    }

    @Override
    public void consume(RuntimeMessage msg) {
        try {
            Object rawPayload = msg.readRawValue("payload");
            if (rawPayload == null) return;

            MqttMessage mqttMessage = new MqttMessage(rawPayload.toString().getBytes());
            mqttMessage.setQos(1);
            this.mqttClient.publish(this.topic, mqttMessage);
        } catch (Exception e) {
            System.err.println("[MQTT Sink Error] Gagal mempublikasikan data: " + e.getMessage());
        }
    }

    @Override
    public void onStop() {}
}
