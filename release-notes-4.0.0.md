!not-ready-for-release!

#### Version Number
${version-number}

#### New Features

#### Breaking Changes
- **US361030**: Java 8 and Java 11 support dropped  
  Java 17 is now the minimum supported version.

- **US361030**: Jakarta EE version update  
  The version of Jakarta EE used for validation and other purposes has been updated
  from Jakarta EE 8 to Jakarta EE 9.  This may mean that `javax.*` imports in worker
  code need to be updated to `jakarta.*` instead.

- **D854021:** Worker Framwork V4 Format message support dropped  
  The worker has been updated to use a new version of the worker framework which no longer supports the V4 format message.

#### Known Issues
