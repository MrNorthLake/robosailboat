package fi.robosailboat.webservice.boatCommunication.dto;

import lombok.Data;

@Data
public class Command {

    private String r; // rudder angle
    private String s; // sail angle

    public Command(final double rudderAngle, final double sailAngle){
        this.r = String.format("%03d", rudderAngle);
        this.s = String.format("%03d", sailAngle);
    }
}
