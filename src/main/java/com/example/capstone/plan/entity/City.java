package com.example.capstone.plan.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum City {
    NONE("국내"),
    GANGNAM_GU("강남구"),
    GANGDONG_GU("강동구"),
    GANGBUK_GU("강북구"),
    GANGSEO_GU("강서구"),
    GWANAK_GU("관악구"),
    GWANGJIN_GU("광진구"),
    GURO_GU("구로구"),
    GEUMCHEON_GU("금천구"),
    NOWON_GU("노원구"),
    DOBONG_GU("도봉구"),
    DONGDAEMUN_GU("동대문구"),
    DONGJAK_GU("동작구"),
    MAPO_GU("마포구"),
    SEODAEMUN_GU("서대문구"),
    SEOCHO_GU("서초구"),
    SEONGDONG_GU("성동구"),
    SEONGBUK_GU("성북구"),
    SONGPA_GU("송파구"),
    YANGCHEON_GU("양천구"),
    YEONGDEUNGPO_GU("영등포구"),
    YONGSAN_GU("용산구"),
    EUNPYEONG_GU("은평구"),
    JONGNO_GU("종로구"),
    JUNG_GU("중구"),
    JUNGNANG_GU("중랑구"),
    JEJU_SI("제주시"),
    SEOGWIPO_SI("서귀포시"),
    SUWON_SI("수원시"),
    SEONGNAM_SI("성남시"),
    GOYANG_SI("고양시"),
    YONGIN_SI("용인시"),
    BUCHEON_SI("부천시"),
    ANSAN_SI("안산시"),
    ANYANG_SI("안양시"),
    NAMYANGJU_SI("남양주시"),
    HWASEONG_SI("화성시"),
    PYEONGTAEK_SI("평택시"),
    UIJEONGBU_SI("의정부시"),
    SIHEUNG_SI("시흥시"),
    GUNPO_SI("군포시"),
    GWACHEON_SI("과천시"),
    GURI_SI("구리시"),
    ICHEON_SI("이천시"),
    ANSEONG_SI("안성시"),
    POCHEON_SI("포천시"),
    HANAM_SI("하남시"),
    OSAN_SI("오산시"),
    DONGDUCHEON_SI("동두천시"),
    GAPYEONG_GUN("가평군"),
    YANGPYEONG_GUN("양평군"),
    YEONCHEON_GUN("연천군"),
    YANGJU_SI("양주시"),
    GWANGJU_SI("광주시"),
    PAJU_SI("파주시"),
    CHUNCHEON_SI("춘천시"),
    WONJU_SI("원주시"),
    GANGNEUNG_SI("강릉시"),
    DONGHAE_SI("동해시"),
    SAMCHEOK_SI("삼척시"),
    TAEBAEK_SI("태백시"),
    SOKCHO_SI("속초시"),
    CHEONGJU_SI("청주시"),
    CHUNGJU_SI("충주시"),
    JECHEON_SI("제천시"),
    CHEONAN_SI("천안시"),
    ASAN_SI("아산시"),
    GONGJU_SI("공주시"),
    DANGJIN_SI("당진시"),
    NONSAN_SI("논산시"),
    SEOSAN_SI("서산시"),
    BUYEo_GUN("부여군"),
    HONGSEONG_GUN("홍성군"),
    JEONJU_SI("전주시"),
    GUNSAN_SI("군산시"),
    IKSAN_SI("익산시"),
    NAMWON_SI("남원시"),
    GIMJE_SI("김제시"),
    SUNCHANG_GUN("순창군"),
    MOKPO_SI("목포시"),
    YEOSU_SI("여수시"),
    SUNCHEON_SI("순천시"),
    GWANGYANG_SI("광양시"),
    HAENAM_GUN("해남군"),
    POHANG_SI("포항시"),
    GUMI_SI("구미시"),
    GYEONGSAN_SI("경산시"),
    GYEONGJU_SI("경주시"),
    ANDONG_SI("안동시"),
    YEONGJU_SI("영주시"),
    SANGJU_SI("상주시"),
    MUNGYEONG_SI("문경시"),
    ULJIN_GUN("울진군"),
    ULLEUNG_GUN("울릉군"),
    CHANGWON_SI("창원시"),
    JINJU_SI("진주시"),
    GIMHAE_SI("김해시"),
    YANGSAN_SI("양산시"),
    MIRYANG_SI("밀양시"),
    GEOJE_SI("거제시"),
    SACHEON_SI("사천시"),
    NAMHAE_GUN("남해군");

    private final String displayName;

    City(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
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

