# Bug Findings Log

## Overlapping bookings silently accepted for the same technician — 2026-11-16

**Bug:** `POST /api/jobs` was supposed to reject a booking that overlaps an existing job for the same technician (per Feature 1's Definition of Done), but two jobs with different-but-overlapping start times (e.g. an existing 09:00-11:00 job and a new 10:00-12:00 job) were silently accepted instead of rejected with a conflict error. Reproduced via `TechnicianAvailabilityServiceTest` using the seeded technician/job data in `MockDataStore`.

**Root cause:** `TechnicianAvailabilityService.bookJob(...)` detected conflicts with `existing.getStartTime().equals(startTime)` — an exact start-time match only. It never compared the two jobs' time ranges, so any overlap that didn't start at the exact same minute as an existing job was invisible to the check.

**Fix:** Replaced the exact-match check with a genuine interval-overlap check in `backend/src/main/java/com/serviceforge/service/TechnicianAvailabilityService.java`: a new booking conflicts with an existing job if `newStart < existingEnd+buffer && existingStart < newEnd+buffer`, where `buffer` is the existing `TRAVEL_BUFFER_MINUTES` (45 minutes) decision — so the fix preserves the travel-buffer policy instead of dropping it. Updated `pipeline/decisions/feature-1-decisions.md` and `pipeline/features/feature-1-technician-availability.md` to mark the issue fixed.

**Regression test:** `TechnicianAvailabilityServiceTest` in `backend/src/test/java/com/serviceforge/service/TechnicianAvailabilityServiceTest.java` — added `rejectsABookingThatOverlapsAnExistingJobWithADifferentStartTime`, `rejectsABookingThatStartsWithinAnotherJobsTravelBuffer`, and `allowsABookingThatStartsAfterAnotherJobsTravelBuffer`; adjusted `booksAJobForAnAvailableTechnician`'s slot time so it no longer collides with a seeded job's travel buffer.
