package org.buddhi.jmeterwrapper.controller;

import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api")
public class JMeterController {

    @PostMapping("/start-test")
    public Map<String, Object> startJMeterTest(@RequestParam("jmxPath") String jmxPath, @RequestParam("testName") String testName) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate testName first
            if (testName == null) {
                response.put("started", false);
                response.put("error", "Test name cannot be null");
                return response;
            }

            String trimmedName = testName.trim();

            if (trimmedName.isEmpty()) {
                response.put("started", false);
                response.put("error", "Test name cannot be empty or contain only whitespace");
                return response;
            }

            if (trimmedName.length() > 100) {
                response.put("started", false);
                response.put("error",
                        String.format("Test name cannot exceed 100 characters. Current length: %d", trimmedName.length()));
                return response;
            }

            // Validate jmxPath
            if (jmxPath == null || jmxPath.trim().isEmpty()) {
                response.put("started", false);
                response.put("error", "JMX file path cannot be null or empty");
                return response;
            }

            // Adjust JMeter binary path
            String jmeterPath = "/home/buddhi/IdeaProjects/jmeter-wrapper/src/main/resources/jmeter/apache-jmeter-5.6.3/bin/jmeter";
            File file = new File(jmxPath);

            if (!file.exists()) {
                response.put("started", false);
                response.put("error", "JMX file not found: " + jmxPath);
                return response;
            }

            // Create results directory
            Path resultsDir = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "result");
            Files.createDirectories(resultsDir);

            String timestamp = String.valueOf(System.currentTimeMillis());
            Path resultPath = resultsDir.resolve("results_" + timestamp + ".jtl");

            // Use the validated testName for report directory
            Path reportDir = resultsDir.resolve("html_report_" + trimmedName + "_" + timestamp);

            // Command to start test
            ProcessBuilder pb = new ProcessBuilder(
                    jmeterPath,
                    "-n",
                    "-t", file.getAbsolutePath(),
                    "-l", resultPath.toString(),
                    "-e",
                    "-o", reportDir.toString()
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
            response.put("testName", trimmedName);

        } catch (Exception e) {
            response.put("started", false);
            response.put("error", "Failed to start JMeter test: " + e.getMessage());
        }

        return response;
    }
}
