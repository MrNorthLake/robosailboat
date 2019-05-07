package fi.robosailboat.webservice.web.controller;

import fi.robosailboat.webservice.boatCommunication.WayPointService;
import fi.robosailboat.webservice.boatCommunication.dto.WaypointData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@RequestMapping("/")
public class WaypointController {

    @RequestMapping("/waypoint")
    public String waypoint() {
        return "waypoint";
    }

    @RequestMapping(value = "/addWaypoint", method = RequestMethod.POST)
    public void addWaypoint(@RequestParam(value = "index", required = true) int index,
                            @RequestParam(value = "latitude", required = true) double latitude,
                            @RequestParam(value = "longitude", required = true) double longitude,
                            @RequestParam(value = "radius", required = true) double radius) {
        WayPointService.addWaypoint(index, new WaypointData(latitude, longitude, radius));
    }

    @RequestMapping(value = "/addWaypointLastInList", method = RequestMethod.POST)
    public void addWaypointLastInList(@RequestParam(value = "latitude", required = true) double latitude,
                                      @RequestParam(value = "longitude", required = true) double longitude,
                                      @RequestParam(value = "radius", required = true) double radius) {
        WayPointService.addWaypointLastInList(new WaypointData(latitude, longitude, radius));
    }

    @RequestMapping(value = "/removeWaypoint", method = RequestMethod.POST)
    public void removeWaypoint(@RequestParam(value = "index", required = true) int index) {
        WayPointService.removeWaypoint(index);
    }

    @RequestMapping(value = "/updateWaypoint", method = RequestMethod.POST)
    public void update(@RequestParam(value = "index", required = true) int index,
                       @RequestParam(value = "action", required = true) int action,
                       @RequestParam(value = "value", required = true) double value) {
        WayPointService.updateWaypoint(index, action, value);
    }

    @RequestMapping(value ="/getWaypointList", method = RequestMethod.GET)
    public void getWaypointList(Model model) {
        model.addAttribute("waypoints",WayPointService.getWaypointList());
    }

}
