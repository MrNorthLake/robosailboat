package fi.robosailboat.webservice.boatCommunication.dto;

import lombok.Data;

@Data
public class Command {

    private int r; // rudder angle
    private int s; // sail angle

    public Command(final int rudderAngle, final int sailAngle){
        this.r = rudderAngle;
        this.s = sailAngle;
    }
}
