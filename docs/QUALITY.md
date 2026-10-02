# Quality Audit

Scope: source review and GitHub repository preparation, 3 October 2026.
This is not an independent penetration test or a long-running TV certification.

## Findings Addressed

- Forecast consistency: multiple independently updated volatile fields could
  combine values from different refreshes. One immutable WeatherData snapshot
  is now published atomically and consumed consistently for each rendered frame.
- Lifecycle race: an old request could finish after stop/start and overwrite
  newer data. A generation guard now rejects cancelled/stale worker results.
- Invalid inputs: non-finite/negative wind and future cache timestamps could
  produce bad animation coordinates or indefinitely fresh weather. Wind is
  bounded; missing, future and 24-hour-old data use a neutral scene.
- Response size: forecast downloads are limited to 1 MiB and use timeouts.
- Image decoding: required snow/rain bitmaps now reject invalid decode results.
- Render allocation: fixed exposure arrays/shaders are reused and exposure
  filters only rebuild when their visible value changes.
- Production cleanup: unused accelerated-demo functions were removed;
  remaining helpers are named LightingTimeline and WeatherScenario.
- Backup rules cover both pre-Android-12 and newer devices. The landscape lint
  exception is restricted to this Leanback-only TV activity and documented.
- Repository hygiene: signing files, passwords, .env files, private videos,
  source references, SDK paths, build outputs and TV captures are excluded.
- Supply-chain basics: Gradle distribution SHA-256 is pinned; GitHub Actions
  use verified commit hashes with read-only repository permissions.
- Documentation: private device address removed; install/build instructions,
  image attribution, source-license status and limitations are explicit.

## Verification

Local validation uses the checked-in Gradle wrapper:

```powershell
.\gradlew.bat assembleDebug assembleRelease testDebugUnitTest lintDebug lintRelease
```

Local debug/unsigned-release builds passed; both lint variants report no issues.
The Gradle wrapper JAR checksum matches the upstream 8.10.2 checksum.
GitHub YAML and Android XML parse checks passed. The publishable working tree
is approximately 31 MiB, with no individual file near 100 MiB.

The unit suite has 10 tests covering WMO mappings, particle limits, every
minute's lighting segment, Istanbul timezone/date rollover, solar boundaries,
stale-cache policy and malformed wind. Release signing is also validated with
Build-Release.ps1; private keys remain outside Git.

Repository checks inspect ignored/private paths, publishable file sizes,
personal-address/key-pattern matches and Git metadata. These checks are
best-effort safeguards, not proof that every possible secret has been detected.

GitHub Actions has been configured; its hosted run cannot be verified until
the repository is pushed. Local Android checks do not exercise the Java network
parser or lifecycle race under instrumentation. Live API/TV behavior had a
physical Android 14 smoke test on the preceding release; this repository's
latest code should receive a fresh on-device smoke test before distribution.

## Remaining Limitations

- No emulator/device matrix, network-outage instrumentation or 24-hour soak test.
- No claim of native 4K source-image fidelity; assets are roughly 1670x942.
- Manual tree/bird masks and wet-scene exposure interpolation are visual approximations.
- API access/licensing and photo share-alike obligations require attention for
  redistribution. Source-code license is awaiting the owner's choice.
- Release-key/password backup and machine migration are maintainer responsibilities.
- Signing identity and applicationId must remain stable for installed updates.
