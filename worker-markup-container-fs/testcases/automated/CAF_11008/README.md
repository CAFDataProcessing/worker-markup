## CAF_11008 - Map MSG conversation topic and index metadata field names ##

Map expected metadata from MSG documents onto the expected metadata on the document. For example MSG refers to conversationtopic and caf-mail-conversation-index.

**Test Steps**

1. Send plain text from MSG email threads into the Markup worker passing conversationtopic, caf-mail-conversation-index, internetmessageid and caf-mail-in-reply-to properties.
2. Examine output

**Test Data**

Variety of plain text derived from MSG email threads e.g. output from Text Extract worker.

**Expected Result**

The files are all processed and the properties are mapped as follows:

- conversationtopic -> `CAF_MAIL_CONVERSATION_TOPIC`
- caf-mail-conversaton-index -> `CAF_MAIL_CONVERSATION_INDEX`
- internetmessageid -> `CAF_MAIL_MESSAGE_ID`
- caf-mail-in-reply-to -> `CAF_MAIL_IN_REPLY_TO`

**JIRA Link** - [CAF-1546](https://jira.autonomy.com/browse/CAF-1546)

