package com.example.capstone.plan.repository;


import com.example.capstone.plan.entity.TravelPlace;
import com.example.capstone.plan.entity.TravelSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<TravelSchedule, Long> {

    @Query("""
        SELECT p FROM TravelPlace p
        JOIN FETCH p.travelDay d
        JOIN FETCH d.travelSchedule s
        WHERE s.id = :scheduleId
        ORDER BY d.dayNumber ASC, p.placeOrder ASC
    """)
    List<TravelPlace> findAllPlacesByScheduleId(@Param("scheduleId") Long scheduleId);
    @Query("""
    SELECT s FROM TravelSchedule s
    WHERE s.id = :scheduleId AND s.user.id = :userId
""")
    Optional<TravelSchedule> findByIdAndUserId(@Param("scheduleId") Long scheduleId, @Param("userId") Long userId);
    List<TravelSchedule> findByUserId(Long userId);

}
