package wooteco.subway.maps.map.acceptance;

import com.google.common.collect.Lists;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import wooteco.security.core.TokenResponse;
import wooteco.subway.common.acceptance.AcceptanceTest;
import wooteco.subway.maps.line.acceptance.step.LineAcceptanceStep;
import wooteco.subway.maps.line.dto.LineResponse;
import wooteco.subway.maps.station.acceptance.step.StationAcceptanceStep;
import wooteco.subway.maps.station.dto.StationResponse;

import static wooteco.subway.maps.line.acceptance.step.LineStationAcceptanceStep.지하철_노선에_지하철역_등록되어_있음;
import static wooteco.subway.maps.map.acceptance.step.PathAcceptanceStep.*;
import static wooteco.subway.members.member.acceptance.step.MemberAcceptanceStep.로그인_되어_있음;
import static wooteco.subway.members.member.acceptance.step.MemberAcceptanceStep.회원_등록되어_있음;

@DisplayName("지하철 경로 조회")
public class PathAcceptanceTest extends AcceptanceTest {
    public static final String EMAIL = "email@email.com";
    public static final String PASSWORD = "password";
    public static final int YOUTH_AGE = 13;
    public static final int CHILD_AGE = 12;

    private Long 교대역;
    private Long 강남역;
    private Long 양재역;
    private Long 남부터미널역;
    private Long 이호선;
    private Long 신분당선;
    private Long 삼호선;

    private TokenResponse loginResponse;


    /**
     * 교대역    --- *2호선* ---   강남역
     * |                        |
     * *3호선*                   *신분당선*
     * |                        |
     * 남부터미널역  --- *3호선* ---   양재
     */
    @BeforeEach
    public void setUp() {
        super.setUp();

        // given
        교대역 = 지하철역_등록되어_있음("교대역");
        강남역 = 지하철역_등록되어_있음("강남역");
        양재역 = 지하철역_등록되어_있음("양재역");
        남부터미널역 = 지하철역_등록되어_있음("남부터미널역");

        이호선 = 지하철_노선_등록되어_있음("2호선", "GREEN");
        신분당선 = 지하철_노선_등록되어_있음("신분당선", "RED");
        삼호선 = 지하철_노선_등록되어_있음("3호선", "ORANGE");

        지하철_노선에_지하철역_등록되어_있음(이호선, null, 교대역, 0, 0);
        지하철_노선에_지하철역_등록되어_있음(이호선, 교대역, 강남역, 2, 2);

        지하철_노선에_지하철역_등록되어_있음(신분당선, null, 강남역, 0, 0);
        지하철_노선에_지하철역_등록되어_있음(신분당선, 강남역, 양재역, 2, 1);

        지하철_노선에_지하철역_등록되어_있음(삼호선, null, 교대역, 0, 0);
        지하철_노선에_지하철역_등록되어_있음(삼호선, 교대역, 남부터미널역, 1, 2);
        지하철_노선에_지하철역_등록되어_있음(삼호선, 남부터미널역, 양재역, 2, 2);


    }

    @DisplayName("두 역의 최단 거리 경로를 조회한다.")
    @Test
    void findPathByDistance() {
        //when
        ExtractableResponse<Response> response = 거리_경로_조회_요청("DISTANCE", 1L, 3L);

        //then
        적절한_경로를_응답(response, Lists.newArrayList(교대역, 남부터미널역, 양재역));
        총_거리와_소요_시간을_함께_응답함(response, 3, 4);
    }


    @DisplayName("두 역의 최소 시간 경로를 조회한다.")
    @Test
    void findPathByDuration() {
        //when
        ExtractableResponse<Response> response = 거리_경로_조회_요청("DURATION", 1L, 3L);
        //then
        적절한_경로를_응답(response, Lists.newArrayList(교대역, 강남역, 양재역));
        총_거리와_소요_시간을_함께_응답함(response, 4, 3);
    }

    @DisplayName("경로의 요금을 조회한다.")
    @Test
    void findFare() {
        //when
        ExtractableResponse<Response> response = 거리_경로_조회_요청("DURATION", 1L, 3L);
        //then
        적절한_경로를_응답(response, Lists.newArrayList(교대역, 강남역, 양재역));
        경로의_총_요금을_응답함(response, 1250);
    }

    @DisplayName("청소년 회원에게 기본 요금에서 할인하여 720원을 부과한다")
    @Test
    void findFareForYouth() {
        회원_등록되어_있음(EMAIL, PASSWORD, YOUTH_AGE);
        loginResponse = 로그인_되어_있음(EMAIL, PASSWORD);

        //when
        ExtractableResponse<Response> response = 거리_경로_조회_요청_청소년(loginResponse,"DURATION", 1L, 3L);
        //then
        적절한_경로를_응답(response, Lists.newArrayList(교대역, 강남역, 양재역));
        경로의_총_요금을_응답함(response, 720);
    }

    @DisplayName("청소년 회원에게 기본 요금에서 할인하여 720원을 부과한다")
    @Test
    void findFareForChild() {
        회원_등록되어_있음(EMAIL, PASSWORD, CHILD_AGE);
        loginResponse = 로그인_되어_있음(EMAIL, PASSWORD);

        //when
        ExtractableResponse<Response> response = 거리_경로_조회_요청_청소년(loginResponse,"DURATION", 1L, 3L);
        //then
        적절한_경로를_응답(response, Lists.newArrayList(교대역, 강남역, 양재역));
        경로의_총_요금을_응답함(response, 450);
    }

    private Long 지하철_노선_등록되어_있음(String name, String color) {
        ExtractableResponse<Response> createLineResponse1 = LineAcceptanceStep.지하철_노선_등록되어_있음(name, color);
        return createLineResponse1.as(LineResponse.class).getId();
    }

    private Long 지하철역_등록되어_있음(String name) {
        ExtractableResponse<Response> createdStationResponse1 = StationAcceptanceStep.지하철역_등록되어_있음(name);
        return createdStationResponse1.as(StationResponse.class).getId();
    }
}
