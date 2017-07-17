## CAF_11016 - isEmail Flag set to True ##

Process plain text from email threads with the isEmail flag set to true. 

**Test Steps**

1. Configure the markup worker with isEmail flag set to true
2. Send plain text from email threads into the Markup worker
3. Examine output

**Test Data**

Variety of plain text derived from email threads

**Expected Result**

The files will be processed successfully with the emails split and headers marked up correctly.

**JIRA Link** - [CAF-1650](https://jira.autonomy.com/browse/CAF-1650)

