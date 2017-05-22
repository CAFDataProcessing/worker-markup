## CAF_11031 - Markup headers in an email from the Sympa Mailing List System ##

Process plain text from email threads which are derived from the Sympa Mailing List System i.e. the headers are surrounded by asterisk characters.

**Test Steps**

1. Send plain text from Sympa email threads into the Markup worker.
2. Examine output

**Test Data**

Variety of plain text derived from email threads derived from the Sympa Mailing List System.

**Expected Result**

The files are all processed and the emails headers are successfully marked up. Headers in supported languages are also successfully marked up.

**JIRA Link** - [CAF-2189](https://jira.autonomy.com/browse/CAF-2189)

