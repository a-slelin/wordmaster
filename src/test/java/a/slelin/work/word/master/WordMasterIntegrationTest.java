package a.slelin.work.word.master;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full application on a real PostgreSQL: migrations, seed data, security and the main user flow.
 */
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "wordmaster.admin.username=admin",
        "wordmaster.admin.email=admin@test.io",
        "wordmaster.admin.password=admin12345",
        "liquibase.analytics.enabled=false"
})
class WordMasterIntegrationTest {

    private static final String STARTER_DECK = "00000000-0000-0000-0000-000000000101";

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JsonMapper json;

    private String token;

    @BeforeEach
    void register() throws Exception {
        String name = "u" + UUID.randomUUID().toString().substring(0, 8);
        JsonNode auth = perform(post("/api/auth/register"),
                Map.of("username", name, "email", name + "@test.io", "password", "secret123"), null)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString().transform(json::readTree);
        token = auth.get("accessToken").asString();
        assertNotNull(auth.get("refreshToken").asString());
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
