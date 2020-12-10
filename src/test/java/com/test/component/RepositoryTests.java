package com.test.component;

import com.test.TestConfiguration;
import com.test.utils.TestUtils;
import com.test.entity.Job;
import com.test.entity.Status;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static com.test.utils.TestUtils.retrievedJobHasStatus;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;

@SpringBootTest(classes = TestConfiguration.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class RepositoryTests {

    @Autowired
    private JobRepository repository;

    @Test
    void canReadFromDB()  {
        // Just checking if it can read from database during tests
        // given

        // when
        Job job = TestUtils.retrieveJobWithId(1L, repository);

        // then
        retrievedJobHasStatus(job, Status.DONE);
    }

    @Test
    void nextAvailableJob()  {
        // given

        // when
        Job job = repository.findNextAvailableJob();

        // then
        retrievedJobHasStatus(job, Status.NEW);
    }


}
