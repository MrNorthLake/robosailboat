package fi.robosailboat.webservice.calculation;

import fi.robosailboat.webservice.boatCommunication.dto.Command;
import fi.robosailboat.webservice.boatCommunication.dto.SensorData;
import fi.robosailboat.webservice.boatCommunication.dto.WaypointData;
import fi.robosailboat.webservice.calculation.Calculations;
import fi.robosailboat.webservice.weatherStationCommunication.WeatherDTO;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CalculationsTest {

    private Calculations calculations;

    @Before
    public void init() {
        calculations = new Calculations();
        SensorData sensorData = new SensorData(60.105381, 19.944503, 180, 0, 0, 150);
        WaypointData waypointData = new WaypointData("1", 60.052229, 19.907767, 15,
                sensorData.getLatitude(), sensorData.getLongitude(), 15);
        WeatherDTO weatherDTO = new WeatherDTO();
        calculations.setData(sensorData, waypointData, weatherDTO);
    }

    @Test
    public void getNextCommand() {
        Command next = calculations.getNextCommand();

        assertEquals(next.getR(), "068");
        assertEquals(next.getS(), "060");
    }

    @Test
    public void calculateTrueWindDirection() {
        double result = calculations.calculateTrueWindDirection(180, 5, 5, 150);

        assertEquals(153, result, 0.5);
    }

    @Test
    public void calculateTrueWindSpeed() {
        double result = calculations.calculateTrueWindSpeed(180, 5, 5, 150);

        assertEquals(-0, result, 0.5);
    }

    @Test
    public void calculateTargetCourse() {
        double result = calculations.calculateTargetCourse();

        assertEquals(195, result, 0.9);
    }

    @Test
    public void distanceBetween() {
        double result = calculations.distanceBetween(60.105381, 19.944503,
                60.098792, 19.947658);

        assertEquals( 753, result,0.5);
    }

    @Test
    public void getTargetTackStarboard() {
        double targetCourse = calculations.calculateTargetCourse();
        boolean result = calculations.getTargetTackStarboard(targetCourse);

        assertEquals(result, false);
    }
}