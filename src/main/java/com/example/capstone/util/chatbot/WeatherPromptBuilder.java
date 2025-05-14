package com.example.capstone.util.chatbot;

import com.example.capstone.plan.entity.City;
import org.springframework.stereotype.Component;

@Component
public class WeatherPromptBuilder {

    public String build(City city) {
        return String.format("""
            너는 대한민국 기상 전문가야.

            [%s] 지역의 현재 날씨 정보를 아래 형식으로 JSON으로 알려줘.

            - region: 지역명
            - currentTemp: 현재 기온 (예: 23도)
            - minTemp: 최저 기온
            - maxTemp: 최고 기온
            - rainProbability: 강수 확률 (예: 40%%)

            조건:
            - 반드시 JSON 객체 형식으로만 응답해
            - 설명 없이 JSON만 출력해
        """, city.getDisplayName());
    }
}
