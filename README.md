# Meydan TV

![Meydan TV identity](app/src/main/res/drawable-nodpi/meydan_banner.png)

A fullscreen native Google TV ambient scene of Mevlana Square and Selimiye
Mosque in Konya. A fixed photograph changes lighting with the real local time
and shows animated weather from live forecast data. This is not a camera feed.

## Features

- Live Turkish date/time in Europe/Istanbul, independent of the TV timezone.
- Konya weather, sunrise/sunset and wind drive the scene.
- Alanya and Konya current conditions plus tomorrow's min/max temperatures.
- Clear/partly-cloudy daylight birds, fog, clouds, rain, snow and hail effects.
- Subtle wind-based motion in two foreground trees.
- Forecast refresh every five minutes, with persistent cached data.
- No accelerated demo, accounts, ads, tracking or device-location permission.

## Build

Requirements: JDK 17, Android SDK Platform 35 and Build Tools 35.0.0. Gradle
8.10.2 is supplied by the wrapper and its distribution checksum is verified.
Use Android Studio or set ANDROID_HOME/ANDROID_SDK_ROOT to your SDK directory.
Alternatively create an untracked `local.properties` with `sdk.dir=...`.

Windows:

```powershell
.\gradlew.bat assembleDebug assembleRelease testDebugUnitTest lintDebug lintRelease
```

Linux/macOS:

```sh
bash ./gradlew assembleDebug assembleRelease testDebugUnitTest lintDebug lintRelease
```

Outputs: `app/build/outputs/apk/debug/app-debug.apk` and, without signing
environment variables, `app/build/outputs/apk/release/app-release-unsigned.apk`.
The unsigned release is useful for validation, not installation.
Android TV/Google TV API 23+ is the declared target; physical testing so far
has been on one Android 14 TV with a 1920x1080 render surface.

## Install

Enable developer mode and wireless debugging on your TV. Pair/connect with
ADB using the address shown by the TV; never commit its pairing code or address.

```powershell
$env:TV_ADDRESS = 'YOUR_TV_IP:DEBUG_PORT'
adb -s $env:TV_ADDRESS install -r app/build/outputs/apk/debug/app-debug.apk
adb -s $env:TV_ADDRESS shell am start -n com.example.loopingtvplayer/.MainActivity
```

The launcher name is **Meydan TV**. Different signing certificates cannot update
each other; moving from debug to release requires one uninstall, which removes
app data. Back up any older local videos before doing this. Routine same-key
release upgrades use `install -r` without uninstalling.

## Signed Release

On Windows, with keytool available:

```powershell
.\Build-Release.ps1
```

This creates/reuses a private release key outside the repository under
`%LOCALAPPDATA%/MeydanTV/signing/`. Its password is encrypted with Windows DPAPI
for the current user/machine. The output is `app/build/outputs/apk/release/app-release.apk`.
Back up the keystore and a recoverable password securely. The encrypted
password.xml by itself cannot be decrypted on another machine.

For manual/CI signing, set MEYDAN_KEYSTORE and MEYDAN_STORE_PASSWORD securely,
then run the Gradle release task directly. The key alias is `meydan-tv`.
Do not store these values in source files or GitHub Actions logs.

## Data and Limitations

Open-Meteo is queried over HTTPS with fixed Alanya/Konya coordinates. Responses
are published atomically so the scene and labels use a consistent forecast.
If requests fail, the last successful data is retained and marked as cached.
After 24 hours without fresh data, precipitation is disabled and a neutral
overcast scene is used. Solar data from a previous date is not displayed.

Wet lighting uses exposure interpolation; clouds, tree motion and bird
occlusion are approximations. Snow/wetness already painted into backgrounds
is static; there is no animated accumulation. No multi-device, 4K fidelity,
long-duration reliability or network-outage certification is claimed.

The free API is intended for noncommercial usage. Check [Open-Meteo terms](https://open-meteo.com/en/terms)
and [data licensing](https://open-meteo.com/en/licence) before commercial distribution.
This project is prepared for sideloading, not submitted to Google Play.

## Repository Quality

GitHub Actions builds debug/unsigned release packages and runs unit tests and
Android lint. Actions are pinned to commit hashes; Dependabot checks updates.
CI does not need TV access or private release credentials.
See [quality notes](docs/QUALITY.md), [contributing](CONTRIBUTING.md) and
[security](SECURITY.md).

Private videos, source references, TV screenshots/backups, build outputs,
local SDK paths and signing credentials are ignored. Their local copies
are not deleted. PNG scene assets are included (each below GitHub's file limit).

## Licensing

Source-code licensing has not yet been selected by the repository owner;
do not treat this as an MIT-licensed project until a code license is added.
The adapted scene photographs are separately **CC BY-SA 4.0**. The generated
brand artwork is separate from those photo adaptations. See [asset credits](app/src/main/assets/PHOTO_CREDITS.txt)
and [third-party notices](THIRD_PARTY_NOTICES.md).
