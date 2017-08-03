## CAF_11014 - Provide facility to standardize the header names ##

Process plain text from email threads where the headers are not the common header names

**Test Steps**

1. Configure the markup worker with the required emailHeaderMappings
2. Send plain text from email threads into the Markup worker where the headers are not the common header names.
3. Examine output

**Test Data**

Variety of plain text derived from email threads where the headers are not the common header names

**Expected Result**

The files will be processed successfully and the uncommon header names that have been added to the emailHeaderMappings will be correctly mapped to standard header names and xml tag created.

**JIRA Link** - [CAF-1442](https://jira.autonomy.com/browse/CAF-1442)


