package com.test.component;

import com.test.entity.Job;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends CrudRepository<Job, Long> {

    @Query(
            value = "SELECT TOP 1 * FROM JOB WHERE status = 'NEW' ORDER BY id FOR UPDATE",
            nativeQuery = true)
    public Job findNextAvailableJob();

}