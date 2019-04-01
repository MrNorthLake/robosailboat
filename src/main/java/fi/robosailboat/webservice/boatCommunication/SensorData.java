package fi.robosailboat.webservice.boatCommunication;

import lombok.Data;

@Data
public class SensorData {

    private final GpsPoint gpsPoint;
    private final int direction;
}
