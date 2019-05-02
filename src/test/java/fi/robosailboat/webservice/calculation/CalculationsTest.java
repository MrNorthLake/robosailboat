package fi.robosailboat.webservice.calculation;

import fi.robosailboat.webservice.boatCommunication.dto.SensorData;
import fi.robosailboat.webservice.boatCommunication.dto.WaypointData;
import fi.robosailboat.webservice.boatCommunication.dto.WindData;
import fi.robosailboat.webservice.calculation.Calculations;
import org.junit.Test;

import static org.junit.Assert.*;

public class CalculationsTest {

    @Test
    public void calculateTrueWindDirection() {
        Calculations calculations = new Calculations();

        double result = calculations.calculateTrueWindDirection(180, 5, 5, 150);

        assertEquals(153, result, 0.5);
    }

    @Test
    public void distanceBetween() {
        Calculations calculations = new Calculations();

        double result = calculations.distanceBetween(60.105381, 19.944503,
                60.098792, 19.947658);

        assertEquals( 753, result,0.5);
    }
}