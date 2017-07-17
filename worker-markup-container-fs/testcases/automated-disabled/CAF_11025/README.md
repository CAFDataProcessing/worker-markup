## CAF_11025 - Support for common languages in email headers ##

Process plain text from email threads which includes email headers that are in common languages such as English, French, German and Spanish.

**Test Steps**

1. Send plain text from email threads into the Markup worker with the email header mappings set for the common languages.
2. Examine output

**Test Data**

Variety of plain text derived from email threads e.g. output from Text Extract worker.

**Expected Result**

The files are all processed and the different language email headers are all successfully contained within the To, From, Sent and Subject xml tags.

**JIRA Link** - [CAF-2004](https://jira.autonomy.com/browse/CAF-2004)

