package fi.robosailboat.webservice.boatCommunication;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SensorData {

    private final double latitud;
    private final double longitud;
    private final double direction;
    private final double track;
    private final int nrOfSatelites;
    private final double compassHeading;

}
