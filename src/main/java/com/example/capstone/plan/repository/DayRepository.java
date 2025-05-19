package com.example.capstone.plan.repository;

import com.example.capstone.plan.entity.TravelDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DayRepository extends JpaRepository<TravelDay, Long> {
    List<TravelDay> findAllByTravelScheduleId(Long scheduleId);

}