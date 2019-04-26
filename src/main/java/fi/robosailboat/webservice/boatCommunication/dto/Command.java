package fi.robosailboat.webservice.boatCommunication.dto;

import lombok.Data;

import static java.lang.String.format;

@Data
public class Command {

    private String r; // rudder angle
    private String s; // sail angle

    public Command(final Double rudderAngle, final Double sailAngle){
        int a = rudderAngle.intValue();
        int b = sailAngle.intValue();
        this.r = format("%03d", a);
        this.s = format("%03d", b);
    }
}
