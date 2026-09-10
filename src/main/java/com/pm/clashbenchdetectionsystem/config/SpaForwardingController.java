package com.pm.clashbenchdetectionsystem.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardingController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        String path = (String) request.getAttribute("jakarta.servlet.error.request_uri");

        // For non-API 404s, forward to index.html so React Router can handle it
        if (status != null && (int) status == HttpStatus.NOT_FOUND.value()
                && path != null
                && !path.startsWith("/api/")
                && !path.startsWith("/actuator/")
                && !path.startsWith("/swagger")
                && !path.startsWith("/v3/")) {
            return "forward:/index.html";
        }

        // For API errors and non-404s, delegate to default error handling
        throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.valueOf(status != null ? (int) status : 500));
    }
}
