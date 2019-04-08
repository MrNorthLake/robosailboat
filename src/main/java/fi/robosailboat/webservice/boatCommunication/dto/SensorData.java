package fi.robosailboat.webservice.boatCommunication.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Document(collection = "Logging")
public class SensorData {

    @Id
    private String id;
    private LocalDateTime created;
    private final double latitude;
    private final double longitude;
    private final double direction;
    private final double track;
    private final int nrOfSatelites;
    private final double compassHeading;
}
