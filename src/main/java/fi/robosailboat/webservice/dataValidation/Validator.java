package fi.robosailboat.webservice.dataValidation;

import fi.robosailboat.webservice.boatCommunication.SensorData;

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
    int maxDiff; // Max differenece in percentage


    public Validator(SensorData latestData, SensorData expectedData, int maxDiff) {
        this.latestData = latestData;
        this.expectedData = expectedData;
        this.maxDiff = maxDiff;
    }

    public void validate(){

        BigDecimal dirDiff = latestData.getDirection().subtract(expectedData.getDirection()).abs();
        BigDecimal latDiff = latestData.getLatitud().subtract(expectedData.getLatitud()).abs();
        BigDecimal lonDiff = latestData.getLongitud().subtract(expectedData.getLongitud()).abs();
        BigDecimal comDiff = latestData.getCompassHeading().subtract(expectedData.getCompassHeading()).abs();

        BigDecimal differenceSum = dirDiff.add(latDiff).add(lonDiff).add(comDiff);
        BigDecimal expectedSum = expectedData.getDirection().add(expectedData.getLatitud())
                .add(expectedData.getLongitud()).add(expectedData.getCompassHeading());

        BigDecimal diffPercentage = differenceSum.divide(expectedSum).multiply(new BigDecimal(100));

        if(diffPercentage.intValue() > maxDiff){
            System.out.println("To big off a difference. Send route modification to boat!");
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
