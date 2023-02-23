package com.tuigroup.tuihomework.client;

import com.tuigroup.tuihomework.exception.UserNotFoundException;
import com.tuigroup.tuihomework.model.GithubBranch;
import com.tuigroup.tuihomework.model.GithubRepository;
import com.tuigroup.tuihomework.model.GithubUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class GithubClient {
    private static final String REPOS = "repos";
    private static final String BRANCHES = "branches";
    private static final String USERS = "users";
    private static final String USER = "user";
    private static final String HEADER_VALUE_SEPARATOR = ";";
    private static final String PAGE = "page";
    private static final String INITIAL_PAGE = "1";
    private static final String PER_PAGE_REQUEST_PARAM = "per_page";
    public static final String NO_URL = "";
    private final RestTemplate restTemplate;
    @Value("${github.url}")
    private String githubUrl;
    @Value("${github.params.perPage}")
    private Integer perPage;
    @Value("${github.header}")
    private String header;

    public List<GithubRepository> getUserRepositories(String user) {
        List<GithubRepository> result = new ArrayList<>();

        getUserId(user).ifPresent(userId -> {
            String nextUrl = githubUrlWithParams(USER, userId, REPOS);
            do {
                ResponseEntity<GithubRepository[]> response = restTemplate.getForEntity(nextUrl, GithubRepository[].class);
                nextUrl = getNextUrl(getHeaderValueWithNextUrl(response)).orElse(NO_URL);
                result.addAll(Arrays.asList(response.getBody()));
            } while (!nextUrl.equals(NO_URL));
        });

        return result;
    }

    public List<GithubBranch> getUserRepositoryBranches(String user, String repository) {
        ResponseEntity<GithubBranch[]> response = restTemplate
                .getForEntity(githubUrl(REPOS, user, repository, BRANCHES), GithubBranch[].class);
        return Arrays.asList(response.getBody());
    }

    private Optional<String> getUserId(String user) {
        try {
            String githubUserId = restTemplate.getForEntity(githubUrl(USERS, user), GithubUser.class)
                    .getBody()
                    .getId();
            return Optional.ofNullable(githubUserId).filter(Predicate.not(String::isEmpty));
        } catch (Exception e) {
            if (e.getMessage().contains(HttpStatus.NOT_FOUND.getReasonPhrase()))
                throw new UserNotFoundException();
            throw e;
        }
    }

    private Optional<String> getHeaderValueWithNextUrl(ResponseEntity<GithubRepository[]> response) {
        return response.getHeaders().getValuesAsList(header)
                .stream()
                .flatMap(Stream::ofNullable)
                .filter(headerValue -> headerValue.contains("rel=\"next\""))
                .findFirst();
    }

    private Optional<String> getNextUrl(Optional<String> from) {
        return from
                .map(headerValue -> headerValue.split(HEADER_VALUE_SEPARATOR)[0])
                .stream()
                .flatMap(Stream::ofNullable)
                .map(headerValue -> headerValue.substring(1, headerValue.length() - 1))
                .filter(GithubClient::isValidURL)
                .findFirst();
    }

    private String githubUrl(String... pathSegments) {
        return UriComponentsBuilder.fromUriString(githubUrl)
                .pathSegment(pathSegments)
                .build()
                .toUriString();
    }

    private String githubUrlWithParams(String... pathSegments) {
        return UriComponentsBuilder.fromUriString(githubUrl)
                .pathSegment(pathSegments)
                .queryParam(PER_PAGE_REQUEST_PARAM, perPage)
                .queryParam(PAGE, INITIAL_PAGE)
                .build()
                .toUriString();
    }

    private static boolean isValidURL(String urlString) {
        try {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
