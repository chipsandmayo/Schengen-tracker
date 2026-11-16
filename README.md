# Schengen 90/180 Tracker

An Android app, written in Kotlin with Jetpack Compose, that counts the days you've spent in the Schengen area.

## Features

- **Calendar tab**: tap the day you entered and the day you left, then press **Add trip**.
  Scroll up to earlier months to add past visits, or tap the pencil icon to type the dates.
  For a one-day visit, tap the day once and press **Add trip**.
- **My trips tab**: every trip, its length, how many of its days still count, and whether it
  goes over the limit. Tap the bin icon to remove a trip you added by mistake.
- **Summary**: days used and days left today, at the top of the screen.
- Trips are saved on your phone.

## The rule

You can stay up to **90 days in any rolling 180-day period**. For a given day, the app looks back
over the 180 days ending on that day, including that day, and counts every day you were in the area.
Entry and exit days both count, and days where trips overlap are only counted once.

## Getting the app on your phone

Every push to GitHub builds the app automatically:

1. Open the **Actions** tab of this repo and click the latest **Build APK** run.
2. Download the **schengen-tracker-apk** artifact (a zip), and unzip it to get `app-debug.apk`.
3. Copy it to your Android phone and open it. You'll need to allow installing apps from that source.

Or open the project in Android Studio and press Run.

## Project layout

- `SchengenRules.kt`: the 90/180 counting logic (unit tested in `SchengenRulesTest.kt`)
- `TripStore.kt`: saves trips on the device
- `ui/SchengenApp.kt`: the screens

*This is a planning aid, not legal advice. Check official sources before travelling.*
