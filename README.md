# StandBy Clock

An original Android app that turns your phone into a bedside / desk clock
while it charges — in the spirit of the iOS StandBy screen.

**Original implementation.** Inspired by the public feature description of
standby-clock apps; no code, strings, art or assets were copied from any
other app. No ads, no Firebase, no billing, no tracking.

## Features

- **144 clock faces** — 12 designs × 6 colours × 2 sizes, with a live-preview picker:
  Big Digital, Flip, Analog, Segmented LCD, Neon, Word Clock, Roman Numerals,
  Matrix, Pixel Art, Solar, World Clock (Phnom Penh / London / New York),
  Day Clock (full date + time of day in large text).
- **Standby screen** — large clock + date + battery % + charging ETA.
  Swipe left/right to change face, tap for settings, long-press for faces.
- **Duo layout** — clock on one side, a widget on the other. Swipe the widget
  side to cycle: Calendar agenda, Battery dashboard, Next alarm, Weather
  (Open-Meteo, free, no key), Music (track + play/pause/next), Photo frame
  (your own photos, slideshow), Timer/Stopwatch, Pomodoro (25/5, background
  foreground service + beep).
- **Night mode** — red tint + dim, manual / scheduled (22:00–07:00 default) /
  light-sensor option, AMOLED pure-black background, pixel-shift burn-in
  protection, sleep timer (15/30/60 min).
- **Auto-start on charging** — opens on plug-in, closes on unplug (both
  toggleable), re-arms after boot. Also works as the Android screensaver
  (Daydream): Settings → Display → Screen saver → StandBy Clock.
- **Settings** — face, text size, night schedule, day/night brightness,
  auto-start toggles, sleep timer, about.

## Build

Local Gradle builds are known to fail in the author's sandbox, so build in
GitHub Actions:

```yaml
# .github/workflows/build-apk.yml (add this in the repo)
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17
      - uses: gradle/actions/setup-gradle@v3
        with:
          gradle-version: 8.7
      - run: gradle :app:assembleDebug --stacktrace
      - uses: actions/upload-artifact@v4
        with:
          name: standby-clock-apk
          path: app/build/outputs/apk/debug/app-debug.apk
```

Or locally with Gradle 8.7 + JDK 17 installed:

```sh
gradle :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Note: `gradlew` in this repo is a thin launcher that delegates to an
installed Gradle 8.7; generate a full wrapper with
`gradle wrapper --gradle-version 8.7` if you want a self-contained one.

## Requirements

- Android 10+ (minSdk 29), targetSdk 34
- Kotlin 2.0, Jetpack Compose BOM 2024.09.00, Material3, AGP 8.5.2

## Permissions (all optional except the standby basics)

WAKE_LOCK, RECEIVE_BOOT_COMPLETED, FOREGROUND_SERVICE (+ media playback),
POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM, READ_CALENDAR, ACCESS_COARSE_LOCATION,
READ_MEDIA_IMAGES, SYSTEM_ALERT_WINDOW, DISABLE_KEYGUARD, INTERNET,
ACCESS_NETWORK_STATE. Calendar, location, photos and notification access
only unlock their widget; the clock works without them.
