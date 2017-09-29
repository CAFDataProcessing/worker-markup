#### Version Number
${version-number}

#### New Features
- [CAF-3561](https://jira.autonomy.com/browse/CAF-3561): Email headers can now be built for use during markup using document metadata.
  For emails where the root email headers are not included in the content of the email, markup can now be configured to build the email headers instead, making them available for use in the output fields operation. This option is controlled via the ```addEmailHeadersDuringMarkup``` configuration option.

#### Known Issues
