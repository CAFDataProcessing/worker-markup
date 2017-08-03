## CAF_11010 - EML file that has an original email with no headers ##

Process the plain text from an EML email file that contains an original email with no headers, split the emails and convert it into xml format with the expected tags applied.

**Test Steps**

1. Send plain text from email threads into the Markup worker
2. Examine output

**Test Data**

Any plain text file derived from an EML email file that contains an original email and a reply. The original email is contained within the reply and has no headers. Additionally the signature of the reply is below the original email.

**Expected Result**

The file will be processed successfully with emails split, headers identified and tagged up along with additional properties mapped and tagged.

**JIRA Link** - [CAF-311](https://jira.autonomy.com/browse/CAF-311)



