package com.example.FoodDeliveryDemoApp.component.weatherItems.weatherData.repository;

import com.example.FoodDeliveryDemoApp.component.weatherItems.weatherData.domain.WeatherData;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

    WeatherData findFirstByStationNameOrderByTimestampDesc(String stationName);

    List<WeatherData> findByTimestamp(Instant timestamp);

    WeatherData findTopByOrderByIdDesc();

    Optional<WeatherData> findByStationNameAndTimestamp(String stationName, Instant timestamp);

    @NotNull
    Optional<WeatherData> findById(@NotNull Long id);

    //List<WeatherData> findByStationNameAndTimestampBetween(String city, Instant start, Instant end);

    @Query("SELECT wd FROM WeatherData wd WHERE wd.stationName = :city " +
            "AND wd.timestamp >= :dt ORDER BY wd.timestamp ASC")
    List<WeatherData> findNextWeatherData(@Param("city") String city, @Param("dt") Instant dt, Pageable pageable);

    @Query("SELECT wd FROM WeatherData wd WHERE wd.stationName = :city " +
            "AND wd.timestamp <= :dt ORDER BY wd.timestamp desc")
    List<WeatherData> findPreviousWeatherData(@Param("city") String city, @Param("dt") Instant dt, Pageable pageable);
}
