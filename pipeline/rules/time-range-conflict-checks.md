# Rule: time-range conflict checks must compare intervals, not start times

## Why this exists

Feature 1's `bookJob(...)` originally rejected booking conflicts only when a new job's start time exactly matched an existing job's start time. It never compared the two time ranges, so overlapping-but-differently-timed bookings were silently accepted. See `pipeline/bugs_finding/bug-findings.md` ("Overlapping bookings silently accepted for the same technician") for the full writeup.

## The rule

Any code that decides whether two time ranges conflict (job bookings, technician availability, future parts/inventory reservations tied to a job's time window, etc.) must compare genuine intervals, not a single timestamp field:

```java
boolean overlaps = startA.isBefore(endB) && startB.isBefore(endA);
```

- Never reduce an overlap check to equality on a single field (e.g. `start.equals(otherStart)`).
- If a buffer/policy constant applies (like `TRAVEL_BUFFER_MINUTES`), apply it to the relevant end of the interval before comparing — don't drop it in the name of simplifying the check.
- Add a test for a conflict where the new range's start time differs from the existing range's start time but still overlaps it, not just the exact-match case.
