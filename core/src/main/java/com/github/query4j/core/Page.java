package com.github.query4j.core;

import java.util.List;

/**
 * Interface representing a paginated result set with metadata.
 * 
 * <p>
 * Provides access to paginated query results along with pagination metadata
 * such as page number, total elements, and navigation helpers. This interface
 * follows immutability principles - all instances are read-only snapshots of
 * query results at execution time.
 * </p>
 * 
 * <p>
 * Page numbers are 1-based, meaning the first page is page 1, not page 0.
 * This aligns with common UI pagination patterns and user expectations.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * // Execute paginated query
 * Page<User> userPage = QueryBuilder.forEntity(User.class)
 *     .where("active", true)
 *     .orderBy("lastName")
 *     .page(1, 20)
 *     .findPage();
 * 
 * // Access results and metadata
 * List<User> users = userPage.getContent();
 * System.out.println("Page " + userPage.getNumber() + " of " + userPage.getTotalPages());
 * System.out.println("Total users: " + userPage.getTotalElements());
 * 
 * // Navigation helpers
 * if (userPage.hasNext()) {
 *     // Load next page
 * }
 * }</pre>
 *
 * @param <T> the entity type
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Page<T> {
    
    /**
     * Returns the content of the current page as a list.
     * 
     * <p>
     * The returned list is immutable and contains the entities for this page only.
     * The list may be empty if the page contains no results, but will never be null.
     * </p>
     * 
     * @return the page content, never null but may be empty
     */
    List<T> getContent();

    /**
     * Returns the number of the current page (1-based).
     * 
     * <p>
     * Page numbers start at 1, so the first page is page 1, not page 0.
     * This differs from zero-based pagination used in some frameworks.
     * </p>
     * 
     * @return the current page number, always &gt;= 1
     */
    int getNumber();

    /**
     * Returns the size of the page (maximum number of elements per page).
     * 
     * <p>
     * This represents the requested page size, not the actual number of elements
     * in the current page. Use {@code getContent().size()} to get the actual
     * number of elements returned.
     * </p>
     * 
     * @return the page size, always &gt; 0
     */
    int getSize();

    /**
     * Returns the total number of elements across all pages.
     * 
     * <p>
     * This count reflects the total number of entities matching the query
     * criteria, regardless of pagination. It requires a count query to be
     * executed, which may have performance implications for very large datasets.
     * </p>
     * 
     * @return the total number of elements, always &gt;= 0
     */
    long getTotalElements();

    /**
     * Returns the total number of pages.
     * 
     * <p>
     * Calculated as {@code ceil(totalElements / size)}. Returns 0 if there
     * are no elements, and 1 if there is at least one element.
     * </p>
     * 
     * @return the total number of pages, always &gt;= 0
     */
    int getTotalPages();

    /**
     * Returns whether there is a next page available.
     * 
     * <p>
     * This is a convenience method equivalent to {@code getNumber() < getTotalPages()}.
     * </p>
     * 
     * @return true if there is a next page, false otherwise
     */
    boolean hasNext();

    /**
     * Returns whether there is a previous page available.
     * 
     * <p>
     * This is a convenience method equivalent to {@code getNumber() > 1}.
     * </p>
     * 
     * @return true if there is a previous page, false otherwise
     */
    boolean hasPrevious();

    /**
     * Returns whether this is the first page.
     * 
     * <p>
     * This is a convenience method equivalent to {@code getNumber() == 1}.
     * </p>
     * 
     * @return true if this is the first page, false otherwise
     */
    boolean isFirst();

    /**
     * Returns whether this is the last page.
     * 
     * <p>
     * This is a convenience method equivalent to {@code getNumber() == getTotalPages()}.
     * </p>
     * 
     * @return true if this is the last page, false otherwise
     */
    boolean isLast();
}
