package com.callumvanzyl.touchstone.service

import com.callumvanzyl.touchstone.model.TestSchematic
import com.callumvanzyl.touchstone.repository.TestSchematicRepository
import com.callumvanzyl.touchstone.util.Outcome
import com.callumvanzyl.touchstone.util.TestRunJob
import org.quartz.CronScheduleBuilder
import org.quartz.CronTrigger
import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

interface SchedulingService {
    fun refreshSchedules(): Outcome<Boolean>
}

@Service("SchedulingService")
class SchedulingServiceImpl(
    private val scheduler: Scheduler,

    private val testSchematicRepository: TestSchematicRepository
) : SchedulingService {

    @Scheduled(fixedDelay = 500)
    private fun scheduled() = refreshSchedules()

    private fun createJobDetail(testSchematic: TestSchematic): JobDetail = JobBuilder.newJob(TestRunJob::class.java)
        .withIdentity(testSchematic.id, JOB_IDENTITY_GROUP)
        .withDescription("Scheduled Test Runs Jobs")
        .usingJobData(JobDataMap(mapOf("schematicId" to testSchematic.id)))
        .storeDurably()
        .build()

    private fun createJobTrigger(testSchematic: TestSchematic, jobDetail: JobDetail): Trigger = TriggerBuilder.newTrigger()
        .forJob(jobDetail)
        .withIdentity(jobDetail.key.name, TRIGGER_IDENTITY_GROUP)
        .withDescription("Scheduled Test Runs Triggers")
        .withSchedule(CronScheduleBuilder.cronSchedule(testSchematic.schedule))
        .build()

    override fun refreshSchedules(): Outcome<Boolean> {
        testSchematicRepository.findAll().forEach {
            val jobKey = JobKey(it.id, JOB_IDENTITY_GROUP)
            val triggerKey = TriggerKey(it.id, TRIGGER_IDENTITY_GROUP)

            if (it.schedule.isNullOrEmpty()) {
                if (scheduler.checkExists(jobKey)) {
                    scheduler.deleteJob(jobKey)
                }
            } else {
                if (scheduler.checkExists(triggerKey)) {
                    if ((scheduler.getTrigger(triggerKey) as CronTrigger).cronExpression != it.schedule) {
                        scheduler.rescheduleJob(triggerKey, createJobTrigger(it, scheduler.getJobDetail(jobKey)))
                    }
                } else {
                    val jobDetail = createJobDetail(it)
                    val jobTrigger = createJobTrigger(it, jobDetail)
                    scheduler.scheduleJob(jobDetail, jobTrigger)
                }
            }
        }

        return Outcome.Success(true)
    }

    companion object {

        const val JOB_IDENTITY_GROUP = "test-runs-jobs"
        const val TRIGGER_IDENTITY_GROUP = "test-runs-triggers"
    }
}
