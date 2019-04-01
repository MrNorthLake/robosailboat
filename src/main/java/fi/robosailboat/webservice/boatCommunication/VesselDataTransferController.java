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
    LOG.info("Fetched sensorData. Time: "+values.getGpsPoint().getTimeStamp()+" | Gps latitud: "
            + values.getGpsPoint().getLatitud()+ " | Gps longitud: " + values.getGpsPoint().getLongitud()
            + " | Compass direction: "+ values.getDirection());

    return new Command(values.getGpsPoint().getLatitud(),values.getGpsPoint().getLongitud());
    }

}
