package com.beanbrewcafe.barista.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduler Service for automatic priority recalculation
 * Runs every 30 seconds to update order priorities
 */
@Service
@EnableScheduling
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final PriorityQueueService priorityQueueService;

    /**
     * Recalculate priorities every 30 seconds
     */
    @Scheduled(fixedDelayString = "${scheduler.priority-recalculation-interval:30000}")
    public void recalculatePriorities() {
        log.debug("Running scheduled priority recalculation");
        priorityQueueService.recalculatePriorities();
    }
}