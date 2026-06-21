package com.api.springcore.dto;

import java.util.List;
import java.util.Map;

public class StatsResponse {

    public record OrganizerStats(
            long totalEvents,
            long activeEvents,
            long totalRegistrations,
            long totalCheckIns,
            double checkInRate,
            Map<String, Long> attendeeStatusCounts,
            List<SessionAttendanceStat> topSessions,
            List<DailyRegistrationStat> registrationTrend
    ) {}

    public record SessionAttendanceStat(
            String sessionTitle,
            String eventTitle,
            long checkInCount,
            int capacity
    ) {}

    public record DailyRegistrationStat(
            String date,
            long count
    ) {}
}