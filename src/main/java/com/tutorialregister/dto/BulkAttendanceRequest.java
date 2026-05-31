package com.tutorialregister.dto;

import com.tutorialregister.model.AttendanceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * Request body for the bulk attendance endpoint.
 * <p>
 * All students enrolled on the course receive an attendance record for
 * {@code attendanceDate}.  The {@code defaultStatus} is applied to every
 * student unless that student is explicitly listed in {@code overrides}.
 */
public record BulkAttendanceRequest(

    /** Date for which attendance is being recorded. */
    @NotNull LocalDate attendanceDate,

    /** ID of the staff member marking attendance (optional). */
    Long markedById,

    /**
     * Default status applied to every enrolled student that does NOT appear
     * in {@code overrides}.  Defaults to {@code PRESENT} when omitted.
     */
    AttendanceStatus defaultStatus,

    /**
     * Per-student status/remarks overrides.  Any student whose ID is listed
     * here will receive the status (and optional remarks) from the override
     * instead of {@code defaultStatus}.
     */
    @Valid List<BulkAttendanceOverride> overrides
) {

    /** Per-student override entry inside a bulk attendance request. */
    public record BulkAttendanceOverride(
        @NotNull Long studentId,
        @NotNull AttendanceStatus status,
        String remarks
    ) {}
}
