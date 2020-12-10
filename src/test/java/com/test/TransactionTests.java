package com.test;

import com.test.component.JobRepository;
import com.test.entity.Job;
import com.test.entity.Status;
import com.test.utils.TestUtils;
import lombok.AllArgsConstructor;

import org.junit.jupiter.api.*;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = TestConfiguration.class)
@ActiveProfiles("test")
class TransactionTests {

    private static final Logger log = LoggerFactory.getLogger(TransactionTests.class);

    @Autowired
    private JobRepository repository;

    @Test
    @Transactional
    void nextAvailableJobRowShouldBeLocked()  {
        // given
        // a Job with status NEW with a lock
        var job = repository.findNextAvailableJob();

        // when
        Future<Integer> taskForUpdatingTheJob = prepareTaskForExecution(job);
        Exception thrownException = Assertions.assertThrows(ExecutionException.class, () -> {
            // get() is blocking so it will wait for the completion of updateJob
            taskForUpdatingTheJob.get();
        });
        job.setStatus(Status.DONE);
        repository.save(job);

        // then
        assertTrue(thrownException.getCause() instanceof PessimisticLockingFailureException);
        assertEquals("DONE", job.getStatus().name());

    }

    private Future<Integer> prepareTaskForExecution(Job job) {
        Callable updateJob = getTask(job);
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<Integer> updateJobResult = executorService.submit(updateJob);
        return updateJobResult;
    }

    private UpdateJob getTask(Job job) {
        return new UpdateJob(job.getId());
    }

    @AllArgsConstructor
    private class UpdateJob implements Callable  {

        private Long id;

        @Override
        public Integer call() throws Exception {
            // given
            // a job with status NEW locked by a transaction
            var job = TestUtils.retrieveJobWithId(id, repository);

            // when
            // changing the job is mandatory otherwise the repository will not try to save to DB
            job.setUrl("Different URL");
            // it should throws an Exception
            var j = repository.save(job);

            throw new AssertionFailedError("It is not expect to run this statement");
        }
    }
}
