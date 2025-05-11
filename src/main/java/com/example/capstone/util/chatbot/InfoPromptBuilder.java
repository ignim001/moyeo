package com.example.capstone.util.chatbot;

import org.springframework.stereotype.Component;

@Component
public class InfoPromptBuilder {

    public String build(String userQuestion) {
        return String.format("""
            당신은 대한민국 관광지에 대해 잘 아는 여행 정보 전문가입니다.

            사용자가 아래와 같은 질문을 했습니다:
            "%s"

            이 질문은 '관광지 정보' 카테고리에 해당하며, 사용자는 특정 장소의 위치, 특징, 운영시간, 입장료, 접근성, 계절별 팁 등을 알고 싶어 할 수 있습니다.

            아래 기준을 참고해 간결하고 전문적인 답변을 해주세요:
            - 장소명과 간단한 소개
            - 운영 시간 또는 입장료 등 실제 정보
            - 찾아가는 방법 (대중교통/차량 등)
            - 계절 또는 시간대에 따른 팁이 있다면 추가

            실제 여행객이 참고하기 좋은 스타일로 구성해주세요.
            """, userQuestion);
    }
}
