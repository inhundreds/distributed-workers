package com.test.utils;

import com.test.component.JobRepository;
import com.test.entity.Job;
import com.test.entity.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestUtils {

    public static Job retrieveJobWithId(Long id, JobRepository repository) {
        return repository.findById(id).orElseThrow(
                () -> { throw new AssertionError("Can't find job with id " + id + "; please check database initialization data."); }
        );
    }

    public static void retrievedJobHasStatus(Job job, Status expectedStatus) {
        assertEquals(expectedStatus, job.getStatus());
    }

    public static void retrievedJobHasHttpCode(Job job, int expectedHttpCode) {
        assertEquals(expectedHttpCode, job.getHttpStatusCode());
    }
}
