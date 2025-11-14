package org.buddhi.jmeterwrapper.controller;

import org.buddhi.jmeterwrapper.model.TestExecution;
import org.buddhi.jmeterwrapper.service.JMeterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class JMeterController {

    @Autowired
    private JMeterService jmeterService;

    @PostMapping("/start-test")
    public Map<String, Object> startJMeterTest(@RequestParam("jmxPath") String jmxPath,
                                               @RequestParam("testName") String testName) {
        return jmeterService.startJMeterTest(jmxPath, testName);
    }

    @GetMapping("/executions")
    public Map<String, Object> getAllExecutions() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<TestExecution> executions = jmeterService.getAllExecutions();
            List<Map<String, Object>> executionData = executions.stream()
                    .map(this::mapExecutionToResponse)
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("executions", executionData);
            response.put("total", executions.size());

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to retrieve executions: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/executions/running")
    public Map<String, Object> getRunningExecutions() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<TestExecution> runningExecutions = jmeterService.getExecutionsByStatus("RUNNING");
            List<Map<String, Object>> executionData = runningExecutions.stream()
                    .map(this::mapExecutionToResponse)
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("runningExecutions", executionData);
            response.put("count", runningExecutions.size());

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to retrieve running executions: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/executions/completed")
    public Map<String, Object> getCompletedExecutions() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<TestExecution> completedExecutions = jmeterService.getExecutionsByStatus("COMPLETED");
            List<Map<String, Object>> executionData = completedExecutions.stream()
                    .map(this::mapExecutionToResponse)
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("completedExecutions", executionData);
            response.put("count", completedExecutions.size());
            //response.put("url",completedExecutions.)

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to retrieve completed executions: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/executions/{id}")
    public Map<String, Object> getExecutionById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<TestExecution> execution = jmeterService.getExecutionById(id);
            if (execution.isPresent()) {
                response.put("success", true);
                response.put("execution", mapExecutionToResponse(execution.get()));
            } else {
                response.put("success", false);
                response.put("error", "Execution not found with id: " + id);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    private Map<String, Object> mapExecutionToResponse(TestExecution execution) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", execution.getId());
        data.put("testName", execution.getTestName());
        data.put("status", execution.getStatus());
        data.put("startTime", execution.getStartTime());
        data.put("endTime", execution.getEndTime());
        data.put("pid", execution.getPid());
        data.put("jmxPath", execution.getJmxPath());


        // Add report information for completed tests
        if ("COMPLETED".equals(execution.getStatus()) && execution.getReportPath() != null) {
            data.put("htmlReportPath", execution.getReportPath());
            data.put("resultPath", execution.getResultPath());
        }

        if (execution.getErrorMessage() != null) {
            data.put("errorMessage", execution.getErrorMessage());
        }

        return data;
    }
}