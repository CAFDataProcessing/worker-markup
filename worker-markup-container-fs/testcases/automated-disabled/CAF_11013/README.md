## CAF_11013 - Markup worker should ensure valid xml tags are created from header names ##

Process plain text from email threads where the headers contain characters which are invalid for xml tag names.

**Test Steps**

1. Send plain text from email threads into the Markup worker where the headers contain characters which are invlaid for use within an xml tag name.
2. Examine output

**Test Data**

Variety of plain text derived from email threads where the headers contain characters which are invalid for use within xml tag names e.g. spaces, special characters

**Expected Result**

The files will be processed successfully with emails split, headers identified and tagged up along with additional properties mapped and tagged. The invalid characters will be removed and the xml tag names created without the invalid characaters.
Note that if a header contained only invalid character the header would be set to "UnreadableHeader".

**JIRA Link** - [CAF-1646](https://jira.autonomy.com/browse/CAF-1646)

