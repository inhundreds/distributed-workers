package com.test.component;

import com.test.WorkersRunner;
import com.test.entity.Job;
import com.test.entity.Status;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RequiredArgsConstructor
@Service
public class JobService {

    private static final Logger log = LoggerFactory.getLogger(JobService.class);

    private final JobRepository repository;
    private final HttpClient httpClient;

    @Transactional
    public Job findNextAvailableJobAndSetStatusToProcessing() {
        Job job = repository.findNextAvailableJob();
        job.setStatus(Status.PROCESSING);
        job = repository.save(job);
        return job;
    }

    @Transactional
    public Job setJobStatusTo(Job job, Status status, Integer httpStatusCode) {
        job.setStatus(status);
        job.setHttpStatusCode(httpStatusCode);
        job = repository.save(job);
        return job;
    }

    @Transactional(rollbackFor = Exception.class)
    public void longTransaction() throws Exception {
        var job = findNextAvailableJobAndSetStatusToProcessing();
        System.out.println(TransactionSynchronizationManager.getCurrentTransactionName());
        callURLAndSetJobStatus(job);
        System.out.println(TransactionSynchronizationManager.getCurrentTransactionName());
    }

    public void callURLAndSetJobStatus(Job job) throws Exception {
        try {
            var httpStatusCode = callUrl(new URI(job.getUrl()));
            if (httpStatusCode == 301) throw new Exception();
            setJobStatusTo(job, Status.DONE, httpStatusCode);
        } catch ( URISyntaxException | IOException  e ) {
            setJobStatusTo(job, Status.ERROR, null);
        } catch ( InterruptedException ie) {
            setJobStatusTo(job, Status.ERROR, null);
            Thread.currentThread().interrupt();
        }

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

    public void printAllJobs() {
        repository.findAll().forEach(job -> log.info(job.toString()));
    }
}
