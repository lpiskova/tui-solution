package com.tuigroup.tuihomework.client;

import com.tuigroup.tuihomework.model.Branch;
import com.tuigroup.tuihomework.model.Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GithubClient {
    public static final String USERS_USER_REPOS_URL = "/users/{user}/repos";
    public static final String REPOS_USER_REPOSITORY_BRANCHES_URL = "/repos/{user}/{repository}/branches";
    private static final String NO_URL = "";
    private static final String PAGE_REQUEST_PARAM = "page";
    private static final String INITIAL_PAGE = "1";
    private static final String PER_PAGE_REQUEST_PARAM = "per_page";
    private static final String LINK_HEADER = "Link";
    private static final String NEXT_PAGE_HEADER_LINK_INDICATOR = "rel=\"next\"";
    private static final String HEADER_VALUE_SEPARATOR = ";";

    private final RestTemplate restTemplate;

    @Value("${github.params.perPage}")
    private Integer perPage;

    public List<Repository> getUserRepositories(String user) {
        List<Repository> result = new ArrayList<>();
        String nextUrl = getFirstPageUrl(user);

        do {
            ResponseEntity<List<Repository>> response = restTemplate.exchange(
                    nextUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );
            nextUrl = getNextPageUrl(response.getHeaders());
            if (response.getBody() != null)
                result.addAll(response.getBody());
        } while (!nextUrl.equals(NO_URL));

        return result;
    }

    public List<Branch> getUserRepositoryBranches(String user, String repository) {
        ResponseEntity<List<Branch>> response = restTemplate.exchange(
                REPOS_USER_REPOSITORY_BRANCHES_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                },
                user,
                repository
        );
        return response.getBody();
    }

    String getFirstPageUrl(String user) {
        return UriComponentsBuilder.fromUriString(USERS_USER_REPOS_URL)
                .queryParam(PER_PAGE_REQUEST_PARAM, perPage)
                .queryParam(PAGE_REQUEST_PARAM, INITIAL_PAGE)
                .buildAndExpand(user)
                .toUriString();
    }


    private String getNextPageUrl(HttpHeaders headers) {
        try {
            List<String> headerValues = headers.getValuesAsList(LINK_HEADER);
            headerValues.removeIf(headerValue -> !headerValue.contains(NEXT_PAGE_HEADER_LINK_INDICATOR));
            String nextPageLinkHeaderValue = headerValues.get(0).split(HEADER_VALUE_SEPARATOR)[0];
            String nextPageUrl = nextPageLinkHeaderValue.substring(1, nextPageLinkHeaderValue.length() - 1);
            if (isValidURL(nextPageUrl))
                return nextPageUrl;
        } catch (Exception e) {
            return NO_URL;
        }
        return NO_URL;
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
