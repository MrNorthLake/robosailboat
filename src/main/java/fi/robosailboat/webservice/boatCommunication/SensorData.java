package fi.robosailboat.webservice.boatCommunication;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SensorData {

    private final BigDecimal latitud;
    private final BigDecimal longitud;
    private final BigDecimal direction;
    private final BigDecimal track;
    private final int nrOfSatelites;
    private final BigDecimal compassHeading;

}
