package fi.robosailboat.webservice.boatCommunication;

import lombok.Data;

@Data
public class SensorData {

    private final int latitud;
    private final int longitud;
    private final int direction;
}
