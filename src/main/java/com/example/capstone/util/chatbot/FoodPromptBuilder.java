package com.example.capstone.util.chatbot;

import org.springframework.stereotype.Component;

@Component
public class FoodPromptBuilder {

    public String build(String userQuestion) {
        return String.format("""
            당신은 대한민국 맛집, 카페 정보에 정통한 미식 전문가입니다.

            사용자가 아래와 같은 질문을 했습니다:
            "%s"

            이 질문은 '맛집/카페 정보' 카테고리에 해당합니다.
            사용자는 지역, 음식 종류, 분위기, 혼밥 여부 등 다양한 기준으로 식당이나 카페를 추천받고자 할 수 있습니다.

            아래 기준을 참고하여 실제 경험 기반의 신뢰도 있는 추천을 제공해주세요:
            - 장소명과 간단한 소개 (지역 포함)
            - 대표 메뉴나 인기 이유
            - 분위기나 테마 (조용한, 뷰 좋은 등)
            - 혼밥 여부나 예약 팁 등 참고 정보

            너무 광고성 문장은 피하고, 실제로 방문 가치가 있는 곳 위주로 구성해주세요.
            """, userQuestion);
    }
}
