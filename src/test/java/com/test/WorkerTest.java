package com.test;

import com.test.component.JobService;
import com.test.entity.Job;
import com.test.entity.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = TestConfiguration.class)
@ActiveProfiles("test")
class WorkerTest {

    @Autowired
    private Worker worker;

    int successHttpStatusCode = 200;

    @Test
    void getNextAvailableJobShouldAskServiceForIt() {
        // given
        Job jobMock = prepareJobMock();
        JobService service = prepareServiceMockForReturningAsNextAvailableJob(jobMock);
        HttpClient httoCLientMock = mock(HttpClient.class);
        worker = new Worker(service, httoCLientMock);

        // when
        Job actualJob = worker.getNextAvailableJob();

        // then
        assertEquals(jobMock, actualJob);
    }

    private JobService prepareServiceMockForReturningAsNextAvailableJob(Job job) {
        JobService service = mock(JobService.class);
        when(service.findNextAvailableJobAndSetStatusToProcessing()).thenReturn(job);
        return service;
    }

    @Test
    void callURLAndSetJobStatusShouldCallHttpClientAndSetHttpStatusCode() throws Exception {
        // given
        Job jobMock = prepareJobMock();
        HttpResponse httpResponseMock = prepareHttpResponseMock();
        HttpClient httpClientMock = prepareHttpClientMockToReturnHttpResponse(httpResponseMock);
        JobService service = prepareJobServiceMockToChangeStatusTo(jobMock);

        // it is needed to reinstantiate worker with mocked httpClient
        worker = new Worker(service, httpClientMock);

        // when
        worker.callURLAndSetJobStatus(jobMock);

        // then
        verify(service).setJobStatusTo(jobMock, Status.DONE, successHttpStatusCode);
    }

    private JobService prepareJobServiceMockToChangeStatusTo(Job jobMock) {
        JobService service = mock(JobService.class);
        when(service.setJobStatusTo(eq(jobMock), any(), any())).thenReturn(jobMock);
        return service;
    }

    private Job prepareJobMock() {
        Job job = mock(Job.class);
        when(job.getUrl()).thenReturn("https://www.google.it");
        return job;
    }

    private HttpResponse prepareHttpResponseMock() {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(successHttpStatusCode);
        return httpResponse;
    }

    private HttpClient prepareHttpClientMockToReturnHttpResponse(HttpResponse httpResponseMock) throws IOException, InterruptedException {
        HttpClient httpClientMock = mock(HttpClient.class);
        when(httpClientMock.send(any(), any())).thenReturn(httpResponseMock).getMock();
        return httpClientMock;
    }

    @Test
    void callURLAndSetJobStatusShouldSetToErrorWhenCallThrowsException() throws Exception {
        // given
        Job jobMock = prepareJobMock();
        HttpClient httpClientMock = prepareHttpClientMockToThrowException();
        JobService service = prepareJobServiceMockToChangeStatusTo(jobMock);
        // need to reinstantiate worker with mocked httpClient
        worker = new Worker(service, httpClientMock);

        // when
        worker.callURLAndSetJobStatus(jobMock);

        // then
        verify(service).setJobStatusTo(jobMock, Status.ERROR, null);
    }

    private HttpClient prepareHttpClientMockToThrowException() throws IOException, InterruptedException {
        HttpClient httpClient = mock(HttpClient.class);
        Exception fakeIOException = new IOException("Fake exception");
        when(httpClient.send(any(), any())).thenThrow(fakeIOException);
        return httpClient;
    }

}
