package com.example.capstone.plan.repository;


import com.example.capstone.plan.entity.Place;
import com.example.capstone.plan.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("""
        SELECT p FROM Place p
        JOIN FETCH p.day d
        JOIN FETCH d.schedule s
        WHERE s.id = :scheduleId
        ORDER BY d.dayNumber ASC, p.placeOrder ASC
    """)
    List<Place> findAllPlacesByScheduleId(@Param("scheduleId") Long scheduleId);
    List<Schedule> findByUserId(Long userId);

}
