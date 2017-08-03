## CAF_11005 - Add normalized hash to the email markup ##

Process plain text from email threads, remove the whitespace from the email body and then hash the resultant text.

**Test Steps**

1. Send plain text from email threads into the Markup worker
2. Examine output

**Test Data**

Variety of plain text derived from email threads e.g. output from Text Extract worker.

**Expected Result**

The files are all processed and the email body is stripped of all whitespace and the result text is then hashed. This hash value is then added along with hash configuration (type of normalization and hash function) to the output.xml. 

**JIRA Link** - [CAF-1445](https://jira.autonomy.com/browse/CAF-1445)

