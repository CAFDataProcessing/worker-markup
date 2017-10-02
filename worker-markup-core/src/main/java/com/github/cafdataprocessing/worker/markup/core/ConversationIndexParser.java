/*
 * Copyright 2015-2017 EntIT Software LLC, a Micro Focus company.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cafdataprocessing.worker.markup.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class ConversationIndexParser
{
    private static final String ERR_CONVERSATION_INDEX_LENGTH_UNEXPECTED = "Unexpected conversation index length. Should be 22 bytes plus extra 5 bytes per child mail message.";
    private static final String ERR_FAILED_TO_SERIALIZE_TO_JSON = "Failed to serialize the MailMessageConversationIndex object to JSON";
    private static final long TICKS_PER_MILLISECOND = 10000;
    private static final long MILLISECOND_PER_SECOND = 1000;
    private static final String WIN32_EPOCH_DATE_STRING = "1601-01-01T00:00:00Z";

    private static final Logger LOG = LoggerFactory.getLogger(ConversationIndexParser.class);

    static class MailMessageConversationIndex
    {
        private final Instant headerDate;
        private final UUID guid;
        private ArrayList<ChildMailMessage> childMessages;

        public MailMessageConversationIndex(final Instant headerDate, final UUID guid)
        {
            this.headerDate = headerDate;
            this.guid = guid;
            this.childMessages = new ArrayList<>();
        }

        public String getHeaderDate()
        {
            return headerDate.toString();
        }

        public UUID getGuid()
        {
            return guid;
        }

        public void setChildMessages(ArrayList<ChildMailMessage> childMessages)
        {
            this.childMessages = childMessages;
        }

        public ArrayList<ChildMailMessage> getChildMessages()
        {
            return childMessages;
        }
    }

    static class ChildMailMessage
    {
        private final Instant messageDate;
        private final int randomNo;
        private final int sequenceCount;

        public ChildMailMessage(final Instant messageDate, final int randomNo, final int sequenceCount)
        {
            this.messageDate = messageDate;
            this.randomNo = randomNo;
            this.sequenceCount = sequenceCount;
        }

        public String getMessageDate()
        {
            return messageDate.toString();
        }

        public int getRandomNo()
        {
            return randomNo;
        }

        public int getSequenceCount()
        {
            return sequenceCount;
        }
    }

    /**
     * Parses the mail message conversation index value and returns its components in a JSON string.
     *
     * @param conversationIndex - base64 encoded PR_CONVERSATION_INDEX value.
     * @return String
     * @throws ConversationIndexParserException
     */
    public static String parseConversationIndex(final String conversationIndex) throws ConversationIndexParserException
    {
        LOG.info("parseConversationIndex: Parsing mail message conversation index ...");

        String parsedConversationJSON = "";
        ObjectMapper mapper = new ObjectMapper();

        //  Mail message conversation index expected to be base64 encoded.
        LOG.info("parseConversationIndex: Decoding conversation index value ...");
        byte[] bytes = Base64.decodeBase64(conversationIndex);

        //  Conversation index length should be 22 bytes plus extra 5 bytes per child mail message.
        if (bytes.length < 22 || (bytes.length - 22) % 5 != 0) {
            throw new ConversationIndexParserException(ERR_CONVERSATION_INDEX_LENGTH_UNEXPECTED);
        }

        //  Bytes 0-5 used for header date.
        //  Bytes 6-22 used for unique identifier.
        //  Remaining bytes used for reply messages in blocks of 5 bytes.
        //  Identify header date from first 6 bytes.
        LOG.info("parseConversationIndex: Identifying header date value from conversation index bytes.");
        byte[] headerDateBytes = Arrays.copyOfRange(bytes, 0, 6);
        ArrayList<Byte> list = new ArrayList<>();
        for (byte b : headerDateBytes) {
            list.add(b);
        }

        //  Get ticks from the start of January 1, 1601 (i.e. Win32 Epoch).
        Optional<Long> longValue = list.stream()
            .map(b -> (long) b & 0xFF)
            .reduce((l1, l2) -> (l1 << 8) + l2);
        long ticks = longValue.get() << 16;

        //  Identify ms since the start of January 1, 1601.
        long msFromWin32Epoch = (ticks / TICKS_PER_MILLISECOND);

        //  Identify time difference in ms between Win32 Epoch and Unix time.
        Instant win32Epoch = Instant.parse(WIN32_EPOCH_DATE_STRING);
        Instant epoch = Instant.EPOCH;
        long diffInMillis = (epoch.getEpochSecond() - win32Epoch.getEpochSecond()) * MILLISECOND_PER_SECOND;

        //  Now determine ms since EPOCH.
        long msFromEpoch = (msFromWin32Epoch - diffInMillis);

        //  Generate java Instant value based on ms from Unix time.
        Instant dateInstant = Instant.ofEpochMilli(msFromEpoch);

        //  Generate unique identifier based on bytes 6-22.
        LOG.info("parseConversationIndex: Generating unique identifier from conversation index bytes.");
        byte[] guidBytes = Arrays.copyOfRange(bytes, 6, 22);
        ByteBuffer bb = ByteBuffer.wrap(guidBytes);
        UUID id = new UUID(bb.getLong(), bb.getLong());

        //  Create new instance of MailMessageConversationIndex.
        MailMessageConversationIndex mmci = new MailMessageConversationIndex(dateInstant, id);

        //  Parse reply message bytes in conversation index value.
        //  Expected 5 byte block per child message.
        int childBlockCount = (bytes.length - 22) / 5;

        ArrayList<ChildMailMessage> childMessages = new ArrayList<>();
        Instant previousChildMessageInstant = dateInstant;
        for (int i = 0; i < childBlockCount; i++) {
            LOG.info("parseConversationIndex: Parsing child message metadata.");

            //  Bytes 0-3 used for the time difference in child messages.
            //  Byte 4 used for for random number and sequence count.
            int copyOfRangeFrom = 22 + i * 5;
            int copyOfRange = copyOfRangeFrom + 5;

            byte[] childMessageBytes = Arrays.copyOfRange(bytes, copyOfRangeFrom, copyOfRange);
            byte[] childMessageDateBytes = Arrays.copyOfRange(childMessageBytes, 0, 4);
            byte[] childRemainderBytes = Arrays.copyOfRange(childMessageBytes, 4, 5);

            //  Parse first 4 bytes for child message time difference.
            LOG.info("parseConversationIndex: Identifying date of the child message.");
            ArrayList<Byte> childMessageDate = new ArrayList<>();
            for (byte b : childMessageDateBytes) {
                childMessageDate.add(b);
            }

            Optional<Long> childLongValue = childMessageDate.stream()
                .map(b -> (long) b & 0xFF)
                .reduce((l1, l2) -> (l1 << 8) + l2);
            long childTicks = childLongValue.get() << 18;
            childTicks = childTicks & ~((long) 1 << 50);

            int milliSecs = (int) (childTicks / TICKS_PER_MILLISECOND);
            Instant childMessageInstant = previousChildMessageInstant.plusMillis(milliSecs);
            previousChildMessageInstant = childMessageInstant;

            //  Parse final byte to get random number and sequence count.
            //  First 4 bits for Random Number, last 4 bits for Sequence Count.
            LOG.info("parseConversationIndex: Identifying random number and sequence count.");
            int randomNo = ((int) childRemainderBytes[0] & 0xFF) >> 4;
            int sequenceCount = ((int) childRemainderBytes[0] & 0xFF) >> 8;
            ChildMailMessage cmm = new ChildMailMessage(childMessageInstant, randomNo, sequenceCount);

            //  Add child message data to array of child messages.
            childMessages.add(cmm);
        }

        //  Add list of child messages to the conversation index instance.
        mmci.setChildMessages(childMessages);

        LOG.info("parseConversationIndex: Mail message conversation index parsing complete.");

        //  Return parsed conversation index value as JSON string.
        try {
            parsedConversationJSON = mapper.writeValueAsString(mmci);
        } catch (JsonProcessingException jpe) {
            throw new ConversationIndexParserException(ERR_FAILED_TO_SERIALIZE_TO_JSON, jpe);
        }

        return parsedConversationJSON;
    }
}
