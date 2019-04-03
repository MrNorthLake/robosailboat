package fi.robosailboat.webservice;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {

    @RequestMapping("/")
    public String home(Model model) {
        model.addAttribute("wind", "wind test");
        return "home";
    }
}
