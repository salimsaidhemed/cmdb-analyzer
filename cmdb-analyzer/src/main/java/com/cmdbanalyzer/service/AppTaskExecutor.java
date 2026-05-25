package com.cmdbanalyzer.service;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * Runs application work away from the JavaFX Application Thread.
 */
public class AppTaskExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppTaskExecutor.class);

    private final ExecutorService executorService;

    public AppTaskExecutor() {
        this.executorService = Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, "cmdb-analyzer-worker");
            thread.setDaemon(true);
            return thread;
        });
    }

    public <T> Task<T> submit(String taskName, Supplier<T> work) {
        Objects.requireNonNull(taskName, "taskName must not be null");
        Objects.requireNonNull(work, "work must not be null");

        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                LOGGER.info("Starting background task: {}", taskName);
                updateMessage(taskName + " in progress...");
                T result = work.get();
                updateMessage(taskName + " completed");
                return result;
            }
        };

        task.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED,
                event -> LOGGER.error("Background task failed: {}", taskName, task.getException()));
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                event -> LOGGER.info("Completed background task: {}", taskName));

        executorService.submit(task);
        return task;
    }

    public Task<String> submitPlaceholderTask(String taskName, Duration duration) {
        Objects.requireNonNull(duration, "duration must not be null");

        return submit(taskName, () -> {
            try {
                Thread.sleep(duration.toMillis());
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(taskName + " was interrupted", exception);
            }
            return taskName + " completed";
        });
    }

    public void shutdown() {
        executorService.shutdownNow();
    }
}
