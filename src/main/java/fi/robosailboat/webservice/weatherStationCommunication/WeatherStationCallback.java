package fi.robosailboat.webservice.weatherStationCommunication;

import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public class WeatherStationCallback implements MqttCallback {

    private final static Logger LOG = LoggerFactory.getLogger(WeatherStationCallback.class);
    private Map lastMessage;
    private int messageSec = 0;

    /**
     * have no clue what to do.
     */
    @Override
    public void connectionLost(Throwable cause) {

    }

    //Override methods from MqttCallback interface
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        LOG.info("message is : " + message);

        if(message.getId() > messageSec) {
            this.lastMessage = JsonObjectParser.parseJsonObject(new JSONObject(message.toString()));
            this.messageSec++;
        }
    }

    /**
     * have no clue what to do.
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public Map getLatestMessage() {
        return this.lastMessage;
    }
}
