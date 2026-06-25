# PowerMate AI Release Checklist

## Build

- [ ] `gradle testDebugUnitTest`
- [ ] `gradle :app:assembleDebug`
- [ ] `gradle :app:assembleRelease`
- [ ] `gradle :app:bundleRelease`
- [ ] GitHub Actions green

## Device QA

- [ ] Android 8/8.1
- [ ] Android 9
- [ ] Android 11
- [ ] Android 13
- [ ] Android 14
- [ ] Android 15
- [ ] Android 16
- [ ] Samsung device
- [ ] Xiaomi/Redmi device
- [ ] Low RAM device

## Feature QA

- [ ] Dashboard loads
- [ ] Charging status changes on plug/unplug
- [ ] Unsupported current sensor fallback works
- [ ] Diagnostic completes
- [ ] History saves
- [ ] Alerts trigger
- [ ] AOD-style screen opens and closes
- [ ] Widget updates
- [ ] Settings persist
- [ ] Clear history works

## Play Store

- [ ] App icon
- [ ] Feature graphic
- [ ] Screenshots
- [ ] Privacy policy URL
- [ ] Data Safety form
- [ ] Signed AAB uploaded
