package com.huishun.cronjobs.models

import java.util.UUID

class JobModel {
    var id: UUID private set
    var jobName: String? = null
    var jobUrl: String? = null
    var jobInterval: String? = null
    var jobCron: String = "*/15 * * * *"
    var jobStatus: Int = 1 // default
    var jobMethod: String? = null
    var jobParams: String? = null
    var jobTimeUnit: String? = null
    var jobNotifyError: Int? = null
    var jobNotifySuccess: Int? = null
    var jobResult: String? = null
    var jobNextRun: String? = null
    var jobRunCount: Int = 0
    var jobAlarmId: Int? = null
    var jobLastRun: String = "1580897313933"
    var jobPausedAt: String? = null

    constructor() {
        id = UUID.randomUUID()
    }

    constructor(uuid: UUID) {
        id = uuid
    }
}
