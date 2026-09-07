package com.example.microservice.organizations.company;

import com.example.microservice.common.tenant.TenantContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.LinkedHashMap;
import java.util.Map;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CompanyApiIntegrationTest {
    private static final String COMPANIES = "/companies";

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private JdbcTemplate jdbc;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void prepare() throws Exception {
        jdbc.update("DELETE FROM organizations");
        adminToken = login("admin", "admin123");
        userToken = login("user", "user123");
    }

    @AfterEach
    void clearContexts() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void inheritedCrudRoutesAndCustomSearchWorkTogether() throws Exception {
        JsonNode created = data(send(post(COMPANIES), adminToken,
                company("akhila-01", "Akhila Company", "akhila@example.test"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.companyCode").value("AKHILA-01"))
                .andReturn());
        assertThat(created.path("id").isIntegralNumber()).isTrue();
        assertThat(created.path("id").asLong()).isPositive();
        String id = created.path("id").asText();
        assertThat(created.path("createdAt").asText()).contains("T");
        assertThat(created.path("createdBy").asText()).isEqualTo("admin");
        assertThat(jdbc.queryForObject("SELECT tenant_id FROM organizations WHERE id = ?", String.class, Long.valueOf(id))).isEqualTo("default");
        assertThat(jdbc.queryForObject("SELECT version FROM organizations WHERE id = ?", Long.class, Long.valueOf(id))).isEqualTo(0L);

        send(get(COMPANIES + "/" + id), adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyName").value("Akhila Company"));
        send(get(COMPANIES).param("page", "0").param("size", "20").param("sort", "id,desc"), adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
        send(get(COMPANIES + "/all"), adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
        send(get(COMPANIES + "/search").param("query", "AKHILA"), adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].companyCode").value("AKHILA-01"));

        send(put(COMPANIES + "/" + id), adminToken,
                company("akhila-01", "Akhila Company Updated", "updated@example.test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyName").value("Akhila Company Updated"));

        assertThat(jdbc.queryForObject("SELECT version FROM organizations WHERE id = ?", Long.class, Long.valueOf(id))).isEqualTo(1L);
        assertThat(jdbc.queryForObject("SELECT created_by FROM organizations WHERE id = ?", String.class, Long.valueOf(id))).isEqualTo("admin");
        send(delete(COMPANIES + "/" + id), adminToken)
                .andExpect(status().isOk());
        send(get(COMPANIES + "/" + id), adminToken)
                .andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM organizations", Integer.class)).isZero();
    }

    @Test
    void searchMatchesCodeNameAndEmailIgnoringCase() throws Exception {
        create(company("unique-code", "Northern Company", "mailbox@example.test"));
        assertSearch("UNIQUE", "UNIQUE-CODE");
        assertSearch("nOrThErN", "UNIQUE-CODE");
        assertSearch("MAILBOX", "UNIQUE-CODE");
    }

    @Test
    void searchTreatsSqlWildcardCharactersAsLiteralText() throws Exception {
        create(company("literal", "100% Under_score", "literal@example.test"));
        create(company("control", "1000 UnderXscore", "control@example.test"));
        assertSearch("%", "LITERAL");
        assertSearch("_", "LITERAL");
    }

    @Test
    void searchValidatesRequiredQuery() throws Exception {
        send(get(COMPANIES + "/search"), adminToken).andExpect(status().isBadRequest());
        send(get(COMPANIES + "/search").param("query", "   "), adminToken).andExpect(status().isBadRequest());
        send(get(COMPANIES + "/search").param("query", "x".repeat(101)), adminToken)
                .andExpect(status().isBadRequest());
    }

    @Test
    void inheritedCreateAndUpdateApplyValidationAndUniquenessHooks() throws Exception {
        create(company("reserved", "Reserved", "reserved@example.test"));
        send(post(COMPANIES), adminToken, company(" RESERVED ", "Duplicate", "duplicate@example.test"))
                .andExpect(status().isConflict());

        Map<String, Object> invalid = company("invalid", "Invalid", "not-an-email");
        send(post(COMPANIES), adminToken, invalid).andExpect(status().isBadRequest());
    }

    @Test
    void inheritedRoutesRequireAuthenticationButAllowAuthenticatedTeamUsers() throws Exception {
        mvc.perform(get(COMPANIES)).andExpect(status().isUnauthorized());
        send(get(COMPANIES), userToken).andExpect(status().isOk());
        send(get(COMPANIES + "/search").param("query", "anything"), userToken)
                .andExpect(status().isOk());
    }

    @Test
    void invalidAndMissingIdsReturnClientErrors() throws Exception {
        send(get(COMPANIES + "/not-a-number"), adminToken).andExpect(status().isBadRequest());
        send(get(COMPANIES + "/0"), adminToken).andExpect(status().isBadRequest());
        send(put(COMPANIES + "/-1"), adminToken, company("bad", "Bad", "bad@example.test")).andExpect(status().isBadRequest());
        send(delete(COMPANIES + "/0"), adminToken).andExpect(status().isBadRequest());
        send(get(COMPANIES + "/" + Long.MAX_VALUE), adminToken).andExpect(status().isNotFound());
    }

    private void assertSearch(String query, String expectedCode) throws Exception {
        send(get(COMPANIES + "/search").param("query", query), adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].companyCode").value(expectedCode));
    }

    private JsonNode create(Map<String, Object> body) throws Exception {
        return data(send(post(COMPANIES), adminToken, body)
                .andExpect(status().isCreated())
                .andReturn());
    }

    private String login(String username, String password) throws Exception {
        return data(mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn()).path("accessToken").asText();
    }

    private ResultActions send(MockHttpServletRequestBuilder request, String token) throws Exception {
        return mvc.perform(request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    private ResultActions send(MockHttpServletRequestBuilder request, String token,
                               Map<String, Object> body) throws Exception {
        return mvc.perform(request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body)));
    }

    private JsonNode data(MvcResult result) throws Exception {
        return json.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private static Map<String, Object> company(String code, String name, String email) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("companyCode", code);
        body.put("companyName", name);
        body.put("email", email);
        body.put("industry", "Technology");
        body.put("country", "India");
        body.put("status", "ACTIVE");
        return body;
    }
}


