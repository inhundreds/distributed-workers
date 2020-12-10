package com.test;

import com.test.component.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;


import java.net.http.HttpClient;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.stream.IntStream;


@SpringBootApplication
@Profile("!test")
public class WorkersRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(WorkersRunner.class);

    public static final Duration HTTP_TIMEOUT = Duration.of(5, ChronoUnit.SECONDS);
    public static final int NO_OF_WORKER = 2;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Autowired
    private JobService service;

    @Autowired
    private HttpClient httpClient;

    public static void main(String[] args) {
        SpringApplication.run(WorkersRunner.class, args).close();
    }

    @Override
    public void run(String... args) throws InterruptedException {
        showInitialState();
        startWorkers();
        shutdown();
        showFinalState();
    }

    private void showInitialState() {
        log.info("Show initial jobs state");
        showState();
    }

    private void showFinalState() {
        log.info("Show final jobs state");
        showState();
    }
    private void showState() {
        service.printAllJobs();
    }

    private void startWorkers() throws InterruptedException {
        ArrayList<Worker> workers = prepareWorkers();
        executorService.invokeAll(workers);
    }

    private ArrayList<Worker> prepareWorkers() {
        return IntStream.range(0, NO_OF_WORKER)
                        .collect(ArrayList<Worker>::new,
                                (acc, i) -> acc.add(new Worker(service,httpClient)),
                                ArrayList::addAll);
    }

    private void shutdown() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        log.info("All thread finished their work");
    }

}