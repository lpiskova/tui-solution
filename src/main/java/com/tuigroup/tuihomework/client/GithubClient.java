package com.tuigroup.tuihomework.client;

import com.tuigroup.tuihomework.client.model.Branch;
import com.tuigroup.tuihomework.client.model.Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GithubClient {
    public static final String USERS_USER_REPOS_URL = "/users/{user}/repos";
    public static final String REPOS_USER_REPOSITORY_BRANCHES_URL = "/repos/{user}/{repository}/branches";
    private static final String NO_URL = "";
    public static final String PAGE_REQUEST_PARAM = "page";
    public static final String PER_PAGE_REQUEST_PARAM = "per_page";
    private static final String HEADER = "Link";
    private static final String HEADER_NEXT_PAGE_INDICATOR = "rel=\"next\"";
    private static final String HEADER_VALUE_SEPARATOR = ";";

    private final RestTemplate restTemplate;

    public List<Repository> getUserRepositories(String user, int fromPage, int perPage) {
        List<Repository> result = new ArrayList<>();
        String nextUrl = getFirstPageUrl(user, fromPage, perPage);

        do {
            ResponseEntity<List<Repository>> response = restTemplate.exchange(
                    nextUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );
            nextUrl = getNextPageUrl(response.getHeaders());
            Optional.ofNullable(response.getBody()).map(result::addAll);
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

    private String getFirstPageUrl(String user, int fromPage, int perPage) {
        return UriComponentsBuilder.fromUriString(USERS_USER_REPOS_URL)
                .queryParam(PER_PAGE_REQUEST_PARAM, perPage)
                .queryParam(PAGE_REQUEST_PARAM, fromPage)
                .buildAndExpand(user)
                .toUriString();
    }


    private String getNextPageUrl(HttpHeaders currentPageHeaders) {
        try {
            List<String> headerValues = currentPageHeaders.getValuesAsList(HEADER);
            headerValues.removeIf(headerValue -> !headerValue.contains(HEADER_NEXT_PAGE_INDICATOR));
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
        } catch (URISyntaxException | MalformedURLException e) {
            return false;
        }
    }

}
