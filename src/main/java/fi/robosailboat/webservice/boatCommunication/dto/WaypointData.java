package fi.robosailboat.webservice.boatCommunication.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
public class WaypointData {
    private int index;
    private double latitude;
    private double longitude;
    private double radius;

    public WaypointData(int index, double latitude, double longitude, double radius){
        this.index = index;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }
}
