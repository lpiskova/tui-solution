package com.tuigroup.tuihomework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.tuigroup.tuihomework.client.GithubClient;
import com.tuigroup.tuihomework.controllers.RepositoryController;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(value = "integration")
@SpringBootTest(classes = TuiHomeworkApplication.class)
@AutoConfigureMockMvc
class TuiHomeworkApplicationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final WireMockServer wireMockServer = new WireMockServer();

    @BeforeEach
    public void before() {
        wireMockServer.start();
    }

    @AfterEach
    void after() {
        wireMockServer.stop();
    }

    @ParameterizedTest
    @CsvSource(value = {"xyuvw, tui-web-app"})
    void getRepositories(String user, String repository) throws Exception {
        String reposUrlRegex = getReposUrlRegex(user);
        String branchesUrlRegex = getBranchesUrlRegex(user, repository);

        stubFor(get(urlPathMatching(reposUrlRegex)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(getFileContent("mocked-repos.json"))));
        stubFor(get(urlEqualTo(branchesUrlRegex)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(getFileContent("mocked-branches.json"))));

        String actualResponse = mockMvc
                .perform(MockMvcRequestBuilders.get(RepositoryController.USER_REPOSITORIES_URL, user))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(getRequestedFor(urlPathMatching(reposUrlRegex)));
        verify(getRequestedFor(urlEqualTo(branchesUrlRegex)));

        JSONAssert.assertEquals(getFileContent("expected-response.json"), actualResponse, JSONCompareMode.LENIENT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abcd"})
    void getRepositories_userWithNoRepositories(String user) throws Exception {
        String reposUrlRegex = getReposUrlRegex(user);
        String branchesUrlRegex = getBranchesUrlRegex(user, "([a-z0-9]*)");

        stubFor(get(urlPathMatching(reposUrlRegex)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("[]")));

        String actualResponse = mockMvc
                .perform(MockMvcRequestBuilders.get(RepositoryController.USER_REPOSITORIES_URL, user))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(getRequestedFor(urlPathMatching(reposUrlRegex)));
        verify(exactly(0), getRequestedFor(urlPathMatching(branchesUrlRegex)));

        JSONAssert.assertEquals(getFileContent("expected-response-userWithNoRepositories.json"), actualResponse, JSONCompareMode.LENIENT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"notFoundUser"})
    void getRepositories_notFoundUser(String user) throws Exception {
        String reposUrlRegex = getReposUrlRegex(user);
        String branchesUrlRegex = getBranchesUrlRegex(user, "([a-z0-9]*)");

        stubFor(get(urlPathMatching(reposUrlRegex)).willReturn(aResponse()
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withHeader("Content-Type", "application/json")
                .withBody("""
                        {
                          "message": "Not Found",
                          "documentation_url": "https://docs.github.com/rest/reference/users#get-a-user"
                        }
                        """)));

        Exception resolvedException = mockMvc
                .perform(MockMvcRequestBuilders.get(RepositoryController.USER_REPOSITORIES_URL, user))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResolvedException();

        verify(getRequestedFor(urlPathMatching(reposUrlRegex)));
        verify(exactly(0), getRequestedFor(urlPathMatching(branchesUrlRegex)));

        assertTrue(resolvedException instanceof HttpClientErrorException.NotFound);
    }

    @ParameterizedTest
    @ValueSource(strings = {"user"})
    void getRepositories_notAcceptableMediaType(String user) throws Exception {
        String reposUrlRegex = getReposUrlRegex(user);
        String branchesUrlRegex = getBranchesUrlRegex(user, "([a-z0-9]*)");

        Exception resolvedException = mockMvc
                .perform(MockMvcRequestBuilders.get(RepositoryController.USER_REPOSITORIES_URL, user)
                        .accept("application/xml"))
                .andExpect(status().isNotAcceptable())
                .andReturn()
                .getResolvedException();

        verify(exactly(0), getRequestedFor(urlPathMatching(reposUrlRegex)));
        verify(exactly(0), getRequestedFor(urlPathMatching(branchesUrlRegex)));

        assertTrue(resolvedException instanceof HttpMediaTypeNotAcceptableException);
    }

    private static String getReposUrlRegex(String user) {
        return UriComponentsBuilder.fromUriString(GithubClient.USERS_USER_REPOS_URL)
                .buildAndExpand(user)
                .toUriString();
    }

    private static String getBranchesUrlRegex(String user, String repository) {
        return UriComponentsBuilder.fromUriString(GithubClient.REPOS_USER_REPOSITORY_BRANCHES_URL)
                .buildAndExpand(user, repository)
                .toUriString();
    }

    private String getFileContent(String filePath) throws IOException {
        Path resourceDirectory = Paths.get("src", "it", "resources");
        Path pathToFile = resourceDirectory.resolve(filePath);
        if (!pathToFile.toFile().exists()) {
            throw new IllegalArgumentException("The file does not exist " + pathToFile.getFileName());
        }
        Object savedResult = objectMapper.readValue(pathToFile.toFile(), Object.class);
        return objectMapper.writeValueAsString(savedResult);
    }

}
