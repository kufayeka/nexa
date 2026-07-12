package nexa.plugin.mqtt.manager;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public final class MqttBrokerManager {
    private static final ConcurrentHashMap<String, MqttClient> clientPool = new ConcurrentHashMap<>();
    private static final ReentrantLock lock = new ReentrantLock();

    public static MqttClient getOrCreateClient(String brokerUrl, int keepAlive) throws Exception {
        MqttClient client = clientPool.get(brokerUrl);
        
        if (client == null || !client.isConnected()) {
            lock.lock();
            try {
                client = clientPool.get(brokerUrl);
                if (client == null || !client.isConnected()) {
                    String clientId = "Nexa-Shared-" + MqttClient.generateClientId();
                    client = new MqttClient(brokerUrl, clientId);
                    
                    MqttConnectOptions options = new MqttConnectOptions();
                    options.setKeepAliveInterval(keepAlive);
                    options.setCleanSession(true);
                    options.setAutomaticReconnect(true);
                    
                    client.connect(options);
                    clientPool.put(brokerUrl, client);
                    System.out.println("[MQTT Pool] TCP Connection established to: " + brokerUrl);
                }
            } finally {
                lock.unlock();
            }
        }
        return client;
    }

    public static void removeClient(String brokerUrl) {
        lock.lock();
        try {
            MqttClient client = clientPool.remove(brokerUrl);
            if (client != null && client.isConnected()) {
                client.disconnect();
                client.close();
            }
        } catch (Exception ignored) {
        } finally {
            lock.unlock();
        }
    }
}
