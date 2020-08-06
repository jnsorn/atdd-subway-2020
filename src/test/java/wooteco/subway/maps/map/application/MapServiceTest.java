package wooteco.subway.maps.map.application;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wooteco.subway.common.TestObjectUtils;
import wooteco.subway.maps.line.application.LineService;
import wooteco.subway.maps.line.domain.Line;
import wooteco.subway.maps.line.domain.LineStation;
import wooteco.subway.maps.map.domain.LineStationEdge;
import wooteco.subway.maps.map.domain.PathType;
import wooteco.subway.maps.map.domain.SubwayPath;
import wooteco.subway.maps.map.dto.MapResponse;
import wooteco.subway.maps.map.dto.PathResponse;
import wooteco.subway.maps.station.application.StationService;
import wooteco.subway.maps.station.domain.Station;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MapServiceTest {
    private MapService mapService;
    @Mock
    private LineService lineService;
    @Mock
    private StationService stationService;
    @Mock
    private PathService pathService;

    private Map<Long, Station> stations;
    private List<Line> lines;
    private Line lineWithFare;

    private SubwayPath subwayPath;
    private SubwayPath subwayPathWith2Distance;
    private SubwayPath subwayPathWith35Distance;
    private SubwayPath subwayPathWith60Distance;
    private SubwayPath subwayPathWithExtraFareLine;

    @BeforeEach
    void setUp() {
        stations = new HashMap<>();
        stations.put(1L, TestObjectUtils.createStation(1L, "교대역"));
        stations.put(2L, TestObjectUtils.createStation(2L, "강남역"));
        stations.put(3L, TestObjectUtils.createStation(3L, "양재역"));
        stations.put(4L, TestObjectUtils.createStation(4L, "남부터미널역"));

        Line line1 = TestObjectUtils.createLine(1L, "2호선", "GREEN");
        LineStation lineStation2 = new LineStation(2L, 1L, 2, 0);
        line1.addLineStation(new LineStation(1L, null, 0, 0));
        line1.addLineStation(lineStation2);

        lineWithFare = TestObjectUtils.createLineWithExtraFare(2L, "신분당선", "RED", 500);
        lineWithFare.addLineStation(new LineStation(2L, null, 0, 0));
        LineStation lineStation3 = new LineStation(3L, 2L, 0, 0);
        lineWithFare.addLineStation(lineStation3);

        Line line3 = TestObjectUtils.createLine(3L, "3호선", "ORANGE");
        line3.addLineStation(new LineStation(1L, null, 0, 0));
        LineStation lineStation6 = new LineStation(4L, 1L, 35, 2);
        LineStation lineStation7 = new LineStation(3L, 4L, 60, 0);
        line3.addLineStation(lineStation6);
        line3.addLineStation(lineStation7);

        lines = Lists.newArrayList(line1, lineWithFare, line3);

        List<LineStationEdge> lineStations = Lists.newArrayList(
                new LineStationEdge(lineStation6, line3.getId()),
                new LineStationEdge(lineStation7, line3.getId())
        );
        subwayPath = new SubwayPath(lineStations);
        subwayPathWith2Distance = new SubwayPath(Lists.newArrayList(
                new LineStationEdge(lineStation2, line1.getId())));
        subwayPathWith35Distance = new SubwayPath(Lists.newArrayList(
                new LineStationEdge(lineStation6, line3.getId())));
        subwayPathWith60Distance = new SubwayPath(Lists.newArrayList(
                new LineStationEdge(lineStation7, line3.getId())));
        subwayPathWithExtraFareLine = new SubwayPath(Lists.newArrayList(
                new LineStationEdge(lineStation3, lineWithFare.getId())));

        mapService = new MapService(lineService, stationService, pathService);
    }

    @Test
    void findPath() {
        when(lineService.findLines()).thenReturn(lines);
        when(pathService.findPath(anyList(), anyLong(), anyLong(), any())).thenReturn(subwayPath);
        when(stationService.findStationsByIds(anyList())).thenReturn(stations);

        PathResponse pathResponse = mapService.findPath(1L, 3L, PathType.DISTANCE);

        assertThat(pathResponse.getStations()).isNotEmpty();
        assertThat(pathResponse.getDuration()).isNotZero();
        assertThat(pathResponse.getDistance()).isNotZero();
        assertThat(pathResponse.getFare()).isNotZero();
    }

    @Test
    void findMap() {
        when(lineService.findLines()).thenReturn(lines);
        when(stationService.findStationsByIds(anyList())).thenReturn(stations);

        MapResponse mapResponse = mapService.findMap();

        assertThat(mapResponse.getLineResponses()).hasSize(3);

    }

    @DisplayName("노선의 추가 요금이 없고 할인 혜택이 없으며 총 이동 거리가 10km 이하 때 일 때, 요금은 1250원이다")
    @Test
    void showFareByDistance() {
        when(lineService.findLines()).thenReturn(lines);
        when(pathService.findPath(anyList(), anyLong(), anyLong(), any())).thenReturn(subwayPathWith2Distance);
        when(stationService.findStationsByIds(anyList())).thenReturn(stations);
        int expected = 1250;

        PathResponse response = mapService.findPath(1L, 2L, PathType.DISTANCE);

        assertThat(response.getFare()).isEqualTo(expected);
    }

    @DisplayName("노선의 추가 요금이 없고 할인 혜택이 없으며 총 이동 거리가 35km 일 때, 요금은 1750원이다")
    @Test
    void showFareByDistance1() {
        when(lineService.findLines()).thenReturn(lines);
        when(pathService.findPath(anyList(), anyLong(), anyLong(), any())).thenReturn(subwayPathWith35Distance);
        when(stationService.findStationsByIds(anyList())).thenReturn(stations);
        int expected = 1750;

        PathResponse response = mapService.findPath(1L, 4L, PathType.DURATION);

        assertThat(response.getFare()).isEqualTo(expected);
    }

    @DisplayName("노선의 추가 요금이 없고 할인 혜택이 없으며 총 이동 거리가 60km 일 때, 요금은 2250원이다")
    @Test
    void showFareByDistance2() {
        when(lineService.findLines()).thenReturn(lines);
        when(pathService.findPath(anyList(), anyLong(), anyLong(), any())).thenReturn(subwayPathWith60Distance);
        when(stationService.findStationsByIds(anyList())).thenReturn(stations);
        int expected = 2250;

        PathResponse response = mapService.findPath(4L, 3L, PathType.DURATION);

        assertThat(response.getFare()).isEqualTo(expected);
    }

    @DisplayName("노선의 추가 요금이 500원이 할인 혜택이 없으며 총 이동 거리가 10km 이하 때 일 때, 요금은 1750원이다")
    @Test
    void showFareByLine() {
        when(lineService.findLines()).thenReturn(lines);
        when(lineService.findLineById(any())).thenReturn(lineWithFare);
        when(pathService.findPath(anyList(), anyLong(), anyLong(), any())).thenReturn(subwayPathWithExtraFareLine);
        when(stationService.findStationsByIds(anyList())).thenReturn(stations);
        int expected = 1750;

        PathResponse response = mapService.findPath(3L, 2L, PathType.DISTANCE);

        assertThat(response.getFare()).isEqualTo(expected);
    }

    @DisplayName("기본 요금에서 청소년 할인을 받으면 요금은 720원이다")
    @Test
    void showFareByYouth() {
        when(lineService.findLines()).thenReturn(lines);
        when(pathService.findPath(anyList(), anyLong(), anyLong(), any())).thenReturn(subwayPath);
        when(stationService.findStationsByIds(anyList())).thenReturn(stations);
        int expected = 720;

        PathResponse response = mapService.findPath(1L, 2L, PathType.DISTANCE);

        assertThat(response.getFare()).isEqualTo(expected);
    }

    @DisplayName("기본 요금에서 어린이 할인을 받으면 요금은 450원이다")
    @Test
    void showFareByChild() {
        when(lineService.findLines()).thenReturn(lines);
        when(pathService.findPath(anyList(), anyLong(), anyLong(), any())).thenReturn(subwayPath);
        when(stationService.findStationsByIds(anyList())).thenReturn(stations);
        int expected = 450;

        PathResponse response = mapService.findPath(1L, 2L, PathType.DISTANCE);

        assertThat(response.getFare()).isEqualTo(expected);
    }
}
