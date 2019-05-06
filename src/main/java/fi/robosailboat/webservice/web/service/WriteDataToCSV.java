package fi.robosailboat.webservice.web.service;

import com.opencsv.CSVWriter;
import fi.robosailboat.webservice.boatCommunication.dto.SensorData;

import java.io.PrintWriter;
import java.util.List;

public class WriteDataToCSV {

    private static String[] dataOut;


    public static void writeDataToCsvUsingStringArray(PrintWriter writer, List<SensorData> dataList) {
        String[] CSV_HEADER = {"Latitude", "Longitude", "Direction", "Track", "CompassHeading"};
        try (
                CSVWriter csvWriter = new CSVWriter(writer,
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END);
        ) {
            csvWriter.writeNext(CSV_HEADER);

            for (SensorData data : dataList) {
                dataOut = new String[]{
                        String.valueOf(data.getLatitude()),
                        String.valueOf(data.getLongitude()),
                        String.valueOf(data.getDirection()),
                        String.valueOf(data.getCompassHeading()),
                };

                csvWriter.writeNext(dataOut);
            }

            System.out.println("Write CSV using CSVWriter successfully!");
        } catch (Exception e) {
            System.out.println("Writing CSV error!");
            e.printStackTrace();
        }
    }


}
