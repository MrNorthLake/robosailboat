package fi.robosailboat.webservice.web.controller;

import fi.robosailboat.webservice.boatCommunication.dto.SensorData;
import fi.robosailboat.webservice.robosailboatLib.repository.LoggingRepository;
import fi.robosailboat.webservice.web.service.WriteDataToCSV;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

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
    public String log(@RequestParam(value = "from", required = false) String from,
                      @RequestParam(value = "to", required = false) String to, Model model) {
        List<SensorData> logs = loggingRepository.findAll();
        Collections.reverse(logs);
        model.addAttribute("logs", logs);

        if (from != null && to != null && !from.isEmpty() && !to.isEmpty()) {
            System.out.println(from + " -> " + to);
            LocalDateTime start = LocalDateTime.parse(from);
            LocalDateTime end = LocalDateTime.parse(to);
            List<SensorData> logsBetween = loggingRepository.findByCreatedBetween(start, end);
            Collections.reverse(logsBetween);
            model.addAttribute("logs", logsBetween);
        }

        return "log";
    }


    @GetMapping("/log/download/Log.csv")
    public void downloadCSV(@RequestParam(value = "from", required = false) String from,
                            @RequestParam(value = "to", required = false) String to,
                            HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; file=customers.csv");

        if (from != null && to != null && !from.isEmpty() && !to.isEmpty()) {
            LocalDateTime start = LocalDateTime.parse(from);
            LocalDateTime end = LocalDateTime.parse(to);
            List<SensorData> logsBetween = loggingRepository.findByCreatedBetween(start, end);
            WriteDataToCSV.writeDataToCsvUsingStringArray(response.getWriter(), logsBetween);
        } else {
            List<SensorData> logs = loggingRepository.findAll();
            WriteDataToCSV.writeDataToCsvUsingStringArray(response.getWriter(), logs);
        }
    }
}
