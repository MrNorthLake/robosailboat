package fi.robosailboat.webservice.calculation;

import fi.robosailboat.webservice.boatCommunication.dto.Command;
import fi.robosailboat.webservice.boatCommunication.dto.SensorData;

import java.lang.*;
import java.util.*;
import java.util.Vector;

public class Calculations {

    private final int DATA_OUT_OF_RANGE = -2000;
    private double prevWaypointLat;
    private double prevWaypointLon;
    private double prevWaypointRadius; // m
    private double nextWaypointLat;
    private double nextWaypointLon;
    private double nextWaypointRadius; // m
    private double vesselLat;
    private double vesselLon;
    private double trueWindSpeed; // m/s
    private double trueWindDirection; // degree [0, 360[ in North-East reference frame (clockwise)
    private Vector<Float> twdBuffer; // True wind direction buffer.

    // Vecteur field parameters
    private float incidenceAngle; // radian
    private float maxDistanceFromLine; // meters

    // Beating sailing mode parameters
    private float closeHauledAngle; // radian
    private float broadReachAngle; // radian
    private float tackingDistance; // meters

    // State variable (input variable)
    private int tackDirection; // [1] and [2]: tack variable (q).

    // Output variables
    private boolean beatingMode; // True if the vessel is in beating motion (zig-zag motion).

    public Calculations(SensorData latestData) {

        vesselLat = latestData.getLatitude();
        vesselLon = latestData.getLongitude();

        // Default values (from sailingrobots)
        tackDirection = 1;
        beatingMode = false;
        incidenceAngle = (float)(90 * Math.PI / 180);
        maxDistanceFromLine = 20;

        closeHauledAngle = (float)(45 * Math.PI / 180);
        broadReachAngle = (float)(30 * Math.PI / 180);
        tackingDistance = 15;
    }

    public Command getNextCommand() {
        int rudderCommand = 0;
        int sailCommand = 0;
        Runnable r = new Runnable() {
            public void run() {
                checkIfEnteredWaypoint();
                double targetCourse = calculateTargetCourse();
                if (targetCourse != DATA_OUT_OF_RANGE) {
                    boolean targetTackStarboard = getTargetTackStarboard(targetCourse);
                    //figure out the commands
                }
            }
        };
        Thread thread = new Thread(r);
        thread.run(); //thread.start() or thread.run()

        return new Command(rudderCommand, sailCommand);
    }

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

    /* If boat passed waypoint or enters it, set new line from boat to next waypoint. Reused code from sailingrobots. */
    public void checkIfEnteredWaypoint() {
        double distanceAfterWaypoint = calculateWaypointsOrthogonalLine(nextWaypointLat, nextWaypointLon, prevWaypointLat, prevWaypointLon,
                vesselLat, vesselLon);
        double distanceToWaypoint = distanceBetween(vesselLat, vesselLon, nextWaypointLat, nextWaypointLon);

        if (distanceAfterWaypoint > 0 || distanceToWaypoint < nextWaypointRadius) {
            prevWaypointLon = vesselLon;
            prevWaypointLat = vesselLat;
        }
    }

    /* Returns true if the desired tack of the vessel is starboard. Reused code from sailingrobots. */
    public boolean getTargetTackStarboard(double targetCourse) {

        double meanTrueWindDirection = meanOfAngles(twdBuffer);
        if (Math.sin((targetCourse - meanTrueWindDirection) * Math.PI / 180) < 0) {
            return true;
        }
        return false;
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
    private double calculateWaypointsOrthogonalLine(double nextLat, double nextLon, double prevLat, double prevLon,
                                                   double gpsLat, double gpsLon) {
        /* Check to see if boat has passed the orthogonal to the line
         * otherwise the boat will continue to follow old line if it passed the waypoint without entering the radius
         */
        int earthRadius = 6371000;

        //a
        double prevWPCoord[] = {
                earthRadius * Math.cos(prevLat * Math.PI / 180) * Math.cos(prevLon * Math.PI / 180),
                earthRadius * Math.cos(prevLat * Math.PI / 180) * Math.sin(prevLon * Math.PI / 180),
                earthRadius * Math.sin(prevLat * Math.PI / 180)
        };
        //b
        double nextWPCoord[] = {
                earthRadius * Math.cos(nextLat * Math.PI / 180) * Math.cos(nextLon * Math.PI / 180),
                earthRadius * Math.cos(nextLat * Math.PI / 180) * Math.sin(nextLon * Math.PI / 180),
                earthRadius * Math.sin(nextLat * Math.PI / 180)
        };
        //m
        double boatCoord[] = {
                earthRadius * Math.cos(gpsLat * Math.PI / 180) * Math.cos(gpsLon * Math.PI / 180),
                earthRadius * Math.cos(gpsLat * Math.PI / 180) * Math.sin(gpsLon * Math.PI / 180),
                earthRadius * Math.sin(gpsLat * Math.PI / 180)
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

        //compute if boat is after waypointModel
        //C the point such as  BC is orthogonal to AB
        double orthogonalToABFromB[] = {
                nextWPCoord[0] + oab[0],
                nextWPCoord[1] + oab[1],
                nextWPCoord[2] + oab[2]
        };

        //vector normal to plane
        double obc[] = {
                (orthogonalToABFromB[1] * nextWPCoord[2] - orthogonalToABFromB[2] * nextWPCoord[1]),
                (orthogonalToABFromB[2] * nextWPCoord[0] - orthogonalToABFromB[0] * nextWPCoord[2]),
                (orthogonalToABFromB[0] * nextWPCoord[1] - orthogonalToABFromB[1] * nextWPCoord[0])
        };

        double normOBC = Math.sqrt(Math.pow(obc[0],2) + Math.pow(obc[1],2) + Math.pow(obc[2],2));

        double orthogonalLine = boatCoord[0] * obc[0]/normOBC + boatCoord[1] * obc[1]/normOBC + boatCoord[2] * obc[2]/normOBC;

        return orthogonalLine;
    }

    /* Reused code from sailingrobots. */
    private int sgn(double value) {
        if(value == 0) return 0;
        if(value < 0) return -1;
        if(value > 0) return 1;

        return 0;
    }
}
