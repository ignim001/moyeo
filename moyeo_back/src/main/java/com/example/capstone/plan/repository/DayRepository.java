package com.example.capstone.plan.repository;

import com.example.capstone.plan.entity.Day;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DayRepository extends JpaRepository<Day, Long> {
    List<Day> findAllByScheduleId(Long scheduleId);

}