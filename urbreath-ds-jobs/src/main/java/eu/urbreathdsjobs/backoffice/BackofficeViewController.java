package eu.urbreathdsjobs.backoffice;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BackofficeViewController {

    @GetMapping("/backoffice")
    public String backoffice() {
        return "backoffice";
    }
}

