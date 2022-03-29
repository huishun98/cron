package com.huishun.cronjobs.models;

import java.util.UUID;

public class JobModel {
    private UUID id;
    private String jobName;
    private String jobUrl;
    private String jobInterval;
    private Integer jobStatus = 1; // default
    private String jobMethod;
    private String jobParams;
    private String jobTimeUnit;
    private Integer jobNotifyError;
    private Integer jobNotifySuccess;
    private String jobResult;
    private String jobNextRun;
    private Integer jobRunCount;
    private Integer jobAlarmId;
    private String jobLastRun;

    public JobModel() {
        id = UUID.randomUUID();
    }

    public JobModel(UUID uuid) {
        this.id = uuid;
    }

    public UUID getId() {
        return id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public void setJobUrl(String jobUrl) {
        this.jobUrl = jobUrl;
    }

    public String getJobInterval() {
        return jobInterval;
    }

    public void setJobInterval(String jobInterval) {
        this.jobInterval = jobInterval;
    }

    public Integer getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(Integer jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getJobMethod() {
        return jobMethod;
    }

    public void setJobMethod(String jobMethod) {
        this.jobMethod = jobMethod;
    }

    public String getJobParams() {
        return jobParams;
    }

    public void setJobParams(String jobParams) {
        this.jobParams = jobParams;
    }

    public String getJobTimeUnit() {
        return jobTimeUnit;
    }

    public void setJobTimeUnit(String jobTimeUnit) {
        this.jobTimeUnit = jobTimeUnit;
    }

    public int getJobNotifyError() {
        return jobNotifyError;
    }

    public void setJobNotifyError(Integer jobNotifyError) {
        this.jobNotifyError = jobNotifyError;
    }

    public Integer getJobNotifySuccess() {
        return jobNotifySuccess;
    }

    public void setJobNotifySuccess(Integer jobNotifySuccess) {
        this.jobNotifySuccess = jobNotifySuccess;
    }

    public String getJobResult() {
        return jobResult;
    }

    public void setJobResult(String jobResult) {
        this.jobResult = jobResult;
    }

    public String getJobNextRun() {
        return jobNextRun;
    }

    public void setJobNextRun(String jobNextRun) {
        this.jobNextRun = jobNextRun;
    }

    public Integer getJobRunCount() {
        return jobRunCount;
    }

    public void setJobRunCount(Integer jobRunCount) {
        this.jobRunCount = jobRunCount;
    }

    public Integer getJobAlarmId() {
        return jobAlarmId;
    }

    public void setJobAlarmId(Integer jobAlarmId) {
        this.jobAlarmId = jobAlarmId;
    }

    public String getJobLastRun() {
        return jobLastRun;
    }

    public void setJobLastRun(String jobLastRun) {
        this.jobLastRun = jobLastRun;
    }
}
