package wooteco.subway.maps.map.domain;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import wooteco.subway.common.TestObjectUtils;
import wooteco.subway.maps.line.domain.Line;
import wooteco.subway.maps.line.domain.LineStation;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SubwayPathTest {
    @DisplayName("노선의 추가 요금이 없고 할인 혜택이 없으며 총 이동 거리가 10km 이하 때 일 때, 요금은 1250원이다")
    @Test
    void showFareByDistance() {
        int expected = 1250;
        SubwayPath subwayPath = getSubwayPathWith(1, 2);

        int actual = subwayPath.calculateFare();

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("노선의 추가 요금이 없고 할인 혜택이 없으며 총 이동 거리가 35km 일 때, 요금은 1750원이다")
    @Test
    void showFareByDistance1() {
        int expected = 1750;
        SubwayPath subwayPath = getSubwayPathWith(30, 5);

        int actual = subwayPath.calculateFare();

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("노선의 추가 요금이 없고 할인 혜택이 없으며 총 이동 거리가 60km 일 때, 요금은 2250원이다")
    @Test
    void showFareByDistance2() {
        int expected = 2250;
        SubwayPath subwayPath = getSubwayPathWith(50, 10);

        int actual = subwayPath.calculateFare();

        assertThat(actual).isEqualTo(expected);
    }

    private SubwayPath getSubwayPathWith(int distance1, int distance2) {
        Line line3 = TestObjectUtils.createLine(3L, "3호선", "ORANGE");

        LineStation lineStation6 = new LineStation(4L, 1L, distance1, 2);
        LineStation lineStation7 = new LineStation(3L, 4L, distance2, 2);
        line3.addLineStation(lineStation6);
        line3.addLineStation(lineStation7);

        List<LineStationEdge> lineStations = Lists.newArrayList(
                new LineStationEdge(lineStation6, line3.getId()),
                new LineStationEdge(lineStation7, line3.getId())
        );
        return new SubwayPath(lineStations);
    }

}