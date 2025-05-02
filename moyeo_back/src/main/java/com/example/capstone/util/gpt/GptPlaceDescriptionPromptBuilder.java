package com.example.capstone.util.gpt;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GptPlaceDescriptionPromptBuilder {

    public String build(List<String> placeNames) {
        StringBuilder sb = new StringBuilder();

        sb.append("다음 장소들에 대해 감성적이고 간결한 한 줄 설명을 만들어줘.\n\n");

        sb.append("장소 목록:\n");
        for (int i = 0; i < placeNames.size(); i++) {
            sb.append(String.format("%d. %s\n", i + 1, placeNames.get(i)));
        }

        sb.append("""
        
 주의사항:
- 답변은 반드시 장소명과 한줄 설명만 작성해.
- 출력 형식은 오직 '- 장소명: 한줄 설명' 형태로만 작성해.
- 다른 문장은 넣지 말고, 꼭 이 형식만 지켜줘.
        
응답 형식:
- 각 장소마다 아래 형태로 응답해줘.
- [장소명]: [한줄 설명]

예시:
- 경복궁: 조선의 숨결을 느낄 수 있는 고궁
- 청남대: 대통령의 발자취를 따라 걷는 여유로운 산책길
""");

        return sb.toString();
    }
}
