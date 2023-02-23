package com.tuigroup.tuihomework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(value = "integration")
@SpringBootTest(classes = TuiHomeworkApplication.class)
@AutoConfigureMockMvc
class TuiHomeworkApplicationITTest {

    @Autowired
    private MockMvc mockMvc;

    private final WireMockServer wireMockServer = new WireMockServer();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void before() {
        wireMockServer.start();
    }

    @AfterEach
    void after() {
        wireMockServer.stop();
    }

    @ParameterizedTest
    @CsvSource(value = {"xyuvw, 123456, tui-web-app"})
    void getRepositories(String user, String userId, String repository) throws Exception {

        String usersUrl = String.format("/users/%s", user);
        String reposUrlRegex = String.format("/user/%s/repos*", userId);
        String branchesUrl = String.format("/repos/%s/%s/branches", user, repository);

        stubFor(get(urlEqualTo(usersUrl)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(getFileContent("mocked-users.json"))));
        stubFor(get(urlPathMatching(reposUrlRegex)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(getFileContent("mocked-repos.json"))));
        stubFor(get(urlEqualTo(branchesUrl)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(getFileContent("mocked-branches.json"))));

        String actualResponse = mockMvc
                .perform(MockMvcRequestBuilders.get(String.format("/repos/%s", user)).contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(getRequestedFor(urlEqualTo(usersUrl)));
        verify(getRequestedFor(urlPathMatching(reposUrlRegex)));
        verify(getRequestedFor(urlEqualTo(branchesUrl)));

        JSONAssert.assertEquals(getFileContent("expected-response.json"), actualResponse, JSONCompareMode.LENIENT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"notFoundUser"})
    void getRepositories_notFoundUser(String user) throws Exception {

        String usersUrl = String.format("/users/%s", user);
        String reposUrlRegex = "/user/repos*";
        String branchesUrlRegex = String.format("/repos/%s/([a-z0-9]*)/branches", user);

        stubFor(get(urlEqualTo(usersUrl)).willReturn(aResponse()
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withHeader("Content-Type", "application/json")
                .withBody("""
                        {
                          "message": "Not Found",
                          "documentation_url": "https://docs.github.com/rest/reference/users#get-a-user"
                        }
                        """)));

        MvcResult mockResult = mockMvc
                .perform(MockMvcRequestBuilders.get(String.format("/repos/%s", user)).contentType("application/json"))
                .andExpect(status().isNotFound())
                .andReturn();

        verify(getRequestedFor(urlEqualTo(usersUrl)));
        verify(exactly(0), getRequestedFor(urlPathMatching(reposUrlRegex)));
        verify(exactly(0), getRequestedFor(urlPathMatching(branchesUrlRegex)));

        assertTrue(mockResult.getResolvedException() instanceof ResponseStatusException);
    }

    @ParameterizedTest
    @CsvSource(value = {"abcd, 11111"})
    void getRepositories_userWithNoRepositories(String user, String userId) throws Exception {

        String usersUrl = String.format("/users/%s", user);
        String reposUrlRegex = String.format("/user/%s/repos*", userId);
        String branchesUrlRegex = String.format("/repos/%s/([a-z0-9]*)/branches", user);

        stubFor(get(urlEqualTo(usersUrl)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(getFileContent("mocked-users-userWithNoRepositories.json"))));
        stubFor(get(urlPathMatching(reposUrlRegex)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("[]")));

        String actualResponse = mockMvc
                .perform(MockMvcRequestBuilders.get(String.format("/repos/%s", user)).contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(getRequestedFor(urlEqualTo(usersUrl)));
        verify(getRequestedFor(urlPathMatching(reposUrlRegex)));
        verify(exactly(0), getRequestedFor(urlPathMatching(branchesUrlRegex)));

        JSONAssert.assertEquals(getFileContent("expected-response-userWithNoRepositories.json"), actualResponse, JSONCompareMode.LENIENT);
    }

    private String getFileContent(String filePath) throws IOException {
        Path resourceDirectory = Paths.get("src", "it", "resources");
        Path pathToFile = resourceDirectory.resolve(filePath);
        if (!pathToFile.toFile().exists()) {
            throw new IllegalArgumentException("The file does not exist " + pathToFile.getFileName());
        }
        Object savedResult = objectMapper.readValue(pathToFile.toFile(), Object.class);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(savedResult);
    }

}
