## CAF_11018 - Map EML conversation topic and index metadata field names ##

Map expected metadata from EML documents onto the expected metadata on the document. For example EML refers to thread-topic and thread-index. 

**Test Steps**

1. Send plain text from EML email threads into the Markup worker passing thread-topic, thread-index, message-id and in-reply-to properties.
2. Examine output

**Test Data**

Variety of plain text derived from EML email threads e.g. output from Text Extract worker.

**Expected Result**

The files are all processed and the properties are mapped as follows:

- thread-topic -> `CAF_MAIL_CONVERSATION_TOPIC`
- thread-index -> `CAF_MAIL_CONVERSATION_INDEX`
- message-id -> `CAF_MAIL_MESSAGE_ID`
- in-reply-to -> `CAF_MAIL_IN_REPLY_TO`

**JIRA Link** - [CAF-1685](https://jira.autonomy.com/browse/CAF-1685)

**Actual Result**

The `CAF_MAIL_CONVERSATION_TOPIC` and `CAF_MAIL_CONVERSATION_INDEX` properties are not output due to the incorrect EML fields being mapped.

