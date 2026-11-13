package a.slelin.work.word.master;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full application on a real PostgreSQL: migrations, seed data, security and the main user flows.
 * <p>
 * The database is started in Docker by Testcontainers. Without Docker an existing PostgreSQL can be used:
 * set {@code TEST_DB_URL} (and optionally {@code TEST_DB_USER}, {@code TEST_DB_PASSWORD}) - <b>its schema
 * is dropped and created again</b>. With neither Docker nor {@code TEST_DB_URL} the test is skipped.
 */
@ActiveProfiles("test")
@AutoConfigureMockMvc
@EnabledIf("databaseAvailable")
@SpringBootTest(properties = {
        "wordmaster.admin.username=admin",
        "wordmaster.admin.email=admin@test.io",
        "wordmaster.admin.password=admin12345",
        "liquibase.analytics.enabled=false"
})
class WordMasterIntegrationTest {

    private static final String STARTER_DECK = "00000000-0000-0000-0000-000000000101";

    private static final String EXTERNAL_DB_URL = System.getenv("TEST_DB_URL");

    private static PostgreSQLContainer postgres;

    static boolean databaseAvailable() {
        return EXTERNAL_DB_URL != null || DockerClientFactory.instance().isDockerAvailable();
    }

    @DynamicPropertySource
    static void dataSource(DynamicPropertyRegistry registry) {
        if (EXTERNAL_DB_URL != null) {
            registry.add("spring.datasource.url", () -> EXTERNAL_DB_URL);
            registry.add("spring.datasource.username", () -> env("TEST_DB_USER", "word_master_user"));
            registry.add("spring.datasource.password", () -> env("TEST_DB_PASSWORD", "password"));
            registry.add("spring.liquibase.drop-first", () -> "true");
            return;
        }

        if (postgres == null) {
            postgres = new PostgreSQLContainer("postgres:17-alpine");
            postgres.start();
        }
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JsonMapper json;

    private String token;

    private String username;

    private String refreshToken;

    @BeforeEach
    void register() throws Exception {
        String name = "u" + UUID.randomUUID().toString().substring(0, 8);
        username = name;
        JsonNode auth = perform(post("/api/auth/register"),
                Map.of("username", name, "email", name + "@test.io", "password", "secret123"), null)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString().transform(json::readTree);
        token = auth.get("accessToken").asString();
        refreshToken = auth.get("refreshToken").asString();
        assertNotNull(refreshToken);
    }

    @Test
    void catalogIsPublicAndContainsStarterDecks() throws Exception {
        JsonNode catalog = read(perform(get("/api/decks/public"), null, null).andExpect(status().isOk()));
        assertEquals(8, catalog.get("page").get("totalElements").asInt());
        assertTrue(catalog.get("content").get(0).get("isOfficial").asBoolean());

        JsonNode preview = read(perform(get("/api/decks/public/" + STARTER_DECK + "/cards?limit=5"), null, null)
                .andExpect(status().isOk()));
        assertEquals(5, preview.size());
    }

    @Test
    void protectedEndpointsRequireToken() throws Exception {
        perform(get("/api/decks"), null, null).andExpect(status().isUnauthorized());
        perform(get("/api/admin/users"), null, token).andExpect(status().isForbidden());
        perform(get("/api/decks"), null, token).andExpect(status().isOk());
    }

    @Test
    void trainingFlowUpdatesProgressAndStats() throws Exception {
        JsonNode languages = read(perform(get("/api/languages"), null, null).andExpect(status().isOk()));
        long ru = idOf(languages, "ru");
        long en = idOf(languages, "en");

        JsonNode deck = read(perform(post("/api/decks"), Map.of("title", "Fruits",
                "sourceLanguageId", ru, "targetLanguageId", en, "isPublic", false), token)
                .andExpect(status().isCreated()));
        String deckId = deck.get("id").asString();

        perform(post("/api/decks/" + deckId + "/cards/bulk"), Map.of("cards", new Object[]{
                Map.of("word", "apple", "translation", "яблоко"),
                Map.of("word", "pear", "translation", "груша")}), token)
                .andExpect(status().isCreated());

        JsonNode session = read(perform(post("/api/training/sessions"),
                Map.of("deckId", deckId, "mode", "typing"), token).andExpect(status().isCreated()));
        String sessionId = session.get("sessionId").asString();
        JsonNode card = session.get("firstCard");
        assertEquals(2, session.get("totalCards").asInt());
        assertTrue(card.get("answer").isNull() || card.get("answer").isMissingNode());

        Map<String, String> answers = Map.of("apple", "яблоко", "pear", "груша");
        while (card != null && !card.isNull()) {
            JsonNode result = read(perform(post("/api/training/sessions/" + sessionId + "/answers"),
                    Map.of("cardId", card.get("cardId").asString(),
                            "answer", answers.get(card.get("prompt").asString())), token)
                    .andExpect(status().isOk()));
            assertTrue(result.get("correct").asBoolean());
            assertEquals("learning", result.get("updatedProgress").get("status").asString());
            card = result.get("nextCard");
        }

        JsonNode finish = read(perform(post("/api/training/sessions/" + sessionId + "/finish"), null, token)
                .andExpect(status().isOk()));
        assertEquals(100.0, finish.get("accuracyPercent").asDouble());
        assertEquals(1, finish.get("currentStreak").asInt());
        assertTrue(finish.get("xpEarned").asLong() > 0);

        JsonNode overview = read(perform(get("/api/stats/overview"), null, token).andExpect(status().isOk()));
        assertEquals(2, overview.get("learningWords").asInt());
        assertEquals(2, overview.get("todayReviewed").asInt());

        perform(delete("/api/decks/" + deckId), null, token).andExpect(status().isNoContent());
        perform(get("/api/decks/" + deckId), null, token).andExpect(status().isNotFound());
    }

    @Test
    void copyLikeAndOwnership() throws Exception {
        JsonNode copy = read(perform(post("/api/decks/" + STARTER_DECK + "/copy"), null, token)
                .andExpect(status().isCreated()));
        assertEquals(100, copy.get("cardsCopiedCount").asInt());

        JsonNode like = read(perform(post("/api/decks/" + STARTER_DECK + "/like"), null, token)
                .andExpect(status().isOk()));
        assertTrue(like.get("liked").asBoolean());

        perform(delete("/api/decks/" + STARTER_DECK), null, token).andExpect(status().isForbidden());
        perform(patch("/api/decks/" + copy.get("newDeckId").asString()), Map.of("title", "My verbs"), token)
                .andExpect(status().isOk());
    }

    @Test
    void unknownSortPropertyIsBadRequest() throws Exception {
        perform(get("/api/decks?sort=nope,desc"), null, token).andExpect(status().isBadRequest());
        perform(get("/api/decks?sort=title,desc"), null, token).andExpect(status().isOk());
    }

    @Test
    void searchTreatsWildcardsLiterally() throws Exception {
        JsonNode all = read(perform(get("/api/decks/public?search=глаголы"), null, null).andExpect(status().isOk()));
        assertTrue(all.get("page").get("totalElements").asInt() >= 2);

        JsonNode wildcard = read(perform(get("/api/decks/public?search=%25"), null, null).andExpect(status().isOk()));
        assertEquals(0, wildcard.get("page").get("totalElements").asInt());
    }

    @Test
    void tokenOfDeletedAccountIsRejected() throws Exception {
        perform(delete("/api/users/me"), null, token).andExpect(status().isNoContent());

        perform(get("/api/users/me"), null, token).andExpect(status().isUnauthorized());
        perform(post("/api/auth/refresh"), Map.of("refreshToken", refreshToken), null)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshTokenIsRotatedAndPasswordChangeRevokesSessions() throws Exception {
        JsonNode refreshed = read(perform(post("/api/auth/refresh"), Map.of("refreshToken", refreshToken), null)
                .andExpect(status().isOk()));
        String second = refreshed.get("refreshToken").asString();
        assertNotEquals(refreshToken, second);
        perform(post("/api/auth/refresh"), Map.of("refreshToken", refreshToken), null)
                .andExpect(status().isUnauthorized());

        perform(put("/api/users/me/password"), Map.of("oldPassword", "wrong-one", "newPassword", "secret456"), token)
                .andExpect(status().isUnprocessableContent());
        perform(put("/api/users/me/password"), Map.of("oldPassword", "secret123", "newPassword", "secret456"), token)
                .andExpect(status().isNoContent());

        perform(post("/api/auth/refresh"), Map.of("refreshToken", second), null)
                .andExpect(status().isUnauthorized());
        perform(post("/api/auth/login"), Map.of("usernameOrEmail", username, "password", "secret123"), null)
                .andExpect(status().isUnauthorized());
        perform(post("/api/auth/login"),
                Map.of("usernameOrEmail", username.toUpperCase() + "@TEST.IO", "password", "secret456"), null)
                .andExpect(status().isOk());
    }

    @Test
    void tooManyFailedLoginsAreBlocked() throws Exception {
        Map<String, String> wrong = Map.of("usernameOrEmail", username, "password", "not-my-password");
        for (int i = 0; i < 10; i++) {
            perform(post("/api/auth/login"), wrong, null).andExpect(status().isUnauthorized());
        }

        perform(post("/api/auth/login"), wrong, null).andExpect(status().isTooManyRequests());
        // Even the right password does not help until the block expires.
        perform(post("/api/auth/login"), Map.of("usernameOrEmail", username, "password", "secret123"), null)
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void duplicateAccountIsConflict() throws Exception {
        perform(post("/api/auth/register"), Map.of("username", username.toUpperCase(),
                "email", "other-" + username + "@test.io", "password", "secret123"), null)
                .andExpect(status().isConflict());
        perform(post("/api/auth/register"), Map.of("username", username + "2",
                "email", username.toUpperCase() + "@TEST.IO", "password", "secret123"), null)
                .andExpect(status().isConflict());
    }

    @Test
    void settingsKeepTimeZoneAndDailyGoal() throws Exception {
        JsonNode updated = read(perform(put("/api/users/me/settings"),
                Map.of("timeZone", "Asia/Tokyo"), token).andExpect(status().isOk()));
        assertEquals("Asia/Tokyo", updated.get("timeZone").asString());
        assertEquals(20, updated.get("dailyGoal").asInt());

        updated = read(perform(put("/api/users/me/settings"), Map.of("dailyGoal", 5), token)
                .andExpect(status().isOk()));
        assertEquals("Asia/Tokyo", updated.get("timeZone").asString());
        assertEquals(5, updated.get("dailyGoal").asInt());

        perform(put("/api/users/me/settings"), Map.of("timeZone", "Mars/Olympus"), token)
                .andExpect(status().isBadRequest());
        perform(put("/api/users/me/settings"), Map.of("dailyGoal", 1), token)
                .andExpect(status().isBadRequest());

        JsonNode forecast = read(perform(get("/api/stats/forecast?days=7"), null, token).andExpect(status().isOk()));
        assertEquals(7, forecast.size());
    }

    @Test
    void forgottenCardIsDrilledAgainWithoutExtraPenalty() throws Exception {
        String deckId = createDeck("Animals", Map.of("cat", "кошка"));

        JsonNode session = read(perform(post("/api/training/sessions"),
                Map.of("deckId", deckId, "mode", "flashcards"), token).andExpect(status().isCreated()));
        String sessionId = session.get("sessionId").asString();
        String cardId = session.get("firstCard").get("cardId").asString();
        assertEquals("кошка", session.get("firstCard").get("answer").asString());

        JsonNode first = answer(sessionId, Map.of("cardId", cardId, "grade", "again"));
        assertFalse(first.get("correct").asBoolean());
        assertFalse(first.get("finished").asBoolean());
        double easeAfterMiss = first.get("updatedProgress").get("easeFactor").asDouble();
        assertTrue(easeAfterMiss < 2.5);

        JsonNode second = answer(sessionId, Map.of("cardId", cardId, "grade", "again"));
        assertEquals(easeAfterMiss, second.get("updatedProgress").get("easeFactor").asDouble());

        JsonNode third = answer(sessionId, Map.of("cardId", cardId, "grade", "good"));
        assertTrue(third.get("finished").asBoolean());
        assertEquals(easeAfterMiss, third.get("updatedProgress").get("easeFactor").asDouble());
        assertEquals("learning", third.get("updatedProgress").get("status").asString());
        assertEquals(1, third.get("updatedProgress").get("intervalDays").asInt());

        // The card is done: one more answer is rejected.
        perform(post("/api/training/sessions/" + sessionId + "/answers"),
                Map.of("cardId", cardId, "grade", "good"), token).andExpect(status().isUnprocessableContent());

        JsonNode finish = read(perform(post("/api/training/sessions/" + sessionId + "/finish"), null, token)
                .andExpect(status().isOk()));
        assertEquals(0.0, finish.get("accuracyPercent").asDouble());
        assertEquals(3, finish.get("answersTotal").asInt());

        JsonNode hard = read(perform(get("/api/stats/hard-words"), null, token).andExpect(status().isOk()));
        assertEquals("cat", hard.get(0).get("word").asString());
    }

    @Test
    void choiceModeAlwaysOffersFourOptions() throws Exception {
        // One card only: the wrong options come from other decks of the same language pair.
        String deckId = createDeck("Tiny", Map.of("dog", "собака"));

        JsonNode session = read(perform(post("/api/training/sessions"),
                Map.of("deckId", deckId, "mode", "choice"), token).andExpect(status().isCreated()));
        JsonNode options = session.get("firstCard").get("options");
        assertEquals(4, options.size());

        Set<String> unique = new HashSet<>();
        options.forEach(option -> unique.add(option.asString()));
        assertEquals(4, unique.size());
        assertTrue(unique.contains("собака"));

        JsonNode result = answer(session.get("sessionId").asString(),
                Map.of("cardId", session.get("firstCard").get("cardId").asString(), "answer", "собака"));
        assertTrue(result.get("correct").asBoolean());
    }

    @Test
    void typingAcceptsBaseFormOfIrregularVerb() throws Exception {
        String deckId = createDeck("Verbs", Map.of("go – went – gone", "идти"));

        JsonNode session = read(perform(post("/api/training/sessions"),
                Map.of("deckId", deckId, "mode", "typing", "direction", "reverse"), token)
                .andExpect(status().isCreated()));
        assertEquals("идти", session.get("firstCard").get("prompt").asString());

        JsonNode result = answer(session.get("sessionId").asString(),
                Map.of("cardId", session.get("firstCard").get("cardId").asString(), "answer", "Go"));
        assertTrue(result.get("correct").asBoolean());
        assertEquals("good", result.get("grade").asString());
    }

    @Test
    void privateDeckIsInvisibleForOthers() throws Exception {
        String deckId = createDeck("Secret", Map.of("key", "ключ"));
        String stranger = registerAnother();

        perform(get("/api/decks/" + deckId), null, stranger).andExpect(status().isNotFound());
        perform(get("/api/decks/" + deckId + "/cards"), null, stranger).andExpect(status().isNotFound());
        perform(post("/api/decks/" + deckId + "/copy"), null, stranger).andExpect(status().isNotFound());
        perform(post("/api/training/sessions"), Map.of("deckId", deckId), stranger).andExpect(status().isNotFound());
        perform(get("/api/decks/public/" + deckId + "/cards"), null, null).andExpect(status().isNotFound());

        perform(patch("/api/decks/" + deckId), Map.of("isPublic", true), token).andExpect(status().isOk());
        perform(get("/api/decks/" + deckId), null, stranger).andExpect(status().isOk());
        perform(patch("/api/decks/" + deckId), Map.of("title", "Hacked"), stranger).andExpect(status().isForbidden());
        perform(post("/api/decks/" + deckId + "/cards"), Map.of("word", "x", "translation", "y"), stranger)
                .andExpect(status().isForbidden());
    }

    private String createDeck(String title, Map<String, String> words) throws Exception {
        JsonNode languages = read(perform(get("/api/languages"), null, null).andExpect(status().isOk()));
        JsonNode deck = read(perform(post("/api/decks"), Map.of("title", title,
                "sourceLanguageId", idOf(languages, "ru"), "targetLanguageId", idOf(languages, "en"),
                "isPublic", false), token).andExpect(status().isCreated()));
        String deckId = deck.get("id").asString();

        List<Map<String, String>> cards = new ArrayList<>();
        words.forEach((word, translation) -> cards.add(Map.of("word", word, "translation", translation)));
        perform(post("/api/decks/" + deckId + "/cards/bulk"), Map.of("cards", cards), token)
                .andExpect(status().isCreated());
        return deckId;
    }

    private JsonNode answer(String sessionId, Map<String, String> body) throws Exception {
        return read(perform(post("/api/training/sessions/" + sessionId + "/answers"), body, token)
                .andExpect(status().isOk()));
    }

    private String registerAnother() throws Exception {
        String name = "s" + UUID.randomUUID().toString().substring(0, 8);
        return read(perform(post("/api/auth/register"),
                Map.of("username", name, "email", name + "@test.io", "password", "secret123"), null)
                .andExpect(status().isCreated())).get("accessToken").asString();
    }

    private ResultActions perform(MockHttpServletRequestBuilder request, Object body, String bearer) throws Exception {
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        }
        if (bearer != null) {
            request.header("Authorization", "Bearer " + bearer);
        }
        return mvc.perform(request);
    }

    private JsonNode read(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }

    private static long idOf(JsonNode languages, String code) {
        for (JsonNode language : languages) {
            if (code.equals(language.get("code").asString())) {
                return language.get("id").asLong();
            }
        }
        throw new IllegalStateException("No language " + code);
    }
}
