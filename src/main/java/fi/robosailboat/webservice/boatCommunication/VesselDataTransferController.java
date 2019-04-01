package fi.robosailboat.webservice.boatCommunication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dataTransfer")
public class VesselDataTransferController {

    private static Logger LOG = LoggerFactory.getLogger(VesselDataTransferController.class);

    @PostMapping(value = "/sensorData", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public Command postData(@RequestBody SensorData values ){
    LOG.info("Fetched sensorData. Time: "+values.getTimeStamp()+" | Gps latitud: "
            + values.getLatitud()+ " | Gps longitud: " + values.getLongitud()
            + " | Compass direction: "+ values.getDirection());

    return new Command(values.getLatitud(),values.getLongitud());
    }

}
