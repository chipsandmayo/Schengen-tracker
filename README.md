# Schengen 90/180 Tracker

A simple, single-file web app for counting the days you've spent in the Schengen area.

## How it works

- **Calendar tab** – tap the day you entered, then the day you left, and press **Add trip**. Use the arrows to go back to earlier months to add past visits (or type the dates under "Or type the dates").
- **My trips tab** – lists every trip, with how many of its days still count. Tap **Remove** to delete a trip entered by mistake.
- The header shows how many days you've used and how many are left **today**.

## The rule

You can stay up to **90 days in any rolling 180-day period**. The app looks back over the
last 180 days (including today) and counts every day you were in the area – entry and exit
days both count. Days in the calendar turn red if being there would put you over 90.

Your trips are saved in your browser (localStorage) on your device only.

## Running it

Open `index.html` in any browser, or turn on GitHub Pages for this repo
(Settings → Pages → deploy from the `main` branch) to use it on your phone.

*This is a planning aid, not legal advice. Check official sources before travelling.*
