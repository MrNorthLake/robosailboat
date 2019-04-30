package fi.robosailboat.webservice.boatCommunication.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;


@Data
public class WaypointData {

    @Id
    private String id;
    private double nextLatitude;
    private double nextLongitude;
    private int nextRadius;
    private double prevLatitude;
    private double prevLongitude;
    private int prevRadius;
}
