## CAF_11021 - Invalid Storage Reference sent to Markup Worker ##

Verify that a task sent to Markup worker with an invalid storage reference is placed in the rejected queue after 10 retries.

**Test Steps**

1. Set up system to perform Markup and send a task message to the worker that contains a invalid storage reference
2. Examine the output

**Test Data**

Plain text files

**Expected Result**

The output message is returned with a status of RESULT_FAILURE

**JIRA Link** - [CAF-1704](https://jira.autonomy.com/browse/CAF-1704)




