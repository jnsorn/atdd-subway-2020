package wooteco.subway.maps.map.domain;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

public class SubwayPath {
    private List<LineStationEdge> lineStationEdges;

    public SubwayPath(List<LineStationEdge> lineStationEdges) {
        this.lineStationEdges = lineStationEdges;
    }

    public List<LineStationEdge> getLineStationEdges() {
        return lineStationEdges;
    }

    public List<Long> extractStationId() {
        List<Long> stationIds = Lists.newArrayList(lineStationEdges.get(0).getLineStation().getPreStationId());
        stationIds.addAll(lineStationEdges.stream()
                .map(it -> it.getLineStation().getStationId())
                .collect(Collectors.toList()));

        return stationIds;
    }

    public int calculateDuration() {
        return lineStationEdges.stream().mapToInt(it -> it.getLineStation().getDuration()).sum();
    }

    public int calculateDistance() {
        return lineStationEdges.stream().mapToInt(it -> it.getLineStation().getDistance()).sum();
    }

    public int calculateFare() {
        int distance = calculateDistance();
        int fare = 1250;

        if (distance > 10) {
            fare += calculateOverFare(distance - 10);
        }
        return fare;
    }

    private int calculateOverFare(int distance) {
        if(distance > 40){
            return 800 + calculateOverFareWhenOver50(distance-40);
        }
        return (int) ((Math.ceil((distance - 1) / 5) + 1) * 100);
    }

    private int calculateOverFareWhenOver50(int distance) {
        return (int) ((Math.ceil((distance - 1) / 8) + 1) * 100);
    }
}
