## CAF_11019 - Ensure parsed date and time is never inferred ##

Verify that all dates parsed within headers are only definitive dates and not inferred.

**Test Steps**

1. Send plain text from email threads in which the headers contain examples such as "Attachments: KeyView_FilterSDK_10.25.0.0_JavaProgramming_en.pdf" or "Subject: Test Email 6" into the Markup worker set isEmail flag to true.
2. Examine output.

**Test Data**

Variety of plain text derived from email threads in which the headers contain examples such as "Attachments: KeyView_FilterSDK_10.25.0.0_JavaProgramming_en.pdf" or "Subject: Test Email 6".

**Expected Result**

The files are all processed and only the headers which contain definitive dates e.g. Sent or any header which has a date set in it are parsed. All other examples as above are not parsed as dates.

**JIRA Link** - [CAF-1724](https://jira.autonomy.com/browse/CAF-1724)


