package com.example.capstone.plan.repository;

import com.example.capstone.plan.entity.TravelPlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaceRepository extends JpaRepository<TravelPlace, Long> {
    List<TravelPlace> findAllByTravelDayIdInOrderByTravelDayIdAscPlaceOrderAsc(List<Long> dayIds);

}