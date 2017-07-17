## CAF_11015 - Integrate the Natty Date Parser to standardise date formats ##

Process plain text from email threads where the dates are in different formats and from different time zones

**Test Steps**

1. Configure the markup worker with the required outputFields for dateUtc
2. Send plain text from email threads into the Markup worker where the date headers are in different formats and from different time zones
3. Examine output

**Test Data**

Variety of plain text derived from email threads where the date headers are in different formats and from different time zones

**Expected Result**

The files will be processed successfully and the date formats and time zones will be standardised into UTC format e.g. "2016-10-07T12:21:00Z".

**JIRA Link** - [CAF-1695](https://jira.autonomy.com/browse/CAF-1695)

