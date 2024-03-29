package com.example.FoodDeliveryDemoApp.unit.service;

import com.example.FoodDeliveryDemoApp.component.calculations.deliveryFee.dto.DeliveryFeeDTO;
import com.example.FoodDeliveryDemoApp.component.calculations.feeRule.domain.RegionalBaseFeeRule;
import com.example.FoodDeliveryDemoApp.component.calculations.feeRule.domain.extraFee.ExtraFeeAirTemperatureRule;
import com.example.FoodDeliveryDemoApp.component.calculations.feeRule.domain.extraFee.ExtraFeeWeatherPhenomenonRule;
import com.example.FoodDeliveryDemoApp.component.calculations.feeRule.domain.extraFee.ExtraFeeWindSpeedRule;
import com.example.FoodDeliveryDemoApp.component.calculations.deliveryFee.service.DeliveryFeeService;
import com.example.FoodDeliveryDemoApp.component.calculations.deliveryFee.service.DeliveryFeeServiceImpl;
import com.example.FoodDeliveryDemoApp.component.calculations.feeRule.service.extraFee.airTemperatureRule.ExtraFeeAirTemperatureRuleService;
import com.example.FoodDeliveryDemoApp.component.calculations.feeRule.service.extraFee.weatherPhenomenonRule.ExtraFeeWeatherPhenomenonRuleService;
import com.example.FoodDeliveryDemoApp.component.calculations.feeRule.service.extraFee.windSpeedRule.ExtraFeeWindSpeedRuleService;
import com.example.FoodDeliveryDemoApp.component.calculations.feeRule.service.regionalBaseFee.RegionalBaseFeeRuleService;
import com.example.FoodDeliveryDemoApp.component.weatherItems.weatherData.service.WeatherDataService;
import com.example.FoodDeliveryDemoApp.exception.CustomBadRequestException;
import com.example.FoodDeliveryDemoApp.component.weatherItems.weatherData.domain.WeatherData;
import com.example.FoodDeliveryDemoApp.component.calculations.deliveryFee.repository.DeliveryFeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@SpringBootTest
public class DeliveryFeeServiceTests {

    private DeliveryFeeService deliveryFeeService;

    @Mock
    private WeatherDataService weatherDataService;

    @Mock
    private ExtraFeeAirTemperatureRuleService airTemperatureRuleService;

    @Mock
    private ExtraFeeWindSpeedRuleService windSpeedRuleService;

    @Mock
    private ExtraFeeWeatherPhenomenonRuleService weatherPhenomenonRuleService;

    @Mock
    private RegionalBaseFeeRuleService baseFeeRuleService;

    @Mock
    private DeliveryFeeRepository deliveryFeeRepository;

    private final Map<String, Long> stationWmoCode = new HashMap<>() {{
        put("tallinn", 26038L);
        put("tartu", 26242L);
        put("pärnu", 41803L);
    }};

    public DeliveryFeeServiceTests() {
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        deliveryFeeService = new DeliveryFeeServiceImpl(
                deliveryFeeRepository,
                weatherDataService,
                airTemperatureRuleService,
                windSpeedRuleService,
                weatherPhenomenonRuleService,
                baseFeeRuleService);
    }

    // todo add tests

    // Instant instant = Instant.parse("2023-03-20T12:15:00Z"); can be used in new tests
    // LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);

    private WeatherData setupWeatherData(String city,
                                         String weatherPhenomenon,
                                         Long wmoCode,
                                         Double airTemperature,
                                         Double windSpeed) {
        WeatherData weatherData = new WeatherData();
        weatherData.setStationName(city);
        weatherData.setWeatherPhenomenon(weatherPhenomenon);
        weatherData.setWmoCode(wmoCode);
        weatherData.setAirTemperature(airTemperature);
        weatherData.setWindSpeed(windSpeed);
        return weatherData;
    }

    private RegionalBaseFeeRule setupBaseFeeRule(String city, Long wmoCode, String vehicleType, Double fee) {
        RegionalBaseFeeRule baseFeeRule = new RegionalBaseFeeRule();
        baseFeeRule.setCity(city);
        baseFeeRule.setWmoCode(wmoCode);
        baseFeeRule.setVehicleType(vehicleType);
        baseFeeRule.setFee(fee);
        return baseFeeRule;
    }

    private ExtraFeeAirTemperatureRule setupAirTemperatureRule(
            Double startAirTemperatureRange, Double endAirTemperatureRange, Double fee) {
        ExtraFeeAirTemperatureRule airTemperatureRule = new ExtraFeeAirTemperatureRule();
        airTemperatureRule.setStartAirTemperatureRange(startAirTemperatureRange);
        airTemperatureRule.setEndAirTemperatureRange(endAirTemperatureRange);
        airTemperatureRule.setFee(fee);
        return airTemperatureRule;
    }

    private ExtraFeeWindSpeedRule setupWindSpeedRule(
            Double startWindSpeedRange, Double endWindSpeedRange, Double fee) {
        ExtraFeeWindSpeedRule windSpeedRule = new ExtraFeeWindSpeedRule();
        windSpeedRule.setStartWindSpeedRange(startWindSpeedRange);
        windSpeedRule.setEndWindSpeedRange(endWindSpeedRange);
        windSpeedRule.setFee(fee);
        return windSpeedRule;
    }

    private ExtraFeeWeatherPhenomenonRule setupWeatherPhenomenonRule(String weatherPhenomenonName, Double fee) {
        ExtraFeeWeatherPhenomenonRule weatherPhenomenonRule = new ExtraFeeWeatherPhenomenonRule();
        weatherPhenomenonRule.setWeatherPhenomenonName(weatherPhenomenonName);
        weatherPhenomenonRule.setFee(fee);
        return weatherPhenomenonRule;
    }

    public final TreeMap<String, List<String>> citiesAndVehicleTypes() {
        TreeMap<String, List<String>> citiesAndVehicleTypes = new TreeMap<>();
        citiesAndVehicleTypes.put("tallinn", Arrays.asList("car", "scooter", "bike"));
        citiesAndVehicleTypes.put("tartu", Arrays.asList("car", "scooter", "bike"));
        citiesAndVehicleTypes.put("pärnu", Arrays.asList("car", "scooter", "bike"));
        return citiesAndVehicleTypes;
    }


    @Test
    /*
      calculateAndSaveDeliveryFee method
      city = tallinn
      vehicleType = car
      weather = normal
      result = success
     */
    public void testCalculateAndSaveDeliveryFee_01_success() throws CustomBadRequestException {
        String city = "tallinn";
        String vehicleType = "car";
        String weatherPhenomenonName = "Clear";
        Long wmo = stationWmoCode.get(city.toLowerCase(Locale.ROOT));
        double airTemperature = 10.0;
        double airTemperatureRangeStart = 0.0;
        double airTemperatureRangeEnd = 100.0;
        double windSpeed = 5.0;
        double windSpeedRangeStart = 0.0;
        double windSpeedRangeEnd = 10.0;

        double regionalBaseFee = 4.0;
        double airTemperatureFee = 0.0;
        double windSpeedFee = 0.0;
        double weatherPhenomenonFee = 0.0;
        double weatherConditionFee = airTemperatureFee + windSpeedFee + weatherPhenomenonFee;
        double deliveryFee = regionalBaseFee + weatherConditionFee;

        TreeMap<String, List<String>> citiesAndVehicleTypes = citiesAndVehicleTypes();

        WeatherData weatherData = setupWeatherData(city, weatherPhenomenonName, wmo, airTemperature, windSpeed);
        RegionalBaseFeeRule baseFeeRule = setupBaseFeeRule(city, wmo, vehicleType, regionalBaseFee);

        ExtraFeeAirTemperatureRule airTemperatureRule =
                setupAirTemperatureRule(airTemperatureRangeStart, airTemperatureRangeEnd, airTemperatureFee);

        ExtraFeeWindSpeedRule windSpeedRule =
                setupWindSpeedRule(windSpeedRangeStart, windSpeedRangeEnd, windSpeedFee);

        ExtraFeeWeatherPhenomenonRule weatherPhenomenonRule =
                setupWeatherPhenomenonRule(weatherPhenomenonName, weatherPhenomenonFee);

        when(weatherDataService.getLastDataByCity(city, null)).thenReturn(weatherData);

        doReturn(baseFeeRule).when(baseFeeRuleService).getByCityAndVehicleType(city, vehicleType);
        doReturn(citiesAndVehicleTypes).when(baseFeeRuleService).getAllUniqueCitiesWithVehicleTypes();
        doReturn(airTemperatureRule).when(airTemperatureRuleService).getByTemperature(weatherData.getAirTemperature());
        doReturn(windSpeedRule).when(windSpeedRuleService).getByWindSpeed(windSpeed);
        doReturn(weatherPhenomenonRule).when(weatherPhenomenonRuleService)
                .getByWeatherPhenomenonName(weatherPhenomenonName);

        DeliveryFeeDTO response = deliveryFeeService.calculateAndSaveDeliveryFee(city, vehicleType, null);

        assertNotNull(response);
        assertEquals(deliveryFee, response.getDeliveryFee());

        verify(weatherDataService, times(2)).getLastDataByCity(city, null);
        verify(baseFeeRuleService, times(1)).getByCityAndVehicleType(city, vehicleType);
        verify(baseFeeRuleService, times(1)).getAllUniqueCitiesWithVehicleTypes();
        verify(airTemperatureRuleService, times(0)).getByTemperature(airTemperature);
        verify(windSpeedRuleService, times(0)).getByWindSpeed(windSpeed);
        verify(weatherPhenomenonRuleService, times(0))
                .getByWeatherPhenomenonName(weatherPhenomenonName);

        verifyNoMoreInteractions(weatherDataService);
        verifyNoMoreInteractions(baseFeeRuleService);
        verifyNoMoreInteractions(airTemperatureRuleService);
        verifyNoMoreInteractions(windSpeedRuleService);
        verifyNoMoreInteractions(weatherPhenomenonRuleService);
    }

    @Test
    /*
      calculateAndSaveDeliveryFee method
      city = tartu
      vehicleType = scooter
      weather = normal
      result = success
     */
    public void testCalculateAndSaveDeliveryFee_02_success() throws CustomBadRequestException {
        String city = "tartu";
        String vehicleType = "scooter";
        String weatherPhenomenonName = "Clear";
        Long wmo = stationWmoCode.get(city.toLowerCase(Locale.ROOT));
        double airTemperature = 10.0;
        double airTemperatureRangeStart = 0.0;
        double airTemperatureRangeEnd = 100.0;
        double windSpeed = 5.0;
        double windSpeedRangeStart = 0.0;
        double windSpeedRangeEnd = 10.0;

        double regionalBaseFee = 3.0;
        double airTemperatureFee = 0.0;
        double windSpeedFee = 0.0;
        double weatherPhenomenonFee = 0.0;
        double weatherConditionFee = airTemperatureFee + windSpeedFee + weatherPhenomenonFee;
        double deliveryFee = regionalBaseFee + weatherConditionFee;

        TreeMap<String, List<String>> citiesAndVehicleTypes = citiesAndVehicleTypes();

        WeatherData weatherData = setupWeatherData(city, weatherPhenomenonName, wmo, airTemperature, windSpeed);
        RegionalBaseFeeRule baseFeeRule = setupBaseFeeRule(city, wmo, vehicleType, regionalBaseFee);
        ExtraFeeAirTemperatureRule airTemperatureRule =
                setupAirTemperatureRule(airTemperatureRangeStart, airTemperatureRangeEnd, airTemperatureFee);

        ExtraFeeWindSpeedRule windSpeedRule = setupWindSpeedRule(windSpeedRangeStart, windSpeedRangeEnd, windSpeedFee);

        ExtraFeeWeatherPhenomenonRule weatherPhenomenonRule =
                setupWeatherPhenomenonRule(weatherPhenomenonName, weatherPhenomenonFee);

        when(weatherDataService.getLastDataByCity(city, null))
                .thenReturn(weatherData);

        doReturn(baseFeeRule).when(baseFeeRuleService).getByCityAndVehicleType(city, vehicleType);
        doReturn(citiesAndVehicleTypes).when(baseFeeRuleService).getAllUniqueCitiesWithVehicleTypes();
        doReturn(airTemperatureRule).when(airTemperatureRuleService).getByTemperature(weatherData.getAirTemperature());
        doReturn(windSpeedRule).when(windSpeedRuleService).getByWindSpeed(windSpeed);
        doReturn(weatherPhenomenonRule).when(weatherPhenomenonRuleService)
                .getByWeatherPhenomenonName(weatherPhenomenonName);

        DeliveryFeeDTO response = deliveryFeeService.calculateAndSaveDeliveryFee(city, vehicleType, null);

        assertNotNull(response);
        assertEquals(deliveryFee, response.getDeliveryFee());

        verify(weatherDataService, times(2)).getLastDataByCity(city, null);
        verify(baseFeeRuleService, times(1)).getByCityAndVehicleType(city, vehicleType);
        verify(baseFeeRuleService, times(1)).getAllUniqueCitiesWithVehicleTypes();
        verify(airTemperatureRuleService, times(1)).getByTemperature(airTemperature);
        verify(windSpeedRuleService, times(0)).getByWindSpeed(windSpeed);
        verify(weatherPhenomenonRuleService, times(1))
                .getByWeatherPhenomenonName(weatherPhenomenonName);

        verifyNoMoreInteractions(weatherDataService);
        verifyNoMoreInteractions(baseFeeRuleService);
        verifyNoMoreInteractions(airTemperatureRuleService);
        verifyNoMoreInteractions(windSpeedRuleService);
        verifyNoMoreInteractions(weatherPhenomenonRuleService);
    }

    @Test
    /*
      calculateAndSaveDeliveryFee method
      city = pärnu
      vehicleType = bike
      weather = normal
      result = success
     */
    public void testCalculateAndSaveDeliveryFee_03_success() throws CustomBadRequestException {
        String city = "pärnu";
        String vehicleType = "bike";
        String weatherPhenomenonName = "Clear";
        Long wmo = stationWmoCode.get(city.toLowerCase(Locale.ROOT));
        double airTemperature = 10.0;
        double airTemperatureRangeStart = 0.0;
        double airTemperatureRangeEnd = 100.0;
        double windSpeed = 5.0;
        double windSpeedRangeStart = 0.0;
        double windSpeedRangeEnd = 10.0;

        double regionalBaseFee = 2.0;
        double airTemperatureFee = 0.0;
        double windSpeedFee = 0.0;
        double weatherPhenomenonFee = 0.0;
        double weatherConditionFee = airTemperatureFee + windSpeedFee + weatherPhenomenonFee;
        double deliveryFee = regionalBaseFee + weatherConditionFee;

        TreeMap<String, List<String>> citiesAndVehicleTypes = citiesAndVehicleTypes();

        WeatherData weatherData = setupWeatherData(city, weatherPhenomenonName, wmo, airTemperature, windSpeed);
        RegionalBaseFeeRule baseFeeRule = setupBaseFeeRule(city, wmo, vehicleType, regionalBaseFee);

        ExtraFeeAirTemperatureRule airTemperatureRule =
                setupAirTemperatureRule(airTemperatureRangeStart, airTemperatureRangeEnd, airTemperatureFee);
        ExtraFeeWindSpeedRule windSpeedRule = setupWindSpeedRule(windSpeedRangeStart, windSpeedRangeEnd, windSpeedFee);

        ExtraFeeWeatherPhenomenonRule weatherPhenomenonRule =
                setupWeatherPhenomenonRule(weatherPhenomenonName, weatherPhenomenonFee);

        when(weatherDataService.getLastDataByCity(city, null)).thenReturn(weatherData);

        doReturn(baseFeeRule).when(baseFeeRuleService).getByCityAndVehicleType(city, vehicleType);
        doReturn(citiesAndVehicleTypes).when(baseFeeRuleService).getAllUniqueCitiesWithVehicleTypes();
        doReturn(airTemperatureRule).when(airTemperatureRuleService).getByTemperature(weatherData.getAirTemperature());
        doReturn(windSpeedRule).when(windSpeedRuleService).getByWindSpeed(windSpeed);
        doReturn(weatherPhenomenonRule).when(weatherPhenomenonRuleService)
                .getByWeatherPhenomenonName(weatherPhenomenonName);

        DeliveryFeeDTO response = deliveryFeeService.calculateAndSaveDeliveryFee(city, vehicleType, null);

        assertNotNull(response);
        assertEquals(deliveryFee, response.getDeliveryFee());

        verify(weatherDataService, times(2)).getLastDataByCity(city, null);
        verify(baseFeeRuleService, times(1)).getByCityAndVehicleType(city, vehicleType);
        verify(baseFeeRuleService, times(1)).getAllUniqueCitiesWithVehicleTypes();
        verify(airTemperatureRuleService, times(1)).getByTemperature(airTemperature);
        verify(windSpeedRuleService, times(1)).getByWindSpeed(windSpeed);
        verify(weatherPhenomenonRuleService, times(1))
                .getByWeatherPhenomenonName(weatherPhenomenonName);

        verifyNoMoreInteractions(weatherDataService);
        verifyNoMoreInteractions(baseFeeRuleService);
        verifyNoMoreInteractions(airTemperatureRuleService);
        verifyNoMoreInteractions(windSpeedRuleService);
        verifyNoMoreInteractions(weatherPhenomenonRuleService);
    }

    @Test
    /*
      calculateAndSaveDeliveryFee method
      city = tallinn (mixed case)
      vehicleType = car (mixed case)
      weather = normal
      result = success
     */
    public void testCalculateAndSaveDeliveryFee_04_mixedCase_success() throws CustomBadRequestException {
        String city = "tallinn";
        String vehicleType = "car";
        String weatherPhenomenonName = "Clear";
        Long wmo = stationWmoCode.get(city.toLowerCase(Locale.ROOT));
        double airTemperature = 10.0;
        double airTemperatureRangeStart = 0.0;
        double airTemperatureRangeEnd = 100.0;
        double windSpeed = 5.0;
        double windSpeedRangeStart = 0.0;
        double windSpeedRangeEnd = 10.0;

        double regionalBaseFee = 4.0;
        double airTemperatureFee = 0.0;
        double windSpeedFee = 0.0;
        double weatherPhenomenonFee = 0.0;
        double weatherConditionFee = airTemperatureFee + windSpeedFee + weatherPhenomenonFee;
        double deliveryFee = regionalBaseFee + weatherConditionFee;

        TreeMap<String, List<String>> citiesAndVehicleTypes = citiesAndVehicleTypes();

        WeatherData weatherData = setupWeatherData(city, weatherPhenomenonName, wmo, airTemperature, windSpeed);
        RegionalBaseFeeRule baseFeeRule = setupBaseFeeRule(city, wmo, vehicleType, regionalBaseFee);
        ExtraFeeAirTemperatureRule airTemperatureRule =
                setupAirTemperatureRule(airTemperatureRangeStart, airTemperatureRangeEnd, airTemperatureFee);

        ExtraFeeWindSpeedRule windSpeedRule = setupWindSpeedRule(windSpeedRangeStart, windSpeedRangeEnd, windSpeedFee);

        ExtraFeeWeatherPhenomenonRule weatherPhenomenonRule =
                setupWeatherPhenomenonRule(weatherPhenomenonName, weatherPhenomenonFee);

        when(weatherDataService.getLastDataByCity(city.toLowerCase(Locale.ROOT), null))
                .thenReturn(weatherData);

        doReturn(baseFeeRule).when(baseFeeRuleService)
                .getByCityAndVehicleType(city.toLowerCase(Locale.ROOT), vehicleType.toLowerCase(Locale.ROOT));

        doReturn(citiesAndVehicleTypes).when(baseFeeRuleService)
                .getAllUniqueCitiesWithVehicleTypes();

        doReturn(airTemperatureRule).when(airTemperatureRuleService).getByTemperature(weatherData.getAirTemperature());
        doReturn(windSpeedRule).when(windSpeedRuleService).getByWindSpeed(windSpeed);

        doReturn(weatherPhenomenonRule).when(weatherPhenomenonRuleService)
                .getByWeatherPhenomenonName(weatherPhenomenonName);

        DeliveryFeeDTO response = deliveryFeeService.calculateAndSaveDeliveryFee(
                city.toLowerCase(Locale.ROOT), vehicleType.toLowerCase(Locale.ROOT), null);

        assertNotNull(response);
        assertEquals(deliveryFee, response.getDeliveryFee());

        verify(weatherDataService, times(2)).getLastDataByCity(city, null);
        verify(baseFeeRuleService, times(1)).getByCityAndVehicleType(city, vehicleType);
        verify(baseFeeRuleService, times(1)).getAllUniqueCitiesWithVehicleTypes();
        verify(airTemperatureRuleService, times(0)).getByTemperature(airTemperature);
        verify(windSpeedRuleService, times(0)).getByWindSpeed(windSpeed);
        verify(weatherPhenomenonRuleService, times(0))
                .getByWeatherPhenomenonName(weatherPhenomenonName);

        verifyNoMoreInteractions(weatherDataService);
        verifyNoMoreInteractions(baseFeeRuleService);
        verifyNoMoreInteractions(airTemperatureRuleService);
        verifyNoMoreInteractions(windSpeedRuleService);
        verifyNoMoreInteractions(weatherPhenomenonRuleService);
    }

    @Test
    /*
      calculateAndSaveDeliveryFee method
      city = tallinn
      vehicleType = bike
      weather = windSpeed > 20.0
      result = exception
     */
    public void testCalculateAndSaveDeliveryFee_05_bigWind_exception() throws CustomBadRequestException {
        String city = "tallinn";
        String vehicleType = "bike";
        String weatherPhenomenonName = "Clear";
        Long wmo = stationWmoCode.get(city.toLowerCase(Locale.ROOT));
        double airTemperature = -3.0;
        double airTemperatureRangeStart = -10.0;
        double airTemperatureRangeEnd = 0.0;
        double windSpeed = 25.0;
        double windSpeedRangeStart = 20.0;
        double windSpeedRangeEnd = 100.0;

        double regionalBaseFee = 3.0;
        double airTemperatureFee = 0.5;
        double windSpeedFee = -1;
        double weatherPhenomenonFee = 0.0;
        double weatherConditionFee = airTemperatureFee + windSpeedFee + weatherPhenomenonFee;
        //noinspection unused
        double deliveryFee = regionalBaseFee + weatherConditionFee;

        TreeMap<String, List<String>> citiesAndVehicleTypes = citiesAndVehicleTypes();

        WeatherData weatherData = setupWeatherData(city, weatherPhenomenonName, wmo, airTemperature, windSpeed);
        RegionalBaseFeeRule baseFeeRule = setupBaseFeeRule(city, wmo, vehicleType, regionalBaseFee);

        ExtraFeeAirTemperatureRule airTemperatureRule =
                setupAirTemperatureRule(airTemperatureRangeStart, airTemperatureRangeEnd, airTemperatureFee);

        ExtraFeeWindSpeedRule windSpeedRule = setupWindSpeedRule(windSpeedRangeStart, windSpeedRangeEnd, windSpeedFee);

        ExtraFeeWeatherPhenomenonRule weatherPhenomenonRule =
                setupWeatherPhenomenonRule(weatherPhenomenonName, weatherPhenomenonFee);

        when(weatherDataService.getLastDataByCity(city, null)).thenReturn(weatherData);

        doReturn(baseFeeRule).when(baseFeeRuleService).getByCityAndVehicleType(city, vehicleType);
        doReturn(citiesAndVehicleTypes).when(baseFeeRuleService).getAllUniqueCitiesWithVehicleTypes();
        doReturn(airTemperatureRule).when(airTemperatureRuleService).getByTemperature(weatherData.getAirTemperature());
        doReturn(windSpeedRule).when(windSpeedRuleService).getByWindSpeed(windSpeed);
        doReturn(weatherPhenomenonRule).when(weatherPhenomenonRuleService)
                .getByWeatherPhenomenonName(weatherPhenomenonName);

        CustomBadRequestException exception = new CustomBadRequestException(
                String.format("Usage of selected vehicle type is forbidden: wind speed ´%s´ is too high", windSpeed));

        try {
            deliveryFeeService.calculateAndSaveDeliveryFee(city, vehicleType);
            //fail("Expected DeliveryFeeExceptionsList to be thrown");
        } catch (CustomBadRequestException e) {
            assertEquals(exception.getMessage(), e.getLocalizedMessage());
        }

        verify(weatherDataService, times(1)).getLastDataByCity(city, null);
        verify(baseFeeRuleService, times(1)).getAllUniqueCitiesWithVehicleTypes();
        verify(baseFeeRuleService, times(1)).getByCityAndVehicleType(city, vehicleType);
        verify(airTemperatureRuleService, times(1)).getByTemperature(airTemperature);
        verify(windSpeedRuleService, times(1)).getByWindSpeed(windSpeed);
        verify(weatherPhenomenonRuleService, times(0))
                .getByWeatherPhenomenonName(weatherPhenomenonName);

        verifyNoMoreInteractions(weatherDataService);
        verifyNoMoreInteractions(baseFeeRuleService);
        verifyNoMoreInteractions(airTemperatureRuleService);
        verifyNoMoreInteractions(windSpeedRuleService);
        verifyNoMoreInteractions(weatherPhenomenonRuleService);
    }

    @Test
    /*
      calculateAndSaveDeliveryFee method
      city = tallinn
      vehicleType = bike
      weather = weatherPhenomenon - thunder
      result = exception
     */
    public void testCalculateAndSaveDeliveryFee_06_thunder_exception() throws CustomBadRequestException {
        String city = "tallinn";
        String vehicleType = "bike";
        String weatherPhenomenonName = "Thunder";
        Long wmo = stationWmoCode.get(city.toLowerCase(Locale.ROOT));
        double airTemperature = -3.0;
        double airTemperatureRangeStart = -10.0;
        double airTemperatureRangeEnd = 0.0;
        double windSpeed = 15.0;
        double windSpeedRangeStart = 10.0;
        double windSpeedRangeEnd = 20.0;

        double regionalBaseFee = 3.0;
        double airTemperatureFee = 0.5;
        double windSpeedFee = 0.5;
        double weatherPhenomenonFee = -1;
        double weatherConditionFee = airTemperatureFee + windSpeedFee + weatherPhenomenonFee;
        //noinspection unused
        double deliveryFee = regionalBaseFee + weatherConditionFee;

        TreeMap<String, List<String>> citiesAndVehicleTypes = citiesAndVehicleTypes();

        WeatherData weatherData = setupWeatherData(city, weatherPhenomenonName, wmo, airTemperature, windSpeed);
        RegionalBaseFeeRule baseFeeRule = setupBaseFeeRule(city, wmo, vehicleType, regionalBaseFee);

        ExtraFeeAirTemperatureRule airTemperatureRule =
                setupAirTemperatureRule(airTemperatureRangeStart, airTemperatureRangeEnd, airTemperatureFee);

        ExtraFeeWindSpeedRule windSpeedRule = setupWindSpeedRule(windSpeedRangeStart, windSpeedRangeEnd, windSpeedFee);

        ExtraFeeWeatherPhenomenonRule weatherPhenomenonRule =
                setupWeatherPhenomenonRule(weatherPhenomenonName, weatherPhenomenonFee);

        when(weatherDataService.getLastDataByCity(city, null)).thenReturn(weatherData);

        doReturn(baseFeeRule).when(baseFeeRuleService).getByCityAndVehicleType(city, vehicleType);
        doReturn(citiesAndVehicleTypes).when(baseFeeRuleService).getAllUniqueCitiesWithVehicleTypes();
        doReturn(airTemperatureRule).when(airTemperatureRuleService).getByTemperature(weatherData.getAirTemperature());
        doReturn(windSpeedRule).when(windSpeedRuleService).getByWindSpeed(windSpeed);
        doReturn(weatherPhenomenonRule).when(weatherPhenomenonRuleService)
                .getByWeatherPhenomenonName(weatherPhenomenonName);

        CustomBadRequestException exception = new CustomBadRequestException(
                String.format("Usage of selected vehicle type is forbidden: weather phenomenon ´%s´ is dangerous",
                        weatherPhenomenonName));

        try {
            deliveryFeeService.calculateAndSaveDeliveryFee(city, vehicleType);
            //fail("Expected DeliveryFeeExceptionsList to be thrown");
        } catch (CustomBadRequestException e) {
            assertEquals(exception.getMessage(), e.getLocalizedMessage());
        }

        verify(weatherDataService, times(1)).getLastDataByCity(city, null);
        verify(baseFeeRuleService, times(1)).getAllUniqueCitiesWithVehicleTypes();
        verify(baseFeeRuleService, times(1)).getByCityAndVehicleType(city, vehicleType);
        verify(airTemperatureRuleService, times(1)).getByTemperature(airTemperature);
        verify(windSpeedRuleService, times(1)).getByWindSpeed(windSpeed);
        verify(weatherPhenomenonRuleService, times(1))
                .getByWeatherPhenomenonName(weatherPhenomenonName);

        verifyNoMoreInteractions(weatherDataService);
        verifyNoMoreInteractions(baseFeeRuleService);
        verifyNoMoreInteractions(airTemperatureRuleService);
        verifyNoMoreInteractions(windSpeedRuleService);
        verifyNoMoreInteractions(weatherPhenomenonRuleService);
    }

    @Test
    /*
      calculateAndSaveDeliveryFee method
      city = tartu
      vehicleType = bike
      weather = airTemperature - -5 > x > -10,
                windSpeed - 10.0 < x < 20.0,
                weatherPhenomenon - snow related
      result = success
     */
    public void testCalculateAndSaveDeliveryFee_07_variousWeatherConditions_success() throws CustomBadRequestException {
        String city = "tartu";
        String vehicleType = "bike";
        String weatherPhenomenonName = "Moderate snowfall";
        Long wmo = stationWmoCode.get(city.toLowerCase(Locale.ROOT));
        double airTemperature = -9.0;
        double airTemperatureRangeStart = -10.0;
        double airTemperatureRangeEnd = 0.0;
        double windSpeed = 15.0;
        double windSpeedRangeStart = 10.0;
        double windSpeedRangeEnd = 20.0;

        double regionalBaseFee = 2.0;
        double airTemperatureFee = 0.0;
        double windSpeedFee = 0.5;
        double weatherPhenomenonFee = 1.0;
        double weatherConditionFee = airTemperatureFee + windSpeedFee + weatherPhenomenonFee;
        double deliveryFee = regionalBaseFee + weatherConditionFee;

        TreeMap<String, List<String>> citiesAndVehicleTypes = citiesAndVehicleTypes();

        WeatherData weatherData = setupWeatherData(city, weatherPhenomenonName, wmo, airTemperature, windSpeed);
        RegionalBaseFeeRule baseFeeRule = setupBaseFeeRule(city, wmo, vehicleType, regionalBaseFee);

        ExtraFeeAirTemperatureRule airTemperatureRule =
                setupAirTemperatureRule(airTemperatureRangeStart, airTemperatureRangeEnd, airTemperatureFee);

        ExtraFeeWindSpeedRule windSpeedRule = setupWindSpeedRule(windSpeedRangeStart, windSpeedRangeEnd, windSpeedFee);

        ExtraFeeWeatherPhenomenonRule weatherPhenomenonRule =
                setupWeatherPhenomenonRule(weatherPhenomenonName, weatherPhenomenonFee);

        when(weatherDataService.getLastDataByCity(city, null)).thenReturn(weatherData);

        doReturn(baseFeeRule).when(baseFeeRuleService).getByCityAndVehicleType(city, vehicleType);
        doReturn(citiesAndVehicleTypes).when(baseFeeRuleService).getAllUniqueCitiesWithVehicleTypes();
        doReturn(airTemperatureRule).when(airTemperatureRuleService).getByTemperature(weatherData.getAirTemperature());
        doReturn(windSpeedRule).when(windSpeedRuleService).getByWindSpeed(windSpeed);
        doReturn(weatherPhenomenonRule).when(weatherPhenomenonRuleService)
                .getByWeatherPhenomenonName(weatherPhenomenonName);

        DeliveryFeeDTO response = deliveryFeeService.calculateAndSaveDeliveryFee(city, vehicleType, null);

        assertNotNull(response);
        assertEquals(deliveryFee, response.getDeliveryFee());

        verify(weatherDataService, times(2)).getLastDataByCity(city, null);
        verify(baseFeeRuleService, times(1)).getByCityAndVehicleType(city, vehicleType);
        verify(baseFeeRuleService, times(1)).getAllUniqueCitiesWithVehicleTypes();
        verify(airTemperatureRuleService, times(1)).getByTemperature(airTemperature);
        verify(windSpeedRuleService, times(1)).getByWindSpeed(windSpeed);
        verify(weatherPhenomenonRuleService, times(1))
                .getByWeatherPhenomenonName(weatherPhenomenonName);

        verifyNoMoreInteractions(weatherDataService);
        verifyNoMoreInteractions(baseFeeRuleService);
        verifyNoMoreInteractions(airTemperatureRuleService);
        verifyNoMoreInteractions(windSpeedRuleService);
        verifyNoMoreInteractions(weatherPhenomenonRuleService);
    }

    @Test
    /*
      calculateAndSaveDeliveryFee method
      city = tallinn
      vehicleType = scooter
      weather = airTemperature - x < -10,
                weatherPhenomenon - rain related
      result = success
     */
    public void testCalculateAndSaveDeliveryFee_08_withConditions_success() throws CustomBadRequestException {
        String city = "tallinn";
        String vehicleType = "scooter";
        String weatherPhenomenonName = "Heavy rain";
        Long wmo = stationWmoCode.get(city.toLowerCase(Locale.ROOT));
        double airTemperature = -20.5;
        double airTemperatureRangeStart = -30.0;
        double airTemperatureRangeEnd = 0.0;
        double windSpeed = 15.0;
        double windSpeedRangeStart = 10.0;
        double windSpeedRangeEnd = 20.0;

        double regionalBaseFee = 3.5;
        double airTemperatureFee = 1.0;
        double windSpeedFee = 0.0;
        double weatherPhenomenonFee = 0.5;
        double weatherConditionFee = airTemperatureFee + windSpeedFee + weatherPhenomenonFee;
        double deliveryFee = regionalBaseFee + weatherConditionFee;

        TreeMap<String, List<String>> citiesAndVehicleTypes = citiesAndVehicleTypes();

        WeatherData weatherData = setupWeatherData(city, weatherPhenomenonName, wmo, airTemperature, windSpeed);
        RegionalBaseFeeRule baseFeeRule = setupBaseFeeRule(city, wmo, vehicleType, regionalBaseFee);

        ExtraFeeAirTemperatureRule airTemperatureRule =
                setupAirTemperatureRule(airTemperatureRangeStart, airTemperatureRangeEnd, airTemperatureFee);

        ExtraFeeWindSpeedRule windSpeedRule = setupWindSpeedRule(windSpeedRangeStart, windSpeedRangeEnd, windSpeedFee);

        ExtraFeeWeatherPhenomenonRule weatherPhenomenonRule =
                setupWeatherPhenomenonRule(weatherPhenomenonName, weatherPhenomenonFee);

        when(weatherDataService.getLastDataByCity(city, null)).thenReturn(weatherData);

        doReturn(baseFeeRule).when(baseFeeRuleService).getByCityAndVehicleType(city, vehicleType);
        doReturn(citiesAndVehicleTypes).when(baseFeeRuleService).getAllUniqueCitiesWithVehicleTypes();
        doReturn(airTemperatureRule).when(airTemperatureRuleService).getByTemperature(weatherData.getAirTemperature());
        doReturn(windSpeedRule).when(windSpeedRuleService).getByWindSpeed(windSpeed);
        doReturn(weatherPhenomenonRule).when(weatherPhenomenonRuleService)
                .getByWeatherPhenomenonName(weatherPhenomenonName);

        DeliveryFeeDTO response = deliveryFeeService.calculateAndSaveDeliveryFee(city, vehicleType, null);

        assertNotNull(response);
        assertEquals(deliveryFee, response.getDeliveryFee());

        verify(weatherDataService, times(2)).getLastDataByCity(city, null);
        verify(baseFeeRuleService, times(1)).getByCityAndVehicleType(city, vehicleType);
        verify(baseFeeRuleService, times(1)).getAllUniqueCitiesWithVehicleTypes();
        verify(airTemperatureRuleService, times(1)).getByTemperature(airTemperature);
        verify(windSpeedRuleService, times(0)).getByWindSpeed(windSpeed);
        verify(weatherPhenomenonRuleService, times(1))
                .getByWeatherPhenomenonName(weatherPhenomenonName);

        verifyNoMoreInteractions(weatherDataService);
        verifyNoMoreInteractions(baseFeeRuleService);
        verifyNoMoreInteractions(airTemperatureRuleService);
        verifyNoMoreInteractions(windSpeedRuleService);
        verifyNoMoreInteractions(weatherPhenomenonRuleService);
    }

    @Test
    /*
      calculateAndSaveDeliveryFee method
      city = asd
      vehicleType = asd
      weather = normal
      result = exception
     */
    public void testCalculateAndSaveDeliveryFee_09_exception() throws CustomBadRequestException {
        String city = "asd";
        String vehicleType = "asd";

        List<CustomBadRequestException> exceptionList = new ArrayList<>(List.of(
                new CustomBadRequestException(String.format("City: ´%s´ argument is invalid or not supported.", city))
        ));

        assertThrows(CustomBadRequestException.class, () ->
                deliveryFeeService.calculateAndSaveDeliveryFee(city, vehicleType),
                "Expected exception not thrown");

        try {
            deliveryFeeService.calculateAndSaveDeliveryFee(city, vehicleType);
            fail("Expected DeliveryFeeException to be thrown");
        } catch (CustomBadRequestException e) {
            assertEquals(exceptionList.get(0).getMessage(), e.getLocalizedMessage());
        }
    }

    @Test
    /*
      calculateAndSaveDeliveryFee method
      city = ""
      vehicleType = ""
      weather = normal
      result = exception
     */
    public void testCalculateAndSaveDeliveryFee_10_exception() throws CustomBadRequestException {
        String city = "";
        String vehicleType = "";

        List<CustomBadRequestException> exceptionList = new ArrayList<>(List.of(
                new CustomBadRequestException("Parameter city: ´´ is empty")
        ));

        assertThrows(CustomBadRequestException.class, () ->
                deliveryFeeService.calculateAndSaveDeliveryFee(city, vehicleType),
                "Expected exception not thrown");

        try {
            deliveryFeeService.calculateAndSaveDeliveryFee(city, vehicleType);
            fail("Expected DeliveryFeeException to be thrown");
        } catch (CustomBadRequestException e) {
            assertEquals(exceptionList.get(0).getMessage(), e.getLocalizedMessage());
        }


    }

    @Test
    /*
      calculateAndSaveDeliveryFee method
      city = ""
      vehicleType = car
      weather = normal
      result = exception
     */
    public void testCalculateAndSaveDeliveryFee_11_exception() throws CustomBadRequestException {
        String city = "";
        String vehicleType = "car";

        List<CustomBadRequestException> exceptionList = new ArrayList<>(List.of(
                new CustomBadRequestException("Parameter city: ´´ is empty")
        ));

        assertThrows(CustomBadRequestException.class, () ->
                deliveryFeeService.calculateAndSaveDeliveryFee(city, vehicleType),
                "Expected exception not thrown");

        try {
            deliveryFeeService.calculateAndSaveDeliveryFee(city, vehicleType);
            fail("Expected DeliveryFeeException to be thrown");
        } catch (CustomBadRequestException e) {
            assertEquals(exceptionList.get(0).getMessage(), e.getLocalizedMessage());
        }
    }

    @Test
    /*
      calculateAndSaveDeliveryFee method
      city = tallinn
      vehicleType = ""
      weather = normal
      result = exception
     */
    public void testCalculateAndSaveDeliveryFee_12_exception() throws CustomBadRequestException {
        String city = "tallinn";
        String vehicleType = "";

        List<CustomBadRequestException> exceptionList = new ArrayList<>(List.of(
                new CustomBadRequestException("Parameter vehicle type: ´´ is empty")
        ));

        assertThrows(CustomBadRequestException.class, () ->
                deliveryFeeService.calculateAndSaveDeliveryFee(city, vehicleType),
                "Expected exception not thrown");

        try {
            deliveryFeeService.calculateAndSaveDeliveryFee(city, vehicleType);
            fail("Expected DeliveryFeeException to be thrown");
        } catch (CustomBadRequestException e) {
            assertEquals(exceptionList.get(0).getMessage(), e.getLocalizedMessage());
        }
    }

}

