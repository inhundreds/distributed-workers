package com.test.component;

import com.test.TestConfiguration;
import com.test.entity.Job;
import com.test.entity.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.test.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestConfiguration.class)
@ActiveProfiles("test")
class ServiceTest {

    @Autowired
    private JobService service;

    @Autowired
    private JobRepository repository;

    private int expectedHttpStatusCode = 200;
    private Long id = 2L;

    @Test
    void findNextAvailableJobAndSetStatusToProcessing() {
        // given

        //when
        Job job = service.findNextAvailableJobAndSetStatusToProcessing();

        // then
        Job actualJob = retrieveJobWithId(job.getId(), repository);
        retrievedJobHasStatus(actualJob, Status.PROCESSING);
    }

    @Test
    void setJobStatusToShouldChangeJobStatusAndHttpCode() {
        // given
        Job job = retrieveJobWithId(id, repository);
        assertNotEquals(Status.DONE, job.getStatus());

        //when
        service.setJobStatusTo(job, Status.DONE, expectedHttpStatusCode);

        // then
        Job actualJob = retrieveJobWithId(id, repository);
        retrievedJobHasStatus(actualJob, Status.DONE);
        retrievedJobHasHttpCode(actualJob, expectedHttpStatusCode);
    }


}
