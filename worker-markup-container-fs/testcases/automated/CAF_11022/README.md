## CAF_11022 - Markup Worker generates Email Tracking fields ##

Verify that fields required for email tracking are generated from plain text derived from emails.

**Test Steps**

1. Send plain text from email threads into the Markup worker with SECTION_SORT, SECTION_ID, ROOT_ID and PARENT_ID defined as output fields.
2. Examine output.

**Test Data**

Variety of plain text derived from email threads.

**Expected Result**

The files are all processed and the SECTION_SORT, SECTION_ID, ROOT_ID and PARENT_ID fields are generated and are correct for the email thread.

**JIRA Link** - [CAF-311](https://jira.autonomy.com/browse/CAF-311)


