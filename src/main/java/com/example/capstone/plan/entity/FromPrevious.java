package com.example.capstone.plan.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FromPrevious {
    private Integer walk;
    private Integer publicTransport;
    private Integer car;
}
