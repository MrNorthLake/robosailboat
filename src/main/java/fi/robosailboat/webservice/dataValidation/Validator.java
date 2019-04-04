package fi.robosailboat.webservice.dataValidation;

import fi.robosailboat.webservice.boatCommunication.SensorData;

import javax.validation.constraints.Max;
import java.math.BigDecimal;

/*
    Validates the difference between latest data and expected data.
    If the difference is bigger than the max difference entered
    the validator should notify the communication component to send
     route modification to the boat
 */
public class Validator {

    private SensorData latestData;
    private SensorData expectedData;
    int maxDistanceDiff; // Max distance difference between latest and expected in meters
    double maxDirectionDiff; // Max difference in direction


    public Validator(SensorData latestData, SensorData expectedData, int maxDistanceDiff, double maxDirectionDiff) {
        this.latestData = latestData;
        this.expectedData = expectedData;
        this.maxDistanceDiff = maxDistanceDiff;
        this.maxDirectionDiff = maxDirectionDiff;
    }

    public void validate(){

        double distanceDiff = distanceBetween(latestData.getLatitud(), latestData.getLongitud(),
                expectedData.getLatitud(), expectedData.getLongitud());

        if(distanceDiff > maxDistanceDiff){
            //Distance is bigger than max value
            System.out.println("Notify boat communication component to send route modifications!");
        }

    }

    /*Return distance in meters between two Gps points. Reused code from sailingrobot github*/
    public double distanceBetween(double lat1, double lon1, double lat2, double lon2){

        final double radiusOfEarth = 6371.0;

        double deltaLatitudeRadians = Math.toRadians(lat2 - lat1);
        double lat1Radians = Math.toRadians(lat1);
        double lat2InRadian = Math.toRadians(lat2);
        double deltaLongitudeRadians = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLatitudeRadians/2) * Math.sin(deltaLatitudeRadians/2)
                + Math.cos(lat1Radians) * Math.cos(lat2InRadian) * Math.sin(deltaLongitudeRadians/2)
                * Math.sin(deltaLongitudeRadians/2);

        double b = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = radiusOfEarth * b * 1000;

        return distance;

    }
}
