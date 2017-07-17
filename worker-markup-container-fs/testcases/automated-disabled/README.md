# Markup Worker Disabled Automated Test Cases

These testcases are currently not running as part of the nightly build as they require regeneration following changes in CAF-3229. There is also an investigation ongoing into time differences in field values that could affect hashing so for now we are delaying updating regeneration of these test cases to avoid regenerating them multiple times.

- [CAF_11001 - Markup worker converts plain text from email into xml format](CAF_11001)
- [CAF_11002 - Identify and markup headers](CAF_11002)
- [CAF_11005 - Add normalized hash to the email markup](CAF_11005)
- [CAF_11006 - Parse extended Exchange properties](CAF_11006)
- [CAF_11008 - Map MSG/EML conversation topic and index metadata field names](CAF_11008)
- [CAF_11010 - EML file that has an original email with no headers](CAF_11010)
- [CAF_11011 - Email file in format returned by Text Extract](CAF_11011)
- [CAF_11013 - Markup worker should ensure valid xml tags are created from header names](CAF_11013)
- [CAF_11014 - Provide facility to standardize the header names](CAF_11014)
- [CAF_11015 - Integrate the Natty Date Parser to standardise date formats](CAF_11015)
- [CAF_11016 - isEmail Flag set to True](CAF_11016)
- [CAF_11017 - isEmail Flag set to False](CAF_11017)
- [CAF_11018 - Map EML conversation topic and index metadata field names](CAF_11018)
- [CAF_11019 - Ensure parsed date and time is never inferred](CAF_11019)
- [CAF_11022 - Markup Worker generates Email Tracking fields](CAF_11022)
- [CAF_11023 - Email that contains a Date header](CAF_11023)
- [CAF_11025 - Support for common languages in email headers](CAF_11025)