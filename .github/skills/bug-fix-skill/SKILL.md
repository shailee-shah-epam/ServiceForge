---
name: bug-fix-skill
description: Use whenever identifying or fixing a bug in this repo's backend (Java/Spring Boot) or frontend (Angular) code. Covers how to reproduce, locate root cause, fix following stack conventions, and record a standing rule to prevent recurrence. Load this before touching any bugfix.
---

# Bug Fix Skill — ServiceForge

## How to find bugs

- Run the backend tests (`mvn test` from `backend/`) and the frontend tests/lint (`npm test` from `frontend/`) — failures are the most reliable signal.
- Compare existing code against each feature's Definition of Done in `pipeline/features/*.md` — a mismatch there is a bug even if no test currently catches it.
- Check `pipeline/decisions/*.md` for known gotchas a later change may have reintroduced.
- Search for the reported symptom (error message, endpoint, component name) across `backend/src` and `frontend/src` rather than guessing the file.

## How to fix them

1. **Reproduce first.** Run the failing test or follow the repro steps before changing anything — don't fix what you haven't confirmed is broken.
2. **Find the root cause, not the symptom.** Trace the bug to where the wrong assumption or logic actually lives; a fix that only patches the visible effect will resurface elsewhere.
3. **Fix following existing conventions.** Apply `build-code-skill`'s backend/frontend conventions. If the fix changes a model's shape, apply `migration-safety-skill` first.
4. **Stay in scope.** Fix only the identified bug — do not refactor unrelated code or expand scope while you're in the file.
5. **Add a regression test.** Write or update a test that fails before the fix and passes after, so the bug can't silently return.
6. **Re-run the full test suite** (not just the new test) to confirm no other consumer broke.

## When a bug points to a systemic gap

If the bug reveals a pattern that could recur (not a one-off typo), record it as a new standing rule under `pipeline/rules/<slug>.md`, per the "Project-wide rules" convention in `AGENTS.md`. This is how the `developer-agent` learns to avoid the same mistake on future features. Don't create a rule file for a one-off, isolated fix.

## Recording the finding

Every bugfix, systemic or not, gets logged in `pipeline/bugs_finding/bug-findings.md` (create the file and folder the first time). Append one entry per bug — never overwrite prior entries — using:

```
## <short bug title> — <date>

**Bug:** what was broken and how it was observed/reproduced.
**Root cause:** the actual underlying cause, not just the symptom.
**Fix:** what changed, and in which files.
**Regression test:** which test now guards against it.
```
