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
        SensorData sensorData = new SensorData(60.105381, 19.944503, 180, 4, 150);
        WaypointData waypointData = new WaypointData("1", 60.052229, 19.907767, 15,
                sensorData.getLatitude(), sensorData.getLongitude(), 15);
        WeatherDTO weatherDTO = new WeatherDTO();
        weatherDTO.setDirection(100);
        weatherDTO.setSpeed(5);
        calculations.setData(sensorData, waypointData, weatherDTO);
    }

    @Test
    public void getNextCommand() {
        Command next = calculations.getNextCommand();

        assertEquals("067", next.getR());
        assertEquals("087", next.getS());

        SensorData sensorData = new SensorData(60.104568, 19.945619, 0, 0, 0, 30);
        WaypointData waypointData = new WaypointData("1", 60.104718, 19.946027, 15, 60.104568, 19.945619, 15);
        WeatherDTO weatherDTO = new WeatherDTO();
        weatherDTO.setDirection(30);
        weatherDTO.setSpeed(5);
        calculations.setData(sensorData, waypointData, weatherDTO);

        next = calculations.getNextCommand();

        assertEquals("060", next.getR());
        assertEquals("094", next.getS());
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

        result = calculations.calculateTrueWindSpeed(0, 5, 5, 30);

        assertEquals(0.8, result, 0.5);
    }

    @Test
    public void calculateTargetCourse() {
        double result = calculations.calculateTargetCourse();

        assertEquals(197, result, 0.5);
    }

    @Test
    public void distanceBetween() {
        double result = calculations.distanceBetween(60.105381, 19.944503,
                60.098792, 19.947658);

        assertEquals( 753, result,0.5);
    }
}