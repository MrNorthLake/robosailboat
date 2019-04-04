package fi.robosailboat.webservice.dataValidation;

import fi.robosailboat.webservice.boatCommunication.dto.SensorData;

public class Validation {

    private SensorData latestData;
    private SensorData expectedData;
    int maxDistanceDiff; // Max distance difference between latest and expected in meters
    double maxDirectionDiff; // Max difference in direction


    public Validation(SensorData latestData, SensorData expectedData, int maxDistanceDiff, double maxDirectionDiff) {
        this.latestData = latestData;
        this.expectedData = expectedData;
        this.maxDistanceDiff = maxDistanceDiff;
        this.maxDirectionDiff = maxDirectionDiff;
    }

    public void validate(){

        double distanceDiff = distanceBetween(latestData.getLatitude(), latestData.getLongitude(),
                expectedData.getLatitude(), expectedData.getLongitude());

        if(distanceDiff > maxDistanceDiff){
            //Distance is bigger than max value
            System.out.println("Notify boat communication component to send route modifications!");
        }

        /*To do:
         * Check if direction difference is too big
         * */

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
