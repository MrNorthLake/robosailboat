package fi.robosailboat.webservice.boatCommunication.dto;

import lombok.Data;

@Data
public class Command {

    private double r; // rudder angle
    private double s; // sail angle

    public Command(final double rudderAngle, final double sailAngle){
        this.r = rudderAngle;
        this.s = sailAngle;
    }
}
