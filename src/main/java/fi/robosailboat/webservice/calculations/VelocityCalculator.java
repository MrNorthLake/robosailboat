package fi.robosailboat.webservice.calculations;

import fi.robosailboat.webservice.boatCommunication.GpsPoint;

public class VelocityCalculator {

    private GpsPoint first;
    private GpsPoint second;

    public VelocityCalculator(GpsPoint first, GpsPoint second) {
        first = first;
        second = second;
    }

    public String calculateKmh(){
        //Calculate velocity and return kmh
        return "";
    }

    public String calcualteKnots(){
        //Calulate velocity and return knots
        return "";
    }
}
