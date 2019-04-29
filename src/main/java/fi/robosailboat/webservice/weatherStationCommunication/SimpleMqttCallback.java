package fi.robosailboat.webservice.weatherStationCommunication;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;

@Slf4j
public class SimpleMqttCallback implements MqttCallback {

    private static final String CONNECTION_URL = "tcp://192.168.1.102:1883";
    private static final String SUBSCRIPTION = "Area1/#";
    private MqttClient client;
    private static double windDirection;

    public SimpleMqttCallback() {
        try {
            this.client = new MqttClient(CONNECTION_URL, MqttClient.generateClientId());
            this.client.setCallback(this);
            this.client.subscribe(SUBSCRIPTION);
        } catch (MqttException e) {
            log.error("Mqtt error: " + e);
        }

    }

    @Override
    public void connectionLost(Throwable cause) {
        log.info("Connection lost because: " + cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        log.info("Message received:\n\t" + new String(message.getPayload()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.info("Mqtt Delivery Complete");
    }

    public static double getLatestWeather() {
        return windDirection;
    }
}
