package fi.robosailboat.webservice.dataValidation;

import fi.robosailboat.webservice.boatCommunication.SensorData;
/*
    Validates the difference between latest data and expected data.
    If the difference is bigger than the max difference entered
    the validator should notify the communication component to send
     route modification to the boat
 */
public class Validator {

    private SensorData latestData;
    private SensorData expectedData;
    int maxDiff;


    public Validator(SensorData latestData, SensorData expectedData, int maxDiff) {
        this.latestData = latestData;
        this.expectedData = expectedData;
        this.maxDiff = maxDiff;
    }
}
