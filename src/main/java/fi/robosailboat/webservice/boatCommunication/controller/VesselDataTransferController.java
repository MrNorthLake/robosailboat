package fi.robosailboat.webservice.boatCommunication.controller;

import fi.robosailboat.webservice.boatCommunication.dto.Command;
import fi.robosailboat.webservice.boatCommunication.dto.SensorData;
import fi.robosailboat.webservice.robosailboatLib.repository.LoggingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dataTransfer")
public class VesselDataTransferController {

    private static Logger LOG = LoggerFactory.getLogger(VesselDataTransferController.class);

    /*@Autowired
    private LoggingRepository loggingRepo;*/

    @PostMapping(value = "/sensorData", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public Command postData(@RequestBody SensorData values) {
        LOG.info("fetched sensorData. Gps latitud: " + values.getLatitude() + " | Gps longitud: " + values.getLongitude()
                + " | Compass direction: " + values.getDirection());

        /*this is just for testing*/
       // loggingRepo.insert(values);

        return new Command(123, 321);
    }

}
