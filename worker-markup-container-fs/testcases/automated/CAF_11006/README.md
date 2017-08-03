## CAF_11006 - Parse extended Email Conversation Index property ##

Process plain text from email threads and parse the additional Email Conversation Index property that the Text Worker extracts.

**Test Steps**

1. Send plain text from email threads into the Markup worker
2. Examine output

**Test Data**

Variety of plain text derived from email threads e.g. output from Text Extract worker.

**Expected Result**

The files are all processed and the Email Conversation Index is parsed and added to the output.xml file with correct tags applied.

**JIRA Link** - [CAF-1449](https://jira.autonomy.com/browse/CAF-1449)

