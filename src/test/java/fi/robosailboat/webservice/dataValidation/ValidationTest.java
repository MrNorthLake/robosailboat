package fi.robosailboat.webservice.dataValidation;

import fi.robosailboat.webservice.boatCommunication.dto.SensorData;
import org.junit.Test;

import static org.junit.Assert.*;

public class ValidationTest {

    @Test
    public void validate() {
    }

    @Test
    public void distanceBetween() {

        SensorData one = new SensorData(60.105381, 19.944503, 0, 0, 0, 0);
        SensorData two = new SensorData(60.098792, 19.947658, 0, 0, 0, 0);
        Validation validation = new Validation(one , two, 1, 2.3);

        double result = validation.distanceBetween(60.105381, 19.944503,
                60.098792, 19.947658);

        assertEquals( 753, result,0.5);
    }
}