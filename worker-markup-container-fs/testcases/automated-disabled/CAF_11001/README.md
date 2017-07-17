## CAF_11001 - Markup worker converts plain text from email into xml format ##

Process the plain text from email threads and convert it into xml format with the expected tags applied.

**Test Steps**

1. Send plain text from email threads into the Markup worker
2. Examine output

**Test Data**

Variety of plain text derived from email threads e.g. output from Text Extract worker.

**Expected Result**

The files are all processed and the plain text from email thread is wrapped correctly in xml format and placed into the output.xml file.

**JIRA Link** - [CAF-1438](https://jira.autonomy.com/browse/CAF-1438)

