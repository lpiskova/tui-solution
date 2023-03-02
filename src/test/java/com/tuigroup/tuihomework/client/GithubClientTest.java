package com.tuigroup.tuihomework.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuigroup.tuihomework.config.RestTemplateConfig;
import com.tuigroup.tuihomework.model.Branch;
import com.tuigroup.tuihomework.model.Commit;
import com.tuigroup.tuihomework.model.Owner;
import com.tuigroup.tuihomework.model.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest
@Import(RestTemplateConfig.class)
public class GithubClientTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${github.url}")
    private String baseUrl;

    private GithubClient githubClient;

    private MockRestServiceServer server;

    private final String user = "xyuvwz";

    private final String repository = "repo";

    private final String branchUrl = UriComponentsBuilder
            .fromUriString(GithubClient.REPOS_USER_REPOSITORY_BRANCHES_URL)
            .buildAndExpand(user, repository)
            .toUriString();

    @BeforeEach
    public void setUp() {
        server = MockRestServiceServer.bindTo(restTemplate).build();
        githubClient = new GithubClient(restTemplate);
    }

    @Test
    public void getUserRepositories_success() throws JsonProcessingException {
        Repository repository = new Repository("repo", new Owner(user), false);
        String payload = objectMapper.writeValueAsString(List.of(repository));
        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(baseUrl + githubClient.getFirstPageUrl(user)))
                .andExpect(queryParam("page", "1"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        List<Repository> result = githubClient.getUserRepositories(user);
        assertThat(result.size()).isEqualTo(1);
        assertEquals(repository.getName(), result.get(0).getName());
        assertEquals(repository.getOwner().getLogin(), result.get(0).getOwner().getLogin());
        assertEquals(repository.isFork(), result.get(0).isFork());
    }

    @Test
    public void getUserRepositories_successWithNoRepositories() throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(List.of());
        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(baseUrl + githubClient.getFirstPageUrl(user)))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        List<Repository> result = githubClient.getUserRepositories(user);
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    public void getUserRepositories_failureWithNotFound() {
        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(baseUrl + githubClient.getFirstPageUrl(user)))
                .andRespond(withResourceNotFound());

        assertThrows(HttpClientErrorException.NotFound.class, () -> githubClient.getUserRepositories(user));
        server.verify();
    }

    @Test
    public void getUserRepositoryBranches_success() throws JsonProcessingException {
        Branch expected = new Branch("branch", new Commit("sha"));
        String payload = objectMapper.writeValueAsString(List.of(expected));
        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(baseUrl + branchUrl))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        List<Branch> actual = githubClient.getUserRepositoryBranches(user, repository);
        assertThat(actual.size()).isEqualTo(1);
        assertEquals(expected.getName(), actual.get(0).getName());
        assertEquals(expected.getCommit().getSha(), actual.get(0).getCommit().getSha());
    }

    @Test
    public void getUserRepositoryBranches_successWithNoBranches() throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(List.of());
        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(baseUrl + branchUrl))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        List<Branch> result = githubClient.getUserRepositoryBranches(user, repository);
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    public void getUserRepositoryBranches_failureWithNotFound() {
        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(baseUrl + branchUrl))
                .andRespond(withResourceNotFound());

        assertThrows(HttpClientErrorException.NotFound.class, () -> githubClient.getUserRepositoryBranches(user, repository));
        server.verify();
    }

}
