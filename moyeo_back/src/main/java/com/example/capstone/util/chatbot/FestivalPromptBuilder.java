package com.example.capstone.util.chatbot;

import org.springframework.stereotype.Component;

@Component
public class FestivalPromptBuilder {

    public String build(String userQuestion) {
        return String.format("""
            당신은 대한민국 지역 축제와 문화 이벤트, 팝업스토어, 전시회 등에 대해 잘 아는 여행 문화 전문가입니다.

            사용자가 아래와 같은 질문을 했습니다:
            "%s"

            이 질문은 '축제/이벤트 정보' 카테고리에 해당합니다.
            사용자는 특정 지역이나 날짜를 기준으로 즐길 수 있는 현지 행사, 문화 프로그램, 계절별 축제를 알고 싶어 할 수 있습니다.

            아래 기준을 참고해 여행자 입장에서 실질적으로 도움이 되는 정보를 제공해주세요:
            - 축제명 또는 이벤트명과 간단한 소개
            - 개최 장소 및 일정 (예정일 포함 가능)
            - 행사 내용 및 분위기 (체험형, 공연, 플리마켓, 팝업 등)
            - 지역 음식/문화와 연결된 팁이 있다면 포함

            너무 형식적인 소개는 피하고, 실제 참여하고 싶은 매력을 중심으로 요약해주세요.
            """, userQuestion);
    }
}
