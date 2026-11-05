package com.ecomm.repository;

import com.ecomm.model.CarListing;
import com.ecomm.model.CarModel;
import com.ecomm.model.User;
import com.ecomm.util.DatabaseUtil;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs against the database from docker-compose.yml (with the seed data loaded).
 * Skipped automatically when the database is not reachable.
 */
class RepositoryIntegrationTest {

    private final UserRepository users = new UserRepository();
    private final CarListingRepository listings = new CarListingRepository();
    private final CarModelRepository models = new CarModelRepository();

    @BeforeAll
    static void requireDatabase() {
        Assumptions.assumeTrue(DatabaseUtil.testConnection(), "database not available, skipping");
    }

    @Test
    void everyListQueryRunsWithoutSqlErrors() {
        int total = listings.findAll().size();
        assertTrue(total >= 12, "seed data should provide at least 12 listings, got " + total);
        assertFalse(listings.findAllActive().isEmpty());
        assertFalse(listings.findRecent(5).isEmpty());
        assertEquals(3, listings.findAllWithPagination(1, 3).size());
        assertFalse(listings.findByPriceRange(BigDecimal.ZERO, new BigDecimal("100000")).isEmpty());
    }

    @Test
    void bodyTypeFilterOnlyReturnsThatBodyType() {
        List<CarListing> suvs = search(null, null, "SUV");
        assertFalse(suvs.isEmpty());
        assertTrue(suvs.stream().allMatch(l -> "SUV".equals(l.getBodyType())));
    }

    @Test
    void keywordMakeAndPriceFiltersCombine() {
        assertTrue(search("tdi", null, null).stream()
                .allMatch(l -> (l.getTitle() + l.getDescription()).toLowerCase().contains("tdi")));

        List<CarListing> vw = listings.searchWithFilters(null, "Volkswagen", null, null, null,
                null, new BigDecimal("15000"), null, null, null, null, null);
        assertFalse(vw.isEmpty());
        assertTrue(vw.stream().allMatch(l -> "Volkswagen".equals(l.getMakeName())
                && l.getPrice().compareTo(new BigDecimal("15000")) <= 0));
    }

    @Test
    void searchWithNoFiltersReturnsAllActiveListings() {
        List<CarListing> all = search(null, null, null);
        assertEquals(listings.countActive(), all.size());
    }

    @Test
    void createDeactivateAndDeleteListing() {
        User seller = users.findByEmail("demo@ecomm.test").orElseThrow();
        CarModel golf = models.searchByMakeAndModelName("Volkswagen", "Golf").get(0);

        CarListing listing = new CarListing(seller.getUserId(), golf.getModelId(),
                "Integration test listing", "created by a test", new BigDecimal("1234.50"), 2015);
        assertTrue(listings.create(listing));
        assertTrue(listing.getListingId() > 0);

        try {
            Optional<CarListing> loaded = listings.findById(listing.getListingId());
            assertTrue(loaded.isPresent());
            assertEquals("Hatchback", loaded.get().getBodyType());

            assertTrue(listings.deactivate(listing.getListingId()));
            assertTrue(listings.findActiveByUserId(seller.getUserId()).stream()
                    .noneMatch(l -> l.getListingId() == listing.getListingId()));
        } finally {
            assertTrue(listings.delete(listing.getListingId()));
        }
        assertFalse(listings.exists(listing.getListingId()));
    }

    @Test
    void registeredUserCanLogInIgnoringEmailCase() {
        String email = "test-" + UUID.randomUUID() + "@ecomm.test";
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(com.ecomm.util.PasswordUtil.createPasswordHash("pw123456"));
        assertTrue(users.create(user));

        try {
            assertTrue(users.existsByEmail(email.toUpperCase()));
            assertTrue(users.authenticate(email.toUpperCase(), "pw123456").isPresent());
            assertTrue(users.authenticate(email, "wrong-password").isEmpty());
        } finally {
            users.delete(user.getUserId());
        }
    }

    private List<CarListing> search(String keyword, String make, String bodyType) {
        return listings.searchWithFilters(keyword, make, null, null, null, null, null, null, null, null, null, bodyType);
    }
}
