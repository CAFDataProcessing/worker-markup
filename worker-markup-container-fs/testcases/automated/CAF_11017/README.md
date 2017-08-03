## CAF_11017 - isEmail Flag set to False ##

Process plain text from email threads and other document types with the isEmail flag set to false. 

**Test Steps**

1. Configure the markup worker with isEmail flag set to false
2. Send plain text from email threads into the Markup worker
3. Examine output

**Test Data**

Variety of plain text derived from email threads and other document types

**Expected Result**

The files will be processed successfully with the content being marked up in xml with only a CONTENT tag. Plain test of emails are not split and headers are not marked up. 

**JIRA Link** - [CAF-1650](https://jira.autonomy.com/browse/CAF-1650)

