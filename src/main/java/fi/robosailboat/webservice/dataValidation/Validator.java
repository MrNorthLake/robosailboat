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
}
