## CAF_11023 - Email that contains a Date header ##

Verify that an email that contains a header called Date is correctly processed by the Markup Worker.

**Test Steps**

1. Send plain text from email threads some of which hate a header called Date instead of Sent
2. Examine output.

**Test Data**

Variety of plain text derived from email threads, some of which have a Date header.

**Expected Result**

The files are all processed, the SECTION_SORT, SECTION_ID, ROOT_ID and PARENT_ID fields are generated and are correct for the email thread. Also the email was split up as expected into the correct number of emails.

**JIRA Link** - [CAF-1712](https://jira.autonomy.com/browse/CAF-1712)


