#### Version Number
${version-number}

#### New Features
- [CAF-3561](https://jira.autonomy.com/browse/CAF-3561): Email headers can now be built for use during markup using document metadata.
  For emails where the root email headers are not included in the content of the email, markup can now be configured to build the email headers instead, making them available for use in the output fields operation. This option is controlled via the ```addEmailHeadersDuringMarkup``` configuration option.
  
- [CAF-3821](https://jira.autonomy.com/browse/CAF-3821): Supporting handling epoch date values as ISO-8601 during markup.
  New mapping option added to transform a field value from epoch format to ISO-8601 during markup. This is the format that dates read from the body of an email are output in so this allows email header to be constructed for use in output fields.

#### Known Issues
