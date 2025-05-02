package com.example.capstone.plan.repository;

import com.example.capstone.plan.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findAllByDayIdInOrderByDayIdAscPlaceOrderAsc(List<Long> dayIds);

}