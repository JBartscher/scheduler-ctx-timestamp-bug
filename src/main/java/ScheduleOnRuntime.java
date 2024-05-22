import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import io.quarkus.scheduler.Scheduler;
import io.quarkus.scheduler.Trigger;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Instant;

import static java.lang.String.format;

/**
 * ScheduleOnRuntime
 * <p>
 * little test case I build to illustrate that the {@link Trigger#getPreviousFireTime()} has the same value as
 * {@link ScheduledExecution#getScheduledFireTime()}.
 * This happens if one uses annotation based scheduling and also if one uses an injected {@link Scheduler}.
 */
public class ScheduleOnRuntime {

    private final Logger log = Logger.getLogger(ScheduleOnRuntime.class);

    @Scheduled(every = "15s", identity = "annotation-based-job")
    void schedule(ScheduledExecution ctx) {
        var trigger = ctx.getTrigger();
        var ts = trigger.getPreviousFireTime();
        log.info(format("annotation based function\nprevious:\t%s\ncurrent:\t%s\nscheduled:\t%s\nnext:\t%s\n", ctx.getTrigger().getPreviousFireTime(), ctx.getFireTime(), ctx.getScheduledFireTime(), ctx.getTrigger().getNextFireTime()));
    }

    @Inject
    Scheduler scheduler;

    public void scheduleOnStart(@Observes StartupEvent event) {
        scheduler.newJob("scheduler-based-job")
                .setConcurrentExecution(Scheduled.ConcurrentExecution.PROCEED)
                // every 15 s
                .setCron("0/15 * * * * ?").setTask(ctx -> {
                    log.info("job " + Instant.now().toString());
                    log.info(format("scheduler based function\nprevious:\t%s\ncurrent:\t%s\nscheduled:\t%s\nnext:\t%s\n", ctx.getTrigger().getPreviousFireTime(), ctx.getFireTime(), ctx.getScheduledFireTime(), ctx.getTrigger().getNextFireTime()));
                }).schedule();

    }
}
