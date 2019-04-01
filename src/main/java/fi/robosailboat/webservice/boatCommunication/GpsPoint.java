package fi.robosailboat.webservice.boatCommunication;

import lombok.Data;

@Data
public class GpsPoint {

    private final String timeStamp;
    private final int latitud;
    private final int longitud;
}
