# Security Notes

PowerMate AI should remain a low-risk offline utility.

## Rules

- Do not request contacts, location, microphone, camera, or broad file storage permissions unless a future feature absolutely requires it.
- Do not use root-only or private OEM APIs.
- Do not fake sensor data.
- Keep background work limited and visible.
- Use foreground services only when needed.
- Keep all AOD/overlay behavior user-initiated.

## Release checks

- Verify release build with minification on.
- Verify app does not crash on unsupported current sensors.
- Verify notifications are permission-gated on Android 13+.
- Verify history clear works.
