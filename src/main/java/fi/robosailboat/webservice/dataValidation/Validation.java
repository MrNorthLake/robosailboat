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

    /* Returns bearing to waypoint. Reused code from sailingrobots. */
    public double bearingToWaypoint(double gpsLat, double gpsLon, double wpLat, double wpLon) {
        //In radians
        double boatLat = gpsLat * Math.PI / 180;
        double waypointLat = wpLat * Math.PI / 180;
        double deltaLon = (wpLon - gpsLon) * Math.PI / 180;

        double y_coordinate = Math.sin(deltaLon) * Math.cos(waypointLat);
        double x_coordinate = Math.cos(boatLat) * Math.sin(waypointLat)
                - Math.sin(boatLat) * Math.cos(waypointLat) * Math.cos(deltaLon);

        double bearingToWaypoint = Math.atan2(y_coordinate, x_coordinate);
        //Degrees
        bearingToWaypoint = bearingToWaypoint / Math.PI * 180;

        return limitAngleRange(bearingToWaypoint);
    }

    /* Limits angle range. Reused code from sailingrobots. */
    private double limitAngleRange(double angle) {
        double fullRevolution = 360;
        double minAngle = 0;

        while(angle < minAngle) {
            angle += fullRevolution;
        }
        while(angle >= (minAngle + fullRevolution)) {
            angle -= fullRevolution;
        }
        return angle;
    }
}
