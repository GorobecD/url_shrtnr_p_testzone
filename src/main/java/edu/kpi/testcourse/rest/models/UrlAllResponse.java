package edu.kpi.testcourse.rest.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Successful result of URL shortening.
 */
public record UrlAllResponse(
    @JsonProperty("alias") String alias,
    @JsonProperty("full_url") String fullUrl
) {

}
