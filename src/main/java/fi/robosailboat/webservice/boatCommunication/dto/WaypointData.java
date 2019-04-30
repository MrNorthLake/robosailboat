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

    public WaypointData(String id, double nextLatitude, double nextLongitude, int nextRadius, double prevLatitude, double prevLongitude, int prevRadius) {
        this.id = id;
        this.nextLatitude = nextLatitude;
        this.nextLongitude = nextLongitude;
        this.nextRadius = nextRadius;
        this.prevLatitude = prevLatitude;
        this.prevLongitude = prevLongitude;
        this.prevRadius = prevRadius;
    }
}
