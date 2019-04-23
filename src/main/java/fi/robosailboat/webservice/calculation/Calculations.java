package fi.robosailboat.webservice.calculation;

import java.util.*;
import java.util.Vector;

public class Calculations {

    private final int DATA_OUT_OF_RANGE = -2000;
    private double prevWaypointLat;
    private double prevWaypointLon;
    private double prevWaypointRadius;
    private double nextWaypointLat;
    private double nextWaypointLon;
    private double nextWaypointRadius;
    private double vesselLat;
    private double vesselLon;
    private double trueWindSpeed;
    private double trueWindDirection;
    private Vector<Float> twdBuffer; // True wind direction buffer.

    // Vecteur field parameters
    private float incidenceAngle;
    private float maxDistanceFromLine;

    // Beating sailing mode parameters
    private float closeHauledAngle;
    private float broadReachAngle;
    private float tackingDistance;

    // State variable (inout variable)
    private int tackDirection;

    // Output variables
    private boolean beatingMode;

    public Calculations() {}

    /* Calculates the angle of the line to be followed. Reused from sailingrobots. */
    public double calculateAngleOfDesiredTrajectory() {
        int earthRadius = 6371000; //meters

        double prevWPCoord[] = {
            earthRadius * Math.cos(prevWaypointLat * Math.PI / 180) * Math.cos(prevWaypointLon * Math.PI / 180),
            earthRadius * Math.cos(prevWaypointLat * Math.PI / 180) * Math.sin(prevWaypointLon * Math.PI / 180),
            earthRadius * Math.sin(prevWaypointLat * Math.PI / 180)
        };

        double nextWPCoord[] = {
            earthRadius * Math.cos(nextWaypointLat * Math.PI / 180) * Math.cos(nextWaypointLon * Math.PI / 180),
            earthRadius * Math.cos(nextWaypointLat * Math.PI / 180) * Math.sin(nextWaypointLon * Math.PI / 180),
            earthRadius * Math.sin(nextWaypointLat * Math.PI / 180)
        };

        double m[][] = {
            {-Math.sin(vesselLon * Math.PI / 180), Math.cos(vesselLon * Math.PI / 180), 0},
            {
                -Math.cos(vesselLon * Math.PI / 180) * Math.sin(vesselLat * Math.PI / 180),
                -Math.sin(vesselLon * Math.PI / 180) * Math.sin(vesselLat * Math.PI / 180),
                Math.cos(vesselLat * Math.PI / 180)
            }
        };

        double bMinusA[] = {
            nextWPCoord[0] - prevWPCoord[0],
            nextWPCoord[1] - prevWPCoord[1],
            nextWPCoord[2] - prevWPCoord[2]
        };

        double phi = Math.atan2(m[0][0] * bMinusA[0] + m[0][1] * bMinusA[1] + m[0][2] * bMinusA[2],
                m[1][0] * bMinusA[0] + m[1][1] * bMinusA[1] + m[1][2] * bMinusA[2]);

        return phi;
    }

    /* Calculates the course to steer by using the line follow algorithm. Reused code from sailingrobots. */
    public double calculateTargetCourse() {

        if(prevWaypointLat == DATA_OUT_OF_RANGE || prevWaypointLon == DATA_OUT_OF_RANGE) {
            prevWaypointLat = vesselLat;
            prevWaypointLon = vesselLon;
            prevWaypointRadius = 30.0;

        }
        if (vesselLat == DATA_OUT_OF_RANGE || vesselLon == DATA_OUT_OF_RANGE || trueWindSpeed == DATA_OUT_OF_RANGE ||
            trueWindDirection == DATA_OUT_OF_RANGE || nextWaypointLat == DATA_OUT_OF_RANGE || nextWaypointLon == DATA_OUT_OF_RANGE ||
            nextWaypointRadius == DATA_OUT_OF_RANGE) {
            return DATA_OUT_OF_RANGE;
        } else {
            // Calculate the angle of the true wind vector.     [1]:(psi)       [2]:(psi_tw).
            double meanTrueWindDir = meanOfAngles(twdBuffer);
            double trueWindAngle = limitRadianAngleRange((meanTrueWindDir * Math.PI / 180) + Math.PI);

            // Calculate signed distance to the line.           [1] and [2]: (e).
            double signedDistance = calculateSignedDistanceToLine();

            // Calculate the angle of the line to be followed.  [1]:(phi)       [2]:(beta)
            double phi = calculateAngleOfDesiredTrajectory();

            // Calculate the target course in nominal mode.     [1]:(theta_*)   [2]:(theta_r)
            double targetCourse = phi + (2 * incidenceAngle / Math.PI) * Math.atan(signedDistance / maxDistanceFromLine);
            targetCourse = limitRadianAngleRange(targetCourse);

            // Change tack direction when reaching tacking distance
            if (Math.abs(signedDistance) > tackingDistance) {
                tackDirection = sgn(signedDistance);
            }

            // Check if the targetcourse is inconsistent with the wind.
            if ((Math.cos(trueWindAngle - targetCourse) + Math.cos(closeHauledAngle) < 0) ||
               ((Math.cos(trueWindAngle - phi) + Math.cos(closeHauledAngle) < 0) && (Math.abs(signedDistance) < maxDistanceFromLine))) {
                // Close hauled mode (Upwind beating mode).
                beatingMode = true;
                targetCourse = Math.PI + trueWindAngle + tackDirection * closeHauledAngle;
            } else if ((Math.cos(trueWindAngle - targetCourse) - Math.cos(broadReachAngle) > 0) ||
                    ((Math.cos(trueWindAngle - phi) - Math.cos(broadReachAngle) > 0) && (Math.abs(signedDistance) < maxDistanceFromLine))) {
                // Broad reach mode (Downwind beating mode).
                beatingMode = true;
                targetCourse = trueWindAngle + tackDirection * broadReachAngle;
            } else {
                beatingMode = false;
            }

            targetCourse = limitRadianAngleRange(targetCourse);
            targetCourse = targetCourse / Math.PI * 180;

            return targetCourse;
        }
    }

    /*Return distance in meters between two Gps points. Reused code from sailingrobot github*/
    public double distanceBetween(double lat1, double lon1, double lat2, double lon2){

        final double radiusOfEarth = 6371.0; //km

        double deltaLatitudeRadians = Math.toRadians(lat2 - lat1);
        double lat1Radians = Math.toRadians(lat1);
        double lat2InRadian = Math.toRadians(lat2);
        double deltaLongitudeRadians = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLatitudeRadians/2) * Math.sin(deltaLatitudeRadians/2)
                + Math.cos(lat1Radians) * Math.cos(lat2InRadian) * Math.sin(deltaLongitudeRadians/2)
                * Math.sin(deltaLongitudeRadians/2);

        double b = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = radiusOfEarth * b * 1000; //meters

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

    /* Limits radian angle range. Reused code from sailingrobots. */
    private double limitRadianAngleRange(double angle) {
        double fullRevolution = 2 * Math.PI;
        double minAngle = 0;

        while (angle < minAngle) {
            angle += fullRevolution;
        }
        while (angle >= (minAngle + fullRevolution)) {
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

    /* Calculates mean of values. Reused code from sailingrobots. */
    private float mean(Vector<Float> values) {
        if (values.size() < 1) {
            return 0;
        }
        float sum = 0;

        Iterator iterator = values.iterator();
        while(iterator.hasNext()) {
            sum += (Float)iterator.next();
        }

        return sum / values.size();
    }

    /*
     * uses formula for calculating mean of angles
     * https://en.wikipedia.org/wiki/Mean_of_circular_quantities
     * Reused code from sailingrobots.
     */
    private double meanOfAngles(Vector<Float> anglesInDegrees) {
        if (anglesInDegrees.size() < 1) {
            return 0;
        }
        Vector<Float> xx = new Vector<>();
        Vector<Float> yy = new Vector<>();
        float x, y;

        // convert all angles to cartesian coordinates
        Iterator iterator = anglesInDegrees.iterator();
        while (iterator.hasNext()) {
            float degrees = (Float)iterator.next();
            x = (float)Math.cos(degrees * Math.PI / 180);
            y = (float)Math.sin(degrees * Math.PI / 180);
            xx.add(x);
            yy.add(y);
        }

        // use formula
        double meanAngleRadians = Math.atan2(mean(yy), mean(xx));
        // atan2 produces results in the range (−π, π],
        // which can be mapped to [0, 2π) by adding 2π to negative results
        if (meanAngleRadians < 0) {
            meanAngleRadians += 2*Math.PI;
        }

        return meanAngleRadians * 180 / Math.PI;
    }

    /* Calculates signed distance to line. Reused code from sailingrobots. */
    private double calculateSignedDistanceToLine() {
        int earthRadius = 6371000; //meters

        //a
        double prevWPCoord[] = {
                earthRadius * Math.cos(prevWaypointLat * Math.PI / 180) * Math.cos(prevWaypointLon * Math.PI / 180),
                earthRadius * Math.cos(prevWaypointLat * Math.PI / 180) * Math.sin(prevWaypointLon * Math.PI / 180),
                earthRadius * Math.sin(prevWaypointLat * Math.PI / 180)
        };
        //b
        double nextWPCoord[] = {
                earthRadius * Math.cos(nextWaypointLat * Math.PI / 180) * Math.cos(nextWaypointLon * Math.PI / 180),
                earthRadius * Math.cos(nextWaypointLat * Math.PI / 180) * Math.sin(nextWaypointLon * Math.PI / 180),
                earthRadius * Math.sin(nextWaypointLat * Math.PI / 180)
        };
        //m
        double boatCoord[] = {
                earthRadius * Math.cos(vesselLat * Math.PI / 180) * Math.cos(vesselLon * Math.PI / 180),
                earthRadius * Math.cos(vesselLat * Math.PI / 180) * Math.sin(vesselLon * Math.PI / 180),
                earthRadius * Math.sin(vesselLat * Math.PI / 180)
        };

        //vector normal to plane
        double oab[] = {
                //Vector product: A^B divided by norm ||a^b||     a^b / ||a^b||
                (prevWPCoord[1] * nextWPCoord[2] - prevWPCoord[2] * nextWPCoord[1]),
                (prevWPCoord[2] * nextWPCoord[0] - prevWPCoord[0] * nextWPCoord[2]),
                (prevWPCoord[0] * nextWPCoord[1] - prevWPCoord[1] * nextWPCoord[0])
        };

        double normOAB = Math.sqrt(Math.pow(oab[0],2) + Math.pow(oab[1],2) + Math.pow(oab[2],2));

        oab[0] = oab[0] / normOAB;
        oab[1] = oab[1] / normOAB;
        oab[2] = oab[2] / normOAB;

        double signedDistance = boatCoord[0] * oab[0] + boatCoord[1] * oab[1] + boatCoord[2] * oab[2];

        return signedDistance;
    }

    /* Reused code from sailingrobots. */
    private int sgn(double value) {
        if(value == 0) return 0;
        if(value < 0) return -1;
        if(value > 0) return 1;

        return 0;
    }
}
