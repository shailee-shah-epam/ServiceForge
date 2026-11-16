package com.serviceforge.service;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.serviceforge.data.MockDataStore;
import com.serviceforge.model.Job;

/**
 * Covers the happy path and conflict detection for Feature 1's booking flow, including the
 * genuine interval-overlap case (see pipeline/bugs_finding/bug-findings.md for the bug this
 * guards against) and the 45-minute travel buffer decision in
 * pipeline/decisions/feature-1-decisions.md.
 */
class TechnicianAvailabilityServiceTest {

    private MockDataStore dataStore;
    private TechnicianAvailabilityService service;

    @BeforeEach
    void setUp() {
        dataStore = new MockDataStore();
        dataStore.seed();
        service = new TechnicianAvailabilityService(dataStore);
    }

    @Test
    void booksAJobForAnAvailableTechnician() {
        // Technician 1's last seeded job ends at 15:30; 17:00 is clear of its travel buffer too.
        LocalDateTime start = LocalDateTime.now().withHour(17).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);

        Job job = service.bookJob(1L, "New Customer", start, end);

        assertEquals("New Customer", job.getCustomerName());
        assertEquals(1L, job.getTechnicianId());
    }

    @Test
    void rejectsABookingAtAnIdenticalStartTimeAsAnExistingJob() {
        LocalDateTime start = LocalDateTime.now().withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);
        // Technician 1 already has a seeded job starting at 09:00 (see MockDataStore.seed()).

        assertThrows(IllegalStateException.class, () -> service.bookJob(1L, "Conflicting Customer", start, end));
    }

    @Test
    void rejectsABookingThatOverlapsAnExistingJobWithADifferentStartTime() {
        // Technician 1 has a seeded job 09:00-11:00; a 10:00-12:00 booking genuinely overlaps it
        // even though its start time doesn't match exactly.
        LocalDateTime start = LocalDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(2);

        assertThrows(IllegalStateException.class, () -> service.bookJob(1L, "Overlapping Customer", start, end));
    }

    @Test
    void rejectsABookingThatStartsWithinAnotherJobsTravelBuffer() {
        // Technician 1's seeded job ends at 11:00; the 45-minute travel buffer extends the
        // effective reservation to 11:45, so an 11:30 start must still be rejected.
        LocalDateTime start = LocalDateTime.now().withHour(11).withMinute(30).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);

        assertThrows(IllegalStateException.class, () -> service.bookJob(1L, "Too Soon Customer", start, end));
    }

    @Test
    void allowsABookingThatStartsAfterAnotherJobsTravelBuffer() {
        // Technician 1's seeded job ends at 11:00; 11:45 is exactly at the end of the travel
        // buffer, so a booking starting then should be accepted.
        LocalDateTime start = LocalDateTime.now().withHour(11).withMinute(45).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);

        Job job = service.bookJob(1L, "Buffer Respecting Customer", start, end);

        assertEquals("Buffer Respecting Customer", job.getCustomerName());
    }

    @Test
    void throwsForAnUnknownTechnician() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(1);

        assertThrows(IllegalArgumentException.class, () -> service.bookJob(999L, "Nobody", start, end));
    }
}
