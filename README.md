# StandBy

An original Android app that turns your phone into a bedside / desk clock
while it charges — a faithful replica of **StandBy Mode: Clock & Widgets**
(Zetabit Tecnologia), rebuilt from scratch from a behavioural spec
(`workspace/rea-standby/replica-spec.md`) extracted from the original APK
with REA/apktool.

**Original implementation.** No code, strings, art, fonts or assets were
copied from the original app — the spec's screen names, labels and hex
colours were used as the design reference only, and every renderer, widget
and screen was written fresh. No ads, no Firebase, no billing, no tracking.
**Everything is free in this build — there is no Premium and no paywall.**

## v2 — what the replica has

- **All 97 screens** from the original's catalogue, in picker order
  (`model/Screens.kt`): every Duo clock, fullscreen variant, battery,
  weather, player, calendar, utility and fun screen, with the original's
  categories, explanations and search keywords.
- **Duo home model** — two side-by-side widget slots in landscape.
  Tap a slot to open its fullscreen counterpart (the original's
  counterpart map), long-press to replace it, empty slots say
  "Customize this space". Default partners follow the original
  (Calendar by default; Weather Hero → Dementia Clock; Matrix/Pixel Art →
  Player Duo; Player/Notification → System Dashboard; Striking Calendar →
  Analog Clock). Lock with "Swipe down with 3 fingers to unlock" plus an
  on-screen Unlock button; double-tap to close (when enabled).
- **Widget picker** — "Add a widget", search by name/category/keywords,
  category tabs, result count, per-screen explanations.
- **Faces, restyled to the spec** — Retroflip with all **33 styles**
  (incl. the spec's gradient stops), Big Crop Clock with all **38 themes**
  (per-digit pastel palettes, like the Play Store screenshots), Solar
  Watch with all **12 styles**, Segmented Clock with all **12 LED fonts**
  (7-segment Canvas, ghost digits, blinking separator), AMOLED, Neon
  (glow), Matrix rain, Pixel Art, Word Clock (11×10 glowing letter grid),
  Roman numerals, Analog family (Classic / Clean / Clean No Markers /
  Square / Square Mini / With Digital), Radial Invert, Dementia, Ants
  (marching-dot digits), Clock Of Clocks (grid of mini analog dials),
  World Clock (5 cities with offsets), Sereno, Clock Hero (sun arc),
  Vibes (mood from the playing track).
- **Widgets** — Charging screen (themes, milestone, ETA, speed label),
  Weather (Open-Meteo, free, no key — labelled "Powered by Open-Meteo";
  H/L, feels-like, humidity, wind, sunrise/sunset, hourly strip),
  Player Duo/Fullscreen/Blur/Hero + Meme Player (NotificationListener,
  transport only), Calendar month + Schedule agenda + Next Event +
  Striking Calendar, Quotes (own quote library, swipe + hourly
  auto-rotate), Timer (countdown + "Time's up!"), Stopwatch (laps),
  Pomodoro (25/5, foreground service + beep), Calculator (+ scientific),
  Tally, Todo, Sober Counter (discreet mode, relapse flow), Protocols
  (manifesto/rotate), Countdown, Command Center, Starship Console
  (LCARS-style), Concepts (all 34 tabs), Game Simon, Quick App Launch,
  Notification Mini (recent notifications, on-device only), System
  Dashboard, Day Centric Dashboard, Widget Rotator, Alarms (local,
  with snooze and a lock-screen ring screen), Photo Slide/Mini/Stack
  (your photos, centre-crop, private).
- **Settings** — the original's sections in order: General (face styles,
  photos, text size, start of week), Night Mode (manual / scheduled /
  light sensor, burn-in protection, day/night brightness), Auto Close
  (unplug / double-tap / portrait), App Launch (Quick Launch + device
  tips for Samsung/Huawei/Vivo/Xiaomi), Labs (Focus Mode, full-screen
  notifications, Export/Import settings backup), News, Support, About.
- **Onboarding** — the original's copy: "This is StandBy Mode Pro —
  An app that turns your Phone into a Smart Display when Charging",
  with the original's permission titles and descriptions.
- **Night mode** — red tint overlay + dim, manual / scheduled
  (22:00–07:00 default) / light-sensor option, AMOLED pure-black
  background, pixel-shift burn-in protection, sleep timer (15/30/60 min).
- **Auto-start on charging** — opens on plug-in, closes on unplug (both
  toggleable), re-arms after boot. Also works as the Android screensaver
  (Daydream): Settings → Display → Screen saver → StandBy.

## Cut from the original (not in this build)

- Third-party widget host (AppWidgetHost slots 1–6 show a placeholder)
- Lyrics tab in the player
- Weather radar
- GIFs in the Meme Player (captions only)
- Spaces profiles
- Focus-mode notification filtering (toggle present, filtering not wired)
- Seasonal effects
- Pomodoro History & Insights / achievements
- Custom YouTube-video alarm themes (local alarms ring via the
  lock-screen ring screen with a beep; no custom themes)
- Google sign-in / ideas board voting / account

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
