package fi.robosailboat.webservice.dataValidation;

public class Calculations {

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

    /* Calculates if the boat has to tack, which it needs if bearing to waypoint is close to true
     * wind direction. Reused code from sailingrobots. */
    public boolean calculateTack(double bearingToWaypoint, double trueWindDirection, double tackAngle) {
        double minTackAngle = trueWindDirection - tackAngle;
        double maxTackAngle = trueWindDirection + tackAngle;

        return isAngleInSector(bearingToWaypoint, minTackAngle, maxTackAngle);
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

    /* Check if angle is between sectorAngle1 and sectorAngle2, going from 1 to 2 clockwise.
    * Reused code from sailingrobots. */
    private boolean isAngleInSector(double angle, double sectorAngle1, double sectorAngle2) {
        double start = 0;
        double end = limitAngleRange(sectorAngle2 - sectorAngle1);
        double toCheck = limitAngleRange(angle - sectorAngle1);

        boolean angleIsInSector = false;
        if (toCheck >= start && toCheck <= end) {
            angleIsInSector = true;
        }
        return angleIsInSector;
    }
}
