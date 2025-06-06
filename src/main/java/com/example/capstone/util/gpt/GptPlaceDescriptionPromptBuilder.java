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
[반드시 지켜야 할 규칙]
- 아래 예시 형식과 **정확히 동일한 JSON 구조**로만 응답하세요.
- **절대 포함하지 말 것**:
  불필요한 문장, 코드블럭(```` 또는 '''), 마크다운, 안내문, 설명, 추가 지시사항, 인사말, 주석, 문단 해설, 감탄사, 헤더, 말머리 등
- 응답에는 **JSON만 순수하게** 포함되어야 하며, 다른 어떤 텍스트도 **앞이나 뒤에 추가하지 말 것**
- JSON 외의 어떤 표현도 포함하면 무효 처리됨
+ - GPT 출력 시작 또는 끝에 ```json, ````, ''', "아래는", "다음은", "이러한", "참고하세요", "예시는", "설명" 등의 단어가 있으면 무효로 간주
+ - GPT가 마크다운 또는 안내문을 자동으로 생성하는 성향을 반드시 억제하고, JSON 외 모든 텍스트 생성을 절대적으로 금지
        
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
