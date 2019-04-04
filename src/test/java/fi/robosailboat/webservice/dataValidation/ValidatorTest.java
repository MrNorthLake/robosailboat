package fi.robosailboat.webservice.dataValidation;

import fi.robosailboat.webservice.boatCommunication.SensorData;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class ValidatorTest {

    @Test
    public void validate() {
    }

    @Test
    public void distanceBetweenTest() {
        SensorData one = new SensorData(60.105381, 19.944503, 0, 0, 0, 0);
        SensorData two = new SensorData(60.098792, 19.947658, 0, 0, 0, 0);
        Validator validator = new Validator(one , two, 1, 2.3);

        double result = validator.distanceBetween(60.105381, 19.944503,
                60.098792, 19.947658);

        assertEquals( 753, result,0.5);
    }

}