# Tech Choices

There is the chance that a job remains in PROCESSING state because the transaction in  
JobService.findNextAvailableJobAndSetStatusToProcessing() doesn't include:
- the call to the URL and
- the subsequent write to database for setting job state to DONE and setting the http status code returned.

This gives the advantage of locking the job row for the minimum amount of time, but it calls for:
- a batch that periodically checks if there are jobs that have been in the PROCESSING state for a time greater than Workers.Runner.HTTP_TIMEOUT and
- an additional column that records the starting time of the PROCESSING state

This could be avoided spanning the database transaction from the moment the worker asks for the next available job till
it write the new status and the http code returned calling its URL. With this approach the status would be DONE or ERROR
depending on the call's result or NEW if the transaction is rolled back for some other reason.
