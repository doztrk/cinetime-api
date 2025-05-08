package com.Cinetime.init;

import com.Cinetime.entity.City;
import com.Cinetime.entity.Cinema;
import com.Cinetime.entity.Hall;
import com.Cinetime.entity.Country;
import com.Cinetime.entity.District;
import com.Cinetime.repo.CinemaRepository;
import com.Cinetime.repo.CityRepository;
import com.Cinetime.repo.CountryRepository;
import com.Cinetime.repo.DistrictRepository;
import com.Cinetime.repo.HallRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final CinemaRepository cinemaRepository;
    private final HallRepository hallRepository;

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Starting data initialization...");

        // Initialize only if data doesn't exist
        if (cityRepository.count() == 0 || cinemaRepository.count() == 0 || hallRepository.count() == 0) {
            initializeData();
        } else {
            logger.info("Database already contains data. Skipping initialization.");
        }
    }

    private void initializeData() {
        // Step 1: Create countries
        logger.info("Initializing countries...");
        Country turkey = createCountryIfNotExists("Turkey");

        // Step 2: Create cities
        logger.info("Initializing cities...");
        Map<String, City> cities = new HashMap<>();
        cities.put("Istanbul", createCityIfNotExists("Istanbul", turkey));
        cities.put("Ankara", createCityIfNotExists("Ankara", turkey));
        cities.put("Izmir", createCityIfNotExists("Izmir", turkey));
        cities.put("Antalya", createCityIfNotExists("Antalya", turkey));

        // Step 3: Create districts
        logger.info("Initializing districts...");
        Map<String, District> districts = new HashMap<>();
        districts.put("Kadikoy", createDistrictIfNotExists("Kadikoy", cities.get("Istanbul")));
        districts.put("Besiktas", createDistrictIfNotExists("Besiktas", cities.get("Istanbul")));
        districts.put("Sisli", createDistrictIfNotExists("Sisli", cities.get("Istanbul")));
        districts.put("Cankaya", createDistrictIfNotExists("Cankaya", cities.get("Ankara")));
        districts.put("Konak", createDistrictIfNotExists("Konak", cities.get("Izmir")));
        districts.put("Konyaalti", createDistrictIfNotExists("Konyaalti", cities.get("Antalya")));

        // Step 4: Create cinemas
        logger.info("Initializing cinemas...");
        Cinema kadikoyImax = createCinemaIfNotExists(
                "CineTime Kadıköy IMAX",
                "cinetime-kadikoy-imax",
                districts.get("Kadikoy"),
                cities.get("Istanbul"),
                "Caferağa Mah. Moda Cad. No:123",
                "(212) 555-1234",
                "kadikoy@cinetime.com"
        );

        Cinema besiktasGold = createCinemaIfNotExists(
                "CineTime Beşiktaş Gold",
                "cinetime-besiktas-gold",
                districts.get("Besiktas"),
                cities.get("Istanbul"),
                "Sinanpaşa Mah. Ortabahçe Cad. No:456",
                "(212) 555-5678",
                "besiktas@cinetime.com"
        );

        Cinema ankaraPremium = createCinemaIfNotExists(
                "CineTime Ankara Premium",
                "cinetime-ankara-premium",
                districts.get("Cankaya"),
                cities.get("Ankara"),
                "Kızılay Mah. Atatürk Bulvarı No:789",
                "(312) 555-9012",
                "ankara@cinetime.com"
        );

        // Step 5: Create halls
        logger.info("Initializing halls...");
        createHallIfNotExists("IMAX", 200, true, kadikoyImax);
        createHallIfNotExists("Standard 1", 150, false, kadikoyImax);
        createHallIfNotExists("Standard 2", 150, false, kadikoyImax);

        createHallIfNotExists("GOLD CLASS", 80, true, besiktasGold);
        createHallIfNotExists("4DX", 120, true, besiktasGold);
        createHallIfNotExists("Standard", 160, false, besiktasGold);

        createHallIfNotExists("PREMIUM CINEMA", 100, true, ankaraPremium);
        createHallIfNotExists("SCREENX", 140, true, ankaraPremium);
        createHallIfNotExists("Standard 1", 180, false, ankaraPremium);
        createHallIfNotExists("Standard 2", 180, false, ankaraPremium);

        logger.info("Data initialization completed successfully.");
    }

    private Country createCountryIfNotExists(String name) {
        return countryRepository.findByName(name)
                .orElseGet(() -> countryRepository.save(new Country(null, name)));
    }

    private City createCityIfNotExists(String name, Country country) {
        return cityRepository.findByName(name)
                .orElseGet(() -> {
                    City city = new City();
                    city.setName(name);
                    city.setCountry(country);
                    return cityRepository.save(city);
                });
    }

    private District createDistrictIfNotExists(String name, City city) {
        return districtRepository.findByNameAndCityId(name, city.getId())
                .orElseGet(() -> {
                    District district = new District();
                    district.setName(name);
                    district.setCity(city);
                    return districtRepository.save(district);
                });
    }

    private Cinema createCinemaIfNotExists(String name, String slug, District district, City city, String address, String phone, String email) {
        return cinemaRepository.findBySlug(slug)
                .orElseGet(() -> {
                    Cinema cinema = new Cinema();
                    cinema.setName(name);
                    cinema.setSlug(slug);
                    cinema.setDistrict(district);
                    cinema.setCity(city);
                    cinema.setAddress(address);
                    cinema.setPhone(phone);
                    cinema.setEmail(email);
                    cinema.setCreatedAt(LocalDateTime.now());
                    cinema.setUpdatedAt(LocalDateTime.now());
                    return cinemaRepository.save(cinema);
                });
    }

    private Hall createHallIfNotExists(String name, Integer seatCapacity, Boolean isSpecial, Cinema cinema) {
        return hallRepository.findByNameAndCinemaId(name, cinema.getId())
                .orElseGet(() -> {
                    Hall hall = Hall.builder()
                            .name(name)
                            .seatCapacity(seatCapacity)
                            .isSpecial(isSpecial)
                            .cinema(cinema)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return hallRepository.save(hall);
                });
    }
}