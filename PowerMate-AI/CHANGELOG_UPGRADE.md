# PowerMate AI — Cleanup & Fix Log

## What was wrong with the uploaded zip
The uploaded archive contained the same project nested inside itself multiple times:
- `PowerMate-AI-main/PowerMate-AI-main/...` (full duplicate of the repo)
- `PowerMate-AI-main/extracted/PowerMateAI/...` (an old, outdated snapshot — missing AOD
  styles, burn-in protection, and other newer features)
- `PowerMate-AI-main/patched/PowerMate-AI-main/...` (duplicate of the pre-patch baseline)
- `PowerMate-AI-main/patched/PowerMate-AI-fixed/...` (the **newest, most complete** version)
- Five separate embedded zip files inside the repo itself
  (`PowerMate-AI-patched.zip`, `PowerMate-AI-v2-patched.zip`, `PowerMate-AI-v3-fixed.zip`,
  `PowerMateAI.zip`, `PowerMate-AI-all-safe-features-changed-files-only.zip`)

I compared every copy byte-for-byte. `PowerMate-AI-v3-fixed.zip` and
`patched/PowerMate-AI-fixed/` were identical and were the most evolved version (most bug
fixes, most features — AOD style picker, burn-in protection, night dim, etc.), so that's
the version this rebuild is based on. Everything else was a stale duplicate and has been
removed. **No code or features were deleted** — only redundant copies of the same code.

## Logical bugs fixed in the code itself

1. **Charger/Cable diagnostic scores were broken** (`domain/scoring/Scoring.kt`)
   The weighted-score formula multiplied an already-0–100 percentage by a *second* raw
   weight (e.g. `* 35f` instead of `* 0.35f`). That pushed the math to 1000+ and the
   `coerceIn(0, 100)` clamp silently capped it — so charger/cable scores were almost
   always near 100 regardless of actual charging quality. This is the app's core
   "is my charger/cable good" feature, so this was a significant bug. Fixed to use
   fractional weights (`0.35f`, `0.20f`, `0.25f`, `0.20f`), matching the pattern already
   used correctly elsewhere in the same function and in `BatteryInsightsEngine.kt`.

2. **CSV history export existed but was completely unreachable** (`export/HistoryExportManager.kt`)
   The class could build a CSV file, but nothing in the UI ever called it, and there was
   no `FileProvider` to share the resulting file — so the feature was fully dead code.
   Wired it up:
   - Added an "Export history (CSV)" button to the Charging History screen
   - Registered a `FileProvider` in `AndroidManifest.xml`
   - Added `res/xml/file_paths.xml` pointing at the export cache folder
   - The button now writes the CSV and opens the Android share sheet

## What was intentionally left untouched
Per your request, no existing code or features were removed or rewritten — competitive
feature registry, AOD styles, widgets, charging coach tips, scoring weights elsewhere,
UI screens, etc. are all unchanged apart from the two fixes above.

## On "better than AmpereFlow/AccuBattery"
I can't make an objective claim that this beats those apps — that depends on real device
testing, polish, and marketing, not just code. What I *can* say: the core diagnostic
scoring (the feature those apps are best known for) was mathematically broken in your
codebase and is now fixed, and a built feature (CSV export) that wasn't reachable by
users now is. Those are concrete, real improvements.
