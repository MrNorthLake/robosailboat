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
        SensorData one = new SensorData(new BigDecimal(60.105381), new BigDecimal(19.944503), new BigDecimal(0),
                new BigDecimal(0), 0, new BigDecimal(0));

        SensorData two = new SensorData(new BigDecimal(60.098792), new BigDecimal(19.947658), new BigDecimal(0),
                new BigDecimal(0), 0, new BigDecimal(0));
        Validator validator = new Validator(one , two, 1);

        double result = validator.distanceBetween(60.105381, 19.944503,
                60.098792, 19.947658);

        assertEquals( 753, result,0.5);
    }

}