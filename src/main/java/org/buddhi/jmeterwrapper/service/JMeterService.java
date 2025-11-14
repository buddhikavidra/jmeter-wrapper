package org.buddhi.jmeterwrapper.service;

import org.buddhi.jmeterwrapper.model.TestExecution;
import org.buddhi.jmeterwrapper.repository.TestExecutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class JMeterService {

    @Autowired
    private TestExecutionRepository testExecutionRepository;

    public Map<String, Object> startJMeterTest(String jmxPath, String testName) {
        Map<String, Object> response = new HashMap<>();

        try {
            Properties props = new Properties();
            props.load(new FileInputStream("src/application.properties"));

            String jmeterPath = Paths.get(System.getProperty("user.dir"),
                    props.getProperty("jmeter.path")).toString();

            File jmeterFile = new File(jmeterPath);
            if (!jmeterFile.exists()) {
                response.put("started", false);
                response.put("error", "JMeter executable not found at: " + jmeterPath);
                return response;
            }

            // Validate test name
            if (testName == null || testName.trim().isEmpty()) {
                response.put("started", false);
                response.put("error", "Test name cannot be null or empty");
                return response;
            }

            String trimmedName = testName.trim();
            if (trimmedName.length() > 100) {
                response.put("started", false);
                response.put("error", "Test name cannot exceed 100 characters");
                return response;
            }

            // Validate JMX path
            if (jmxPath == null || jmxPath.trim().isEmpty()) {
                response.put("started", false);
                response.put("error", "JMX file path cannot be null or empty");
                return response;
            }

            File jmxFile = new File(jmxPath);
            if (!jmxFile.exists()) {
                response.put("started", false);
                response.put("error", "JMX file not found: " + jmxPath);
                return response;
            }

            // Create results directory
            Path resultsDir = Paths.get("target/jmeter-results");
            Files.createDirectories(resultsDir);

            String timestamp = String.valueOf(System.currentTimeMillis());
            Path resultPath = resultsDir.resolve("results_" + timestamp + ".jtl");
            Path reportDir = resultsDir.resolve("html_report_" + trimmedName + "_" + timestamp);

            // Create and save test execution record
            TestExecution execution = new TestExecution(trimmedName, jmxPath, "RUNNING");
            execution.setResultPath(resultPath.toString());
            execution.setReportPath(reportDir.toString());
            TestExecution savedExecution = testExecutionRepository.save(execution);

            // Start JMeter process using path from application properties
            ProcessBuilder pb = new ProcessBuilder(
                    jmeterPath,
                    "-n",
                    "-t", jmxFile.getAbsolutePath(),
                    "-l", resultPath.toString(),
                    "-e",
                    "-o", reportDir.toString()
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Update execution with PID
            savedExecution.setPid(process.pid());
            testExecutionRepository.save(savedExecution);

            // Monitor process completion
            monitorProcess(process, savedExecution.getId());

            // Log output
            logOutput(process);

            response.put("started", true);
            response.put("message", "JMeter test started successfully");
            response.put("executionId", savedExecution.getId());
            response.put("pid", process.pid());
            response.put("testName", trimmedName);

        } catch (Exception e) {
            response.put("started", false);
            response.put("error", "Failed to start JMeter test: " + e.getMessage());
        }

        return response;
    }

    private void monitorProcess(Process process, Long executionId) {
        CompletableFuture.runAsync(() -> {
            try {
                int exitCode = process.waitFor();
                Optional<TestExecution> executionOpt = testExecutionRepository.findById(executionId);

                if (executionOpt.isPresent()) {
                    TestExecution execution = executionOpt.get();
                    if (exitCode == 0) {
                        execution.setStatus("COMPLETED");
                    } else {
                        execution.setStatus("FAILED");
                        execution.setErrorMessage("Process exited with code: " + exitCode);
                    }
                    execution.setEndTime(java.time.LocalDateTime.now());
                    testExecutionRepository.save(execution);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                updateExecutionStatus(executionId, "FAILED", "Process monitoring interrupted");
            }
        });
    }

    private void logOutput(Process process) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[JMeter] " + line);
                }
            } catch (IOException e) {
                System.err.println("Error reading JMeter output: " + e.getMessage());
            }
        }).start();
    }

    private void updateExecutionStatus(Long executionId, String status, String errorMessage) {
        Optional<TestExecution> executionOpt = testExecutionRepository.findById(executionId);
        if (executionOpt.isPresent()) {
            TestExecution execution = executionOpt.get();
            execution.setStatus(status);
            execution.setErrorMessage(errorMessage);
            execution.setEndTime(java.time.LocalDateTime.now());
            testExecutionRepository.save(execution);
        }
    }

    public List<TestExecution> getAllExecutions() {
        return testExecutionRepository.findAllByOrderByStartTimeDesc();
    }

    public List<TestExecution> getExecutionsByStatus(String status) {
        return testExecutionRepository.findByStatus(status);
    }

    public Optional<TestExecution> getExecutionById(Long id) {
        return testExecutionRepository.findById(id);
    }
}