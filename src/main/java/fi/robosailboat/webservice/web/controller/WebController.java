package fi.robosailboat.webservice.web.controller;

import fi.robosailboat.webservice.boatCommunication.dto.SensorData;
import fi.robosailboat.webservice.robosailboatLib.repository.LoggingRepository;
import fi.robosailboat.webservice.web.service.WriteDataToCSV;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
public class WebController {

    @Autowired
    LoggingRepository loggingRepository;

    @RequestMapping("/")
    public String home(Model model) {
        SensorData latestData = loggingRepository.findTopByOrderByCreatedDesc();
        model.addAttribute("wind", "wind test");
        model.addAttribute("heading", latestData.getCompassHeading());
        model.addAttribute("position", latestData.getLatitude()+", "+latestData.getLongitude());
        return "index";
    }

    @RequestMapping("/log")
    public String log() {
        return "log";
    }


    @GetMapping("/log/download/Log.csv")
    public void downloadCSV(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; file=customers.csv");

        List<SensorData> logs = loggingRepository.findAll();

         WriteDataToCSV.writeDataToCsvUsingStringArray(response.getWriter(), logs);
    }
}
