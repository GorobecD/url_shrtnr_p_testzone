package edu.kpi.testcourse.entities;

/**
 * User profile, sufficient for signing in. User-generated content is stored separately.
 */
public record User(String email, String passwordHash) { }
