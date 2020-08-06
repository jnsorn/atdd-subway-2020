package wooteco.subway.maps.station.application;

import org.springframework.transaction.annotation.Transactional;
import wooteco.subway.maps.station.domain.Station;
import wooteco.subway.maps.station.domain.StationRepository;
import wooteco.subway.maps.station.dto.StationCreateRequest;
import wooteco.subway.maps.station.dto.StationResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class StationService {
    private StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public Map<Long, Station> findStationsByIds(List<Long> ids) {
        return stationRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(it -> it.getId(), Function.identity()));
    }

    public StationResponse saveStation(StationCreateRequest request) {
        Station station = stationRepository.save(request.toStation());
        return StationResponse.of(station);
    }

    public List<StationResponse> findStations() {
        List<Station> stations = stationRepository.findAll();
        return StationResponse.listOf(stations);
    }

    public void deleteStationById(Long id) {
        stationRepository.deleteById(id);
    }
}
