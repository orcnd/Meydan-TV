# Contributing

Use JDK 17 and Android SDK 35. Keep changes focused and retain the asset credits.
No running TV, network service, or signing key is required for local unit tests.

```sh
bash ./gradlew assembleDebug testDebugUnitTest lintDebug lintRelease
```

On Windows use `./gradlew.bat` instead of `bash ./gradlew`. Include tests for new
weather mappings, stale-data policy, clock/date boundaries and solar behavior.
Do not add device addresses, credentials, signing keys, private videos, generated
build outputs, or TV screenshots to commits. Check `git diff --check` before a PR.

Release signing belongs to the maintainer; CI builds unsigned release and signed
debug packages without private release credentials. Do not change applicationId
or signing-key handling when making routine updates: both affect installed updates.

Photo asset changes need the same attribution and CC BY-SA 4.0 license as their
source. Do not assume the source-code license applies to adapted photographs.
