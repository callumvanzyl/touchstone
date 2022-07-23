package com.callumvanzyl.touchstone.util

import com.callumvanzyl.touchstone.model.TestRun
import com.callumvanzyl.touchstone.service.TestRunService
import org.quartz.JobExecutionContext
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Component

@Component
class TestRunJob(
    private val testRunService: TestRunService
) : QuartzJobBean() {

    override fun executeInternal(context: JobExecutionContext) {
        testRunService.addToQueue(TestRun(schematicId = context.mergedJobDataMap.getString("schematicId"), creator = "System"))
    }
}
