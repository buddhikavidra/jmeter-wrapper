package org.buddhi.jmeterwrapper.model;

import jakarta.persistence.*;


import java.time.LocalDateTime;

@Entity
@Table(name = "test_executions")
public class TestExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String testName;

    @Column(nullable = false)
    private String jmxPath;

    private String status; // RUNNING, COMPLETED, FAILED

    private Long pid;

    private String resultPath;

    private String reportPath;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String errorMessage;

    // Constructors
    public TestExecution() {}

    public TestExecution(String testName, String jmxPath, String status) {
        this.testName = testName;
        this.jmxPath = jmxPath;
        this.status = status;
        this.startTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getJmxPath() { return jmxPath; }
    public void setJmxPath(String jmxPath) { this.jmxPath = jmxPath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getPid() { return pid; }
    public void setPid(Long pid) { this.pid = pid; }

    public String getResultPath() { return resultPath; }
    public void setResultPath(String resultPath) { this.resultPath = resultPath; }

    public String getReportPath() { return reportPath; }
    public void setReportPath(String reportPath) { this.reportPath = reportPath; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}