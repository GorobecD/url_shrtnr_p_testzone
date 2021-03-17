package edu.kpi.testcourse.entities;

/**
 * A single shortened URL.
 *
 * @param alias The unique short identifier of this URL. Prepend <tt>https://example.org/r/</tt> to get the short URL.
 * @param destinationUrl full version of URL
 * @param email an email of user that created this alias
 */
public record UrlAlias(String alias, String destinationUrl, String email) { }
