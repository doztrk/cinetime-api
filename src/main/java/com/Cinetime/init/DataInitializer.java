package com.Cinetime.init;

import com.Cinetime.entity.*;
import com.Cinetime.enums.Gender;
import com.Cinetime.enums.RoleName;
import com.Cinetime.repo.*;
import com.Cinetime.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final CinemaRepository cinemaRepository;
    private final HallRepository hallRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final ShowtimeRepository showtimeRepository;
    @Value("${Admin.Email}")
    private String adminEmail;
    @Value("${Admin.Password}")
    private String adminPassword;
    @Value("${User.Email}")
    private String userEmail;
    @Value("${User.Password}")
    private String userPassword;

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
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            createAdminUser();
        } else {
            logger.info("Admin user already exists. Skipping creation.");
        }
        if (userRepository.findByEmail(userEmail).isEmpty()) {
            createMemberUser();
        } else {
            logger.info("Member user already exists. Skipping creation.");
        }


        logger.info("User initialization completed.");

        // Initialize movies if they don't exist
     /*   if (movieRepository.count() == 0) {
            initializeMovies();
        } else {
            logger.info("Movies already exist in database. Skipping movie initialization.");
        }*/
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

/*    private void initializeMovies() {
        logger.info("Initializing movies...");

        // Get all halls for movie-hall relationships
        List<Hall> allHalls = hallRepository.findAll();
        if (allHalls.isEmpty()) {
            logger.error("No halls found. Cannot create movies without halls.");
            return;
        }

        // Create multiple movies
        createMovieIfNotExists(
                "Dune: Part Two",
                "dune-part-two",
                "Paul Atreides unites with Chani and the Fremen while on a warpath of revenge against the conspirators who destroyed his family.",
                LocalDate.of(2024, 3, 1),
                165,
                8.5,
                "Denis Villeneuve",
                Arrays.asList("Timothée Chalamet", "Zendaya", "Rebecca Ferguson", "Oscar Isaac"),
                Arrays.asList("IMAX", "4DX", "Standard"),
                Arrays.asList("Sci-Fi", "Adventure", "Drama"),
                MovieStatus.IN_THEATERS,
                "/uploads/posters/dune-part-two.jpg",
                allHalls.subList(0, Math.min(3, allHalls.size()))
        );

        createMovieIfNotExists(
                "Oppenheimer",
                "oppenheimer",
                "The story of J. Robert Oppenheimer and his role in the development of the atomic bomb.",
                LocalDate.of(2023, 7, 21),
                180,
                8.3,
                "Christopher Nolan",
                Arrays.asList("Cillian Murphy", "Emily Blunt", "Matt Damon", "Robert Downey Jr."),
                Arrays.asList("IMAX", "70mm", "Standard"),
                Arrays.asList("Biography", "Drama", "History"),
                MovieStatus.IN_THEATERS,
                "/uploads/posters/oppenheimer.jpg",
                allHalls.subList(0, Math.min(4, allHalls.size()))
        );

        createMovieIfNotExists(
                "Spider-Man: Across the Spider-Verse",
                "spider-man-across-spider-verse",
                "Miles Morales catapults across the Multiverse, where he encounters a team of Spider-People.",
                LocalDate.of(2023, 6, 2),
                140,
                8.7,
                "Joaquim Dos Santos",
                Arrays.asList("Shameik Moore", "Hailee Steinfeld", "Brian Tyree Henry", "Luna Lauren Vélez"),
                Arrays.asList("IMAX", "4DX", "Standard"),
                Arrays.asList("Animation", "Action", "Adventure"),
                MovieStatus.IN_THEATERS,
                "/uploads/posters/spider-verse.jpg",
                allHalls.subList(1, Math.min(5, allHalls.size()))
        );

        createMovieIfNotExists(
                "The Batman 2",
                "the-batman-2",
                "The Dark Knight returns to face a new threat in Gotham City.",
                LocalDate.of(2025, 10, 3),
                150,
                null, // Rating not available yet
                "Matt Reeves",
                Arrays.asList("Robert Pattinson", "Zoë Kravitz", "Paul Dano", "Jeffrey Wright"),
                Arrays.asList("IMAX", "Standard"),
                Arrays.asList("Action", "Crime", "Drama"),
                MovieStatus.COMING_SOON,
                "/uploads/posters/the-batman-2.jpg",
                allHalls.subList(0, Math.min(2, allHalls.size()))
        );

        createMovieIfNotExists(
                "Avatar 3",
                "avatar-3",
                "The saga continues as Jake Sully and Neytiri face new challenges in Pandora.",
                LocalDate.of(2025, 12, 20),
                170,
                null,
                "James Cameron",
                Arrays.asList("Sam Worthington", "Zoe Saldana", "Sigourney Weaver", "Stephen Lang"),
                Arrays.asList("IMAX", "3D", "4DX", "Standard"),
                Arrays.asList("Sci-Fi", "Action", "Adventure"),
                MovieStatus.COMING_SOON,
                "/uploads/posters/avatar-3.jpg",
                allHalls.subList(0, Math.min(6, allHalls.size()))
        );

        createMovieIfNotExists(
                "John Wick: Chapter 5",
                "john-wick-5",
                "John Wick faces his most dangerous mission yet.",
                LocalDate.of(2025, 5, 21),
                130,
                null,
                "Chad Stahelski",
                Arrays.asList("Keanu Reeves", "Ian McShane", "Laurence Fishburne", "Lance Reddick"),
                Arrays.asList("IMAX", "Standard"),
                Arrays.asList("Action", "Crime", "Thriller"),
                MovieStatus.COMING_SOON,
                "/uploads/posters/john-wick-5.jpg",
                allHalls.subList(2, Math.min(7, allHalls.size()))
        );

        createMovieIfNotExists(
                "Killers of the Flower Moon",
                "killers-flower-moon",
                "Based on the true story of the Osage murders and the birth of the FBI.",
                LocalDate.of(2023, 10, 20),
                206,
                7.8,
                "Martin Scorsese",
                Arrays.asList("Leonardo DiCaprio", "Robert De Niro", "Lily Gladstone", "Jesse Plemons"),
                Arrays.asList("Standard", "IMAX"),
                Arrays.asList("Crime", "Drama", "History"),
                MovieStatus.ENDED,
                "/uploads/posters/killers-flower-moon.jpg",
                allHalls.subList(0, Math.min(3, allHalls.size()))
        );

        logger.info("Movie initialization completed successfully.");
    }*/

    // Existing methods remain the same...
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

    private void createAdminUser() {
        logger.info("Creating admin user...");

        Role adminRole = roleService.getRole(RoleName.ADMIN);

        User admin = User.builder()
                .firstname("Admin")
                .lastname("User")
                .email(adminEmail) //Burayi kendi mailinizle degistirebilirsiniz.
                .phoneNumber(adminPassword)
                .password(passwordEncoder.encode("Admin123!"))
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .role(adminRole)
                .builtIn(true) // Built-in users cannot be deleted/updated
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(admin);
        logger.info("Admin user created successfully with email: {}", admin.getEmail());
    }

    private void createMemberUser() {
        logger.info("Creating member user...");

        Role memberRole = roleService.getRole(RoleName.MEMBER);

        User member = User.builder()
                .firstname("John")
                .lastname("Doe")
                .email(userEmail) // Burayi kendi mailinizle degistirebilirsiniz
                .phoneNumber(userPassword)
                .password(passwordEncoder.encode("Member123!"))
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .role(memberRole)
                .builtIn(false) // Regular user that can be updated/deleted
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(member);
        logger.info("Member user created successfully with email: {}", member.getEmail());
    }

    private void createHallIfNotExists(String name, Integer seatCapacity, Boolean isSpecial, Cinema cinema) {
        hallRepository.findByNameAndCinemaId(name, cinema.getId())
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