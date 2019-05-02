package fi.robosailboat.webservice.calculation;

import fi.robosailboat.webservice.boatCommunication.dto.Command;
import fi.robosailboat.webservice.boatCommunication.dto.SensorData;
import fi.robosailboat.webservice.boatCommunication.dto.WaypointData;
import fi.robosailboat.webservice.boatCommunication.dto.WindData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.*;
import java.util.*;
import java.util.Vector;

public class Calculations {

    private static Logger LOG = LoggerFactory.getLogger(Calculations.class);

    // Constants
    private final int DATA_OUT_OF_RANGE = -2000;
    private final int NO_COMMAND = -1000;

    // For rudder angle calculation
    private double desiredHeading; // degrees [0, 360[ in North-East reference frame (clockwise)
    private double vesselHeading; // degrees [0, 360[ in North-East reference frame (clockwise)
    private double maxRudderAngle; // degrees

    // For sail angle calculation
    private double maxSailAngle; // degrees
    private double minSailAngle; // degrees
    private double apparentWindDirection; // degrees [0, 360[ in North-East reference frame (clockwise)

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
    private Vector<Double> twdBuffer; // True wind direction buffer.
    private int twdBufferMaxSize = 200;

    // Vecteur field parameters
    private double incidenceAngle; // radian
    private double maxDistanceFromLine; // meters

    // Beating sailing mode parameters
    private double closeHauledAngle; // radian
    private double broadReachAngle; // radian
    private double tackingDistance; // meters

    // State variable (inout variable)
    private int tackDirection; // [1] and [2]: tack variable (q).
                            // if tacking is required => tack direction is either 1 or -1

    // Output variables
    private boolean beatingMode; // True if the vessel is in beating motion (zig-zag motion).

    private double rudderCommandAngle; // degrees [-30, +30[ in vessel reference frame (clockwise from top view)
    private double sailCommandAngle; // degrees

    public Calculations() {
        LOG.info("Initialising values...");
        init();

        // Max and min angles
        maxRudderAngle = 30;
        maxSailAngle = 120; //??
        minSailAngle = 60; //??

        // Default values (from sailingrobots)
        tackDirection = 1;
        beatingMode = false;
        incidenceAngle = Math.toRadians(90);
        maxDistanceFromLine = 20;

        closeHauledAngle = Math.toRadians(45);
        broadReachAngle = Math.toRadians(30);
        tackingDistance = 15;
    }

    public void init() {
        desiredHeading = DATA_OUT_OF_RANGE;
        vesselHeading = DATA_OUT_OF_RANGE;
        vesselLat = DATA_OUT_OF_RANGE;
        vesselLon = DATA_OUT_OF_RANGE;
        prevWaypointLat = DATA_OUT_OF_RANGE;
        prevWaypointLon = DATA_OUT_OF_RANGE;
        prevWaypointRadius = DATA_OUT_OF_RANGE;
        nextWaypointLat = DATA_OUT_OF_RANGE;
        nextWaypointLon = DATA_OUT_OF_RANGE;
        nextWaypointRadius = DATA_OUT_OF_RANGE;
        trueWindSpeed = DATA_OUT_OF_RANGE;
        trueWindDirection = DATA_OUT_OF_RANGE;
        apparentWindDirection = DATA_OUT_OF_RANGE;
        twdBuffer = new Vector<>();
    }

    /* Must set values for all calculations to work. */
    public void setData(SensorData sensorData, WaypointData waypointData, WindData windData) {
        LOG.info("Setting data...");
        vesselLat = sensorData.getLatitude();
        vesselLon = sensorData.getLongitude();
        vesselHeading = sensorData.getCompassHeading();

        double gpsSpeed = windData.getWindSpeed(); // get from GPS

        nextWaypointLat = waypointData.getNextLatitude();
        nextWaypointLon = waypointData.getNextLongitude();
        nextWaypointRadius = waypointData.getNextRadius();
        prevWaypointLat = waypointData.getPrevLatitude();
        prevWaypointLon = waypointData.getPrevLongitude();
        prevWaypointRadius = waypointData.getPrevRadius();

        double windDir = windData.getWindDirection();
        double windSpeed = windData.getWindSpeed();

        trueWindDirection = calculateTrueWindDirection(windDir, windSpeed, gpsSpeed, vesselHeading);
        LOG.info("Calculated true wind direction: " + trueWindDirection);
        trueWindSpeed = calculateTrueWindSpeed(windDir, windSpeed, gpsSpeed, vesselHeading);
        LOG.info("Calculated true wind speed: " + trueWindSpeed);

        addValueToTwdBuffer(trueWindDirection);

        calculateApparentWind(windDir, windSpeed, gpsSpeed, vesselHeading);
        LOG.info("Calculated apparent wind direction: " + apparentWindDirection);
    }

    public Command getNextCommand() {
        checkIfEnteredWaypoint();
        desiredHeading = calculateTargetCourse();
        LOG.info("Calculated desired heading (target course): " + desiredHeading);
        if (desiredHeading != DATA_OUT_OF_RANGE) {
            // True if the desired tack of the vessel is starboard.
            boolean targetTackStarboard = getTargetTackStarboard(desiredHeading); //need to send to boat?
        }
        //figure out the commands
        rudderCommandAngle = calculateRudderAngle();
        // +90 degrees for converting to Arduino
        rudderCommandAngle += 90;
        sailCommandAngle = calculateSailAngle();
        LOG.info("Got rudder angle: " + rudderCommandAngle + " and sail angle: " + sailCommandAngle);

        return new Command(rudderCommandAngle, sailCommandAngle);
    }

    /* Calculates the command rudder angle according to the course difference. Reused code from sailingrobots. */
    public double calculateRudderAngle() {
        if (desiredHeading != DATA_OUT_OF_RANGE && vesselHeading != DATA_OUT_OF_RANGE) {
            double differenceHeading = (vesselHeading - desiredHeading) * Math.PI / 180; //radians
            LOG.info("In calculateRudderAngle, got difference heading: " + differenceHeading);

            // Wrong sense because over +/- 90°
            if (Math.cos(differenceHeading) < 0) {
                // Max Rudder angle in the opposite way
                LOG.info("Max rudder angle in opposite direction");
                return sgn(Math.sin(differenceHeading)) * maxRudderAngle;
            } else {
                // Regulation of the rudder
                return Math.sin(differenceHeading) * maxRudderAngle;
            }
        }
        return NO_COMMAND;
    }

    /* Calculate the sail angle according to a linear relation to the apparent wind direction. Reused code from sailingrobots. */
    public double calculateSailAngle() {
        if (apparentWindDirection != DATA_OUT_OF_RANGE) {
            // Equation from book "Robotic Sailing 2015", page 141
            return (maxSailAngle - minSailAngle) * Math.abs(limitAngleRange180(apparentWindDirection)) / 180 + minSailAngle;
        }
        return NO_COMMAND;
    }

    public double calculateTrueWindDirection(double windsensorDir, double windsensorSpeed, double gpsSpeed, double heading) {
        if (windsensorSpeed < 0.001) {
            return heading;
        }

        if (windsensorDir < 0.001) {
            windsensorDir = 0.001;
        } else if (windsensorDir > 359.999) {
            windsensorDir = 359.999;
        }
        double windSensorDirRadian = Math.toRadians(windsensorDir);

        double trueWindSpeed = Math.sqrt((windsensorSpeed*windsensorSpeed) + (gpsSpeed*gpsSpeed) -
                (2 * gpsSpeed * windsensorSpeed * Math.cos(windSensorDirRadian)));

        double alpha = Math.acos((windsensorSpeed * Math.cos(windSensorDirRadian) - gpsSpeed) / trueWindSpeed);

        double twd = 0;
        if (windsensorDir > 180) {
            twd = limitAngleRange(heading - alpha);
        } else {
            twd = limitAngleRange(heading + alpha);
        }
        return twd;
    }

    public double calculateTrueWindSpeed(double windsensorDir, double windsensorSpeed, double gpsSpeed, double heading) {
        if (windsensorSpeed < 0.001) {
            return gpsSpeed;
        }

        double apparentWindAngle = Math.toRadians(windsensorDir);

        if (apparentWindAngle < 0.001) {
            apparentWindAngle = 0.001;
        } else if (apparentWindAngle > 359.999) {
            apparentWindAngle = 359.999;
        }

        double u = gpsSpeed * Math.sin(heading) - windsensorSpeed * Math.sin(apparentWindAngle);
        double v = gpsSpeed * Math.cos(heading) - windsensorSpeed * Math.cos(apparentWindAngle);

        return Math.atan(u/v);
    }

    public void calculateApparentWind(double windsensorDir, double windsensorSpeed, double gpsSpeed, double heading) {
        double wcaw[] = {
                trueWindSpeed * Math.cos((trueWindDirection+Math.PI) - heading) - gpsSpeed,
                trueWindSpeed * Math.sin((trueWindDirection+Math.PI) - heading)
        };

        //apparentWindSpeed = Math.sqrt(Math.pow(wcaw[0], 2) + Math.pow(wcaw[1], 2));
        apparentWindDirection = Math.toDegrees(-Math.atan2(wcaw[0], wcaw[1]));
    }

    /* Calculates the angle of the line to be followed. Reused from sailingrobots. */
    public double calculateAngleOfDesiredTrajectory() {
        int earthRadius = 6371000; //meters

        double prevWPCoord[] = {
            earthRadius * Math.cos(Math.toRadians(prevWaypointLat)) * Math.cos(Math.toRadians(prevWaypointLon)),
            earthRadius * Math.cos(Math.toRadians(prevWaypointLat)) * Math.sin(Math.toRadians(prevWaypointLon)),
            earthRadius * Math.sin(Math.toRadians(prevWaypointLat))
        };

        double nextWPCoord[] = {
            earthRadius * Math.cos(Math.toRadians(nextWaypointLat)) * Math.cos(Math.toRadians(nextWaypointLon)),
            earthRadius * Math.cos(Math.toRadians(nextWaypointLat)) * Math.sin(Math.toRadians(nextWaypointLon)),
            earthRadius * Math.sin(Math.toRadians(nextWaypointLat))
        };

        double m[][] = {
            {-Math.sin(vesselLon * Math.PI / 180), Math.cos(vesselLon * Math.PI / 180), 0},
            {
                -Math.cos(Math.toRadians(vesselLon)) * Math.sin(Math.toRadians(vesselLat)),
                -Math.sin(Math.toRadians(vesselLon)) * Math.sin(Math.toRadians(vesselLat)),
                Math.cos(Math.toRadians(vesselLon))
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
            LOG.info("In calculateTargetCourse()");
            // Calculate the angle of the true wind vector.     [1]:(psi)       [2]:(psi_tw).
            double meanTrueWindDir = meanOfAngles(twdBuffer);
            LOG.info("meanTrueWindDir: " + meanTrueWindDir);
            double trueWindAngle = limitRadianAngleRange((Math.toRadians(meanTrueWindDir)) + Math.PI);
            LOG.info("trueWindAngle: " + trueWindAngle);

            // Calculate signed distance to the line.           [1] and [2]: (e).
            double signedDistance = calculateSignedDistanceToLine();
            LOG.info("signedDistance: " + signedDistance);

            // Calculate the angle of the line to be followed.  [1]:(phi)       [2]:(beta)
            double phi = calculateAngleOfDesiredTrajectory();
            LOG.info("phi: " + phi);

            // Calculate the target course in nominal mode.     [1]:(theta_*)   [2]:(theta_r)
            double targetCourse = phi + (2 * incidenceAngle / Math.PI) * Math.atan(signedDistance / maxDistanceFromLine);
            targetCourse = limitRadianAngleRange(targetCourse);

            // Change tack direction when reaching tacking distance
            if (Math.abs(signedDistance) > tackingDistance) {
                LOG.info("Changing tack direction...");
                tackDirection = sgn(signedDistance);
            }

            // Check if the targetcourse is inconsistent with the wind.
            if ((Math.cos(trueWindAngle - targetCourse) + Math.cos(closeHauledAngle) < 0) ||
               ((Math.cos(trueWindAngle - phi) + Math.cos(closeHauledAngle) < 0) && (Math.abs(signedDistance) < maxDistanceFromLine))) {
                // Close hauled mode (Upwind beating mode).
                LOG.info("Close hauled mode");
                beatingMode = true;
                targetCourse = Math.PI + trueWindAngle + tackDirection * closeHauledAngle;
            } else if ((Math.cos(trueWindAngle - targetCourse) - Math.cos(broadReachAngle) > 0) ||
                    ((Math.cos(trueWindAngle - phi) - Math.cos(broadReachAngle) > 0) && (Math.abs(signedDistance) < maxDistanceFromLine))) {
                // Broad reach mode (Downwind beating mode).
                LOG.info("Broad reach mode");
                beatingMode = true;
                targetCourse = trueWindAngle + tackDirection * broadReachAngle;
            } else {
                LOG.info("beatingMode = false");
                beatingMode = false;
            }

            targetCourse = limitRadianAngleRange(targetCourse);
            targetCourse = Math.toDegrees(targetCourse);

            return targetCourse;
        }
    }

    /* If boat passed waypoint or enters it, set new line from boat to next waypoint. Reused code from sailingrobots. */
    public void checkIfEnteredWaypoint() {
        double distanceAfterWaypoint = calculateWaypointsOrthogonalLine(nextWaypointLat, nextWaypointLon, prevWaypointLat, prevWaypointLon,
                vesselLat, vesselLon);
        double distanceToWaypoint = distanceBetween(vesselLat, vesselLon, nextWaypointLat, nextWaypointLon);

        if (distanceAfterWaypoint > 0 || distanceToWaypoint < nextWaypointRadius) {
            LOG.info("Setting previous waypoint to boat position...");
            prevWaypointLon = vesselLon;
            prevWaypointLat = vesselLat;
        }
    }

    /* Returns true if the desired tack of the vessel is starboard. Reused code from sailingrobots. */
    public boolean getTargetTackStarboard(double targetCourse) {

        double meanTrueWindDirection = meanOfAngles(twdBuffer);
        if (Math.sin(Math.toRadians(targetCourse - meanTrueWindDirection)) < 0) {
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
        LOG.info("Distance between: " + distance);

        return distance;

    }

    /* Returns bearing to waypoint. Reused code from sailingrobots. */
    public double bearingToWaypoint(double gpsLat, double gpsLon, double wpLat, double wpLon) {
        //In radians
        double boatLat = Math.toRadians(gpsLat);
        double waypointLat = Math.toRadians(wpLat);
        double deltaLon = Math.toRadians(wpLon - gpsLon);

        double y_coordinate = Math.sin(deltaLon) * Math.cos(waypointLat);
        double x_coordinate = Math.cos(boatLat) * Math.sin(waypointLat)
                - Math.sin(boatLat) * Math.cos(waypointLat) * Math.cos(deltaLon);

        double bearingToWaypoint = Math.atan2(y_coordinate, x_coordinate);
        //Degrees
        bearingToWaypoint = Math.toDegrees(bearingToWaypoint);

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

    /* Limits angle range, min angle -180. Reused code from sailingrobots. */
    private double limitAngleRange180(double angle) {
        double fullRevolution = 360;
        double minAngle = -180;

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

    /* Calculates mean of values. Reused code from sailingrobots. */
    private double mean(Vector<Double> values) {
        if (values.size() < 1) {
            return 0;
        }
        double sum = 0;

        Iterator iterator = values.iterator();
        while(iterator.hasNext()) {
            sum += (Double)iterator.next();
        }

        return sum / values.size();
    }

    /*
     * uses formula for calculating mean of angles
     * https://en.wikipedia.org/wiki/Mean_of_circular_quantities
     * Reused code from sailingrobots.
     */
    private double meanOfAngles(Vector<Double> anglesInDegrees) {
        if (anglesInDegrees.size() < 1) {
            return 0;
        }
        Vector<Double> xx = new Vector<>();
        Vector<Double> yy = new Vector<>();
        double x, y;

        // convert all angles to cartesian coordinates
        Iterator iterator = anglesInDegrees.iterator();
        while (iterator.hasNext()) {
            double degrees = (Double)iterator.next();
            x = Math.cos(Math.toRadians(degrees));
            y = Math.sin(Math.toRadians(degrees));
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

    /* Add values to twdBuffer. Reused code from sailingrobots. */
    private void addValueToTwdBuffer(double value) {
        twdBuffer.add(value);

        if (twdBuffer.size() > twdBufferMaxSize) {
            twdBuffer.removeElementAt(0);
        }
    }

    /* Calculates signed distance to line. Reused code from sailingrobots. */
    private double calculateSignedDistanceToLine() {
        int earthRadius = 6371000; //meters

        //a
        double prevWPCoord[] = {
                earthRadius * Math.cos(Math.toRadians(prevWaypointLat)) * Math.cos(Math.toRadians(prevWaypointLon)),
                earthRadius * Math.cos(Math.toRadians(prevWaypointLat)) * Math.sin(Math.toRadians(prevWaypointLon)),
                earthRadius * Math.sin(Math.toRadians(prevWaypointLat))
        };
        //b
        double nextWPCoord[] = {
                earthRadius * Math.cos(Math.toRadians(nextWaypointLat)) * Math.cos(Math.toRadians(nextWaypointLon)),
                earthRadius * Math.cos(Math.toRadians(nextWaypointLat)) * Math.sin(Math.toRadians(nextWaypointLon)),
                earthRadius * Math.sin(Math.toRadians(nextWaypointLat))
        };
        //m
        double boatCoord[] = {
                earthRadius * Math.cos(Math.toRadians(vesselLat)) * Math.cos(Math.toRadians(vesselLon)),
                earthRadius * Math.cos(Math.toRadians(vesselLat)) * Math.sin(Math.toRadians(vesselLon)),
                earthRadius * Math.sin(Math.toRadians(vesselLat))
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
                earthRadius * Math.cos(Math.toRadians(prevLat)) * Math.cos(Math.toRadians(prevLon)),
                earthRadius * Math.cos(Math.toRadians(prevLat)) * Math.sin(Math.toRadians(prevLon)),
                earthRadius * Math.sin(Math.toRadians(prevLat))
        };
        //b
        double nextWPCoord[] = {
                earthRadius * Math.cos(Math.toRadians(nextLat)) * Math.cos(Math.toRadians(nextLon)),
                earthRadius * Math.cos(Math.toRadians(nextLat)) * Math.sin(Math.toRadians(nextLon)),
                earthRadius * Math.sin(Math.toRadians(nextLat))
        };
        //m
        double boatCoord[] = {
                earthRadius * Math.cos(Math.toRadians(gpsLat)) * Math.cos(Math.toRadians(gpsLon)),
                earthRadius * Math.cos(Math.toRadians(gpsLat)) * Math.sin(Math.toRadians(gpsLon)),
                earthRadius * Math.sin(Math.toRadians(gpsLat))
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

    /* The sign function. Reused code from sailingrobots. */
    private int sgn(double value) {
        if(value == 0) return 0;
        if(value < 0) return -1;
        if(value > 0) return 1;

        return 0;
    }
}
