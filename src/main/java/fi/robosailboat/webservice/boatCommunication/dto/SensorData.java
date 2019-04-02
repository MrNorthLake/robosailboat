package fi.robosailboat.webservice.boatCommunication.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Document(collection = "test logging")
public class SensorData {

    @Id
    private String id;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final BigDecimal direction;
    private final BigDecimal track;
    private final int nrOfSatelites;
    private final BigDecimal compassHeading;
}
