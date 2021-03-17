package edu.kpi.testcourse.entities;

/**
 * A bearer token of an authenticated user.
 */
public record AuthToken(String token, String userEmail) {
}
