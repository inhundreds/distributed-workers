package com.test;

import com.test.component.JobService;
import com.test.entity.Job;
import com.test.entity.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Callable;

@Component
@AllArgsConstructor
public class Worker implements Callable<Boolean> {

    private static final Logger log = LoggerFactory.getLogger(Worker.class);

    private final JobService service;

    private final HttpClient httpClient;

    @Override
    public Boolean call() throws Exception {
        while(true)
            {
                Job job = getNextAvailableJob();
                if (job==null) {
                    log.info("No available job");
                    return false;
                }
                callURLAndSetJobStatus(job);
            }
    }

    public void callURLAndSetJobStatus(Job job) {
        try {
            var httpStatusCode = callUrl(new URI(job.getUrl()));
            setJobStatus(job, httpStatusCode);
        } catch ( URISyntaxException | IOException  e ) {
            setJobStatusForExceptionalConditions(job);
        } catch ( InterruptedException ie) {
            setJobStatusForExceptionalConditions(job);
            Thread.currentThread().interrupt();
        }
    }

    public Job getNextAvailableJob() {
        log.info("Looking for a job...");
        Job job = service.findNextAvailableJobAndSetStatusToProcessing();
        log.info("... found a job {},",job);
        return job;
    }

    private Integer callUrl(URI uri) throws IOException, InterruptedException {
        log.info("Calling URI {}", uri);
        Integer result = get(uri);
        log.info("Http Status Code {} for {}", result, uri);
        return result;
    }

    private Integer get(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .timeout(WorkersRunner.HTTP_TIMEOUT)
                .uri(uri)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode();
    }

    private Job setJobStatus(Job job, int httpStatusCode) {
        Status newStatus = Status.DONE;
        if (httpStatusCode != 200)
            newStatus = Status.ERROR;
        service.setJobStatusTo(job, newStatus, httpStatusCode);
        return job;
    }

    private Job setJobStatusForExceptionalConditions(Job job) {
        service.setJobStatusTo(job, Status.ERROR, null);
        return job;
    }
}
