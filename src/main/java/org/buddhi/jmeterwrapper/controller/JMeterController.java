package org.buddhi.jmeterwrapper.controller;

import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class JMeterController {

    @PostMapping("/start-test")
    public Map<String, Object> startJMeterTest(@RequestParam("jmxPath") String jmxPath) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Adjust JMeter binary path
            String jmeterPath = "/home/buddhi/IdeaProjects/jmeter-wrapper/src/main/resources/jmeter/apache-jmeter-5.6.3/bin/jmeter"; // path to jmeter CLI
            File file = new File(jmxPath);

            if (!file.exists()) {
                response.put("started", false);
                response.put("error", "JMX file not found: " + jmxPath);
                return response;
            }

            // Command to start test
            ProcessBuilder pb = new ProcessBuilder(
                    jmeterPath, "-n", "-t", file.getAbsolutePath(), "-l", "results_" + System.currentTimeMillis() + ".jtl"
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Print process output asynchronously
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[JMeter] " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            response.put("started", true);
            response.put("message", "JMeter test started successfully.");
            response.put("pid", process.pid());
        } catch (Exception e) {
            response.put("started", false);
            response.put("error", e.getMessage());
        }

        return response;
    }
}
