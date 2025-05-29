package com.example.capstone.plan.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum City {
    // 서울특별시 (areaCode: 1)
    GANGNAM_GU("강남구", 1, 4),
    GANGDONG_GU("강동구", 1, 17),
    GANGBUK_GU("강북구", 1, 11),
    GANGSEO_GU("강서구", 1, 5),
    GWANAK_GU("관악구", 1, 15),
    GWANGJIN_GU("광진구", 1, 2),
    GURO_GU("구로구", 1, 7),
    GEUMCHEON_GU("금천구", 1, 18),
    NOWON_GU("노원구", 1, 12),
    DOBONG_GU("도봉구", 1, 13),
    DONGDAEMUN_GU("동대문구", 1, 3),
    DONGJAK_GU("동작구", 1, 16),
    MAPO_GU("마포구", 1, 6),
    SEODAEMUN_GU("서대문구", 1, 14),
    SEOCHO_GU("서초구", 1, 10),
    SEONGDONG_GU("성동구", 1, 1),
    SEONGBUK_GU("성북구", 1, 9),
    SONGPA_GU("송파구", 1, 21),
    YANGCHEON_GU("양천구", 1, 23),
    YEONGDEUNGPO_GU("영등포구", 1, 19),
    YONGSAN_GU("용산구", 1, 8),
    EUNPYEONG_GU("은평구", 1, 20),
    JONGNO_GU("종로구", 1, 22),
    JUNG_GU("중구", 1, 24),
    JUNGNANG_GU("중랑구", 1, 25),

    // 제주특별자치도 (areaCode: 39)
    JEJU_SI("제주시", 39, 1),
    SEOGWIPO_SI("서귀포시", 39, 2),

    // 충청북도 (areaCode: 33)
    CHEONGJU_SI("청주시", 33, 1),
    CHUNGJU_SI("충주시", 33, 2),
    JECHEON_SI("제천시", 33, 3),

    // 경기도 (areaCode: 31)
    SUWON_SI("수원시", 31, 1),
    SEONGNAM_SI("성남시", 31, 2),
    GOYANG_SI("고양시", 31, 3),
    YONGIN_SI("용인시", 31, 4),
    BUCHEON_SI("부천시", 31, 5),
    ANSAN_SI("안산시", 31, 6),
    ANYANG_SI("안양시", 31, 7),
    NAMYANGJU_SI("남양주시", 31, 8),
    HWASEONG_SI("화성시", 31, 9),
    PYEONGTAEK_SI("평택시", 31, 10),
    UIJEONGBU_SI("의정부시", 31, 11),
    SIHEUNG_SI("시흥시", 31, 12),
    GUNPO_SI("군포시", 31, 13),
    GWACHEON_SI("과천시", 31, 14),
    GURI_SI("구리시", 31, 15),
    ICHEON_SI("이천시", 31, 16),
    ANSEONG_SI("안성시", 31, 17),
    POCHEON_SI("포천시", 31, 18),
    HANAM_SI("하남시", 31, 19),
    OSAN_SI("오산시", 31, 20),
    DONGDUCHEON_SI("동두천시", 31, 21),
    GAPYEONG_GUN("가평군", 31, 22),
    YANGPYEONG_GUN("양평군", 31, 23),
    YEONCHEON_GUN("연천군", 31, 24),
    YANGJU_SI("양주시", 31, 25),
    GWANGJU_SI("광주시", 31, 26),
    PAJU_SI("파주시", 31, 27),

    // 강원도 (areaCode: 32)
    CHUNCHEON_SI("춘천시", 32, 1),
    WONJU_SI("원주시", 32, 2),
    GANGNEUNG_SI("강릉시", 32, 3),
    DONGHAE_SI("동해시", 32, 4),
    SAMCHEOK_SI("삼척시", 32, 5),
    TAEBAEK_SI("태백시", 32, 6),
    SOKCHO_SI("속초시", 32, 7),

    // 충청남도 (areaCode: 34)
    CHEONAN_SI("천안시", 34, 1),
    ASAN_SI("아산시", 34, 2),
    GONGJU_SI("공주시", 34, 3),
    DANGJIN_SI("당진시", 34, 4),
    NONSAN_SI("논산시", 34, 5),
    SEOSAN_SI("서산시", 34, 6),
    BUYEO_GUN("부여군", 34, 7),
    HONGSEONG_GUN("홍성군", 34, 8),

    // 전라북도 (areaCode: 35)
    JEONJU_SI("전주시", 35, 1),
    GUNSAN_SI("군산시", 35, 2),
    IKSAN_SI("익산시", 35, 3),
    NAMWON_SI("남원시", 35, 4),
    GIMJE_SI("김제시", 35, 5),
    SUNCHANG_GUN("순창군", 35, 6),

    // 전라남도 (areaCode: 36)
    MOKPO_SI("목포시", 36, 1),
    YEOSU_SI("여수시", 36, 2),
    SUNCHEON_SI("순천시", 36, 3),
    GWANGYANG_SI("광양시", 36, 4),
    HAENAM_GUN("해남군", 36, 5),

    // 경상북도 (areaCode: 37)
    POHANG_SI("포항시", 37, 1),
    GUMI_SI("구미시", 37, 2),
    GYEONGSAN_SI("경산시", 37, 3),
    GYEONGJU_SI("경주시", 37, 4),
    ANDONG_SI("안동시", 37, 5),
    YEONGJU_SI("영주시", 37, 6),
    SANGJU_SI("상주시", 37, 7),
    MUNGYEONG_SI("문경시", 37, 8),
    ULJIN_GUN("울진군", 37, 9),
    ULLEUNG_GUN("울릉군", 37, 10),

    // 경상남도 (areaCode: 38)
    CHANGWON_SI("창원시", 38, 1),
    JINJU_SI("진주시", 38, 2),
    GIMHAE_SI("김해시", 38, 3),
    YANGSAN_SI("양산시", 38, 4),
    MIRYANG_SI("밀양시", 38, 5),
    GEOJE_SI("거제시", 38, 6),
    SACHEON_SI("사천시", 38, 7),
    NAMHAE_GUN("남해군", 38, 8),

    // 기본값
    NONE("국내", 0, 0);

    private final String displayName;
    private final int areaCode;
    private final int sigunguCode;

    City(String displayName, int areaCode, int sigunguCode) {
        this.displayName = displayName;
        this.areaCode = areaCode;
        this.sigunguCode = sigunguCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getAreaCode() {
        return areaCode;
    }

    public int getSigunguCode() {
        return sigunguCode;
    }

    @JsonCreator
    public static City from(String input) {
        for (City c : City.values()) {
            if (c.name().equalsIgnoreCase(input) || c.getDisplayName().equals(input)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown city: " + input);
    }
}
