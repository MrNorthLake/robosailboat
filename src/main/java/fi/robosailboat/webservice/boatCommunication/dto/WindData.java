package fi.robosailboat.webservice.boatCommunication.dto;

import lombok.Data;


@Data
public class WindData {

    private double windDirection;
    private double windSpeed;

    public WindData(double windDirection, double windSpeed) {
        this.windDirection = windDirection;
        this.windSpeed = windSpeed;
    }
}
