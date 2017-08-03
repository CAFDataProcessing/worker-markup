## CAF_11002 - Identify and markup headers ##

Process plain text from email thread, identify headers and markup in xml

**Test Steps**

1. Send plain text from email threads into the Markup worker
2. Examine output

**Test Data**

Variety of plain text derived from email threads e.g. output from Text Extract worker.

**Expected Result**

The files are all processed, the headers (e.g. From, To, Subject, Importance etc.) are all successfully identified and marked up in xml format with valid tags added to the output.xml file.

**JIRA Link** - [CAF-1441](https://jira.autonomy.com/browse/CAF-1441)

