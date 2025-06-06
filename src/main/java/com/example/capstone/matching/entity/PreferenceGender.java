package com.example.capstone.matching.entity;

import com.example.capstone.user.entity.Gender;

public enum PreferenceGender {
    MALE, FEMALE, NONE;

    public Gender toGender() {
        if (this == NONE) return null;
        return Gender.valueOf(this.name());
    }
}
