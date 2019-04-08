package fi.robosailboat.webservice.web.controller;

import fi.robosailboat.webservice.boatCommunication.dto.SensorData;
import fi.robosailboat.webservice.robosailboatLib.repository.LoggingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {

    @Autowired
    LoggingRepository db;

    @RequestMapping("/")
    public String home(Model model) {
        SensorData latestData = db.findTopByOrderByCreatedDesc();
        model.addAttribute("wind", "wind test");
        model.addAttribute("heading", latestData.getCompassHeading());
        model.addAttribute("position", latestData.getLatitude()+", "+latestData.getLongitude());
        return "index";
    }

    @RequestMapping("/log")
    public String log() {
        return "log";
    }
}
