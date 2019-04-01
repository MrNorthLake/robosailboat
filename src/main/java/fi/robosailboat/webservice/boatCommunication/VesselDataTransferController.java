package fi.robosailboat.webservice.boatCommunication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dataTransfer")
public class VesselDataTransferController {

    private static Logger LOG = LoggerFactory.getLogger(VesselDataTransferController.class);

    @PostMapping(value = "/postData", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public Command postData(@RequestBody DataInputParams params ){
    LOG.info("entered postData. value1: " + params.getEtt()+ " | value2: " + params.getTvo());

    return new Command(params.getEtt(),params.getTvo());
    }

}
