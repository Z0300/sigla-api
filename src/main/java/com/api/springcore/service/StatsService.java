package com.api.springcore.service;

import com.api.springcore.dto.StatsResponse;
import com.api.springcore.repository.StatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final StatsRepository        statsRepository;

    @Transactional(readOnly = true)
    public StatsResponse.OrganizerStats getOrganizerStats(Long organizerId) {
        long totalEvents       = statsRepository.countTotalEvents(organizerId);
        long activeEvents      = statsRepository.countActiveEvents(organizerId);
        long totalRegistrations = statsRepository.countTotalRegistrations(organizerId);
        long totalCheckIns     = statsRepository.countTotalCheckIns(organizerId);

        double checkInRate = totalRegistrations > 0
                ? Math.round((totalCheckIns * 100.0 / totalRegistrations) * 10.0) / 10.0
                : 0.0;

        Map<String, Long> statusCounts = buildStatusCounts(
                statsRepository.countAttendeesByStatus(organizerId));

        List<StatsResponse.SessionAttendanceStat> topSessions = buildTopSessions(
                statsRepository.findTopSessionsByCheckIns(organizerId));

        List<StatsResponse.DailyRegistrationStat> trend = buildTrend(
                statsRepository.countRegistrationsByDay(organizerId));

        log.info("Stats fetched for organizer={}", organizerId);

        return new StatsResponse.OrganizerStats(
                totalEvents,
                activeEvents,
                totalRegistrations,
                totalCheckIns,
                checkInRate,
                statusCounts,
                topSessions,
                trend
        );
    }

    private Map<String, Long> buildStatusCounts(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }

    private List<StatsResponse.SessionAttendanceStat> buildTopSessions(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new StatsResponse.SessionAttendanceStat(
                        (String) r[0],
                        (String) r[1],
                        (Long)   r[2],
                        (Integer) r[3]
                ))
                .toList();
    }

    private List<StatsResponse.DailyRegistrationStat> buildTrend(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new StatsResponse.DailyRegistrationStat(
                        r[0].toString(),
                        (Long) r[1]
                ))
                .toList();
    }
}