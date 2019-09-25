package org.kohsuke.github;

/**
 * Provides the media type strings for GitHub API previews
 *
 * https://developer.github.com/v3/previews/
 *
 * @author Kohsuke Kawaguchi
 */
/*package*/ class Previews {

    /**
     * Require multiple approving reviews
     *
     * @see <a href="https://developer.github.com/v3/previews/#require-multiple-approving-reviews">GitHub API Previews</a>
     */
    static final String LUKE_CAGE = "application/vnd.github.luke-cage-preview+json";

    /**
     * Reactions
     *
     * @see <a href="https://developer.github.com/v3/previews/#reactions">GitHub API Previews</a>
     */
    static final String SQUIRREL_GIRL = "application/vnd.github.squirrel-girl-preview";

    /**
     * Commit Search
     *
     * @see <a href="https://developer.github.com/v3/previews/#commit-search">GitHub API Previews</a>
     */
    static final String CLOAK = "application/vnd.github.cloak-preview+json";

    /**
     * Require signed commits
     *
     * @see <a href="https://developer.github.com/v3/previews/#require-signed-commits">GitHub API Previews</a>
     */
    static final String ZZZAX = "application/vnd.github.zzzax-preview+json";
    static final String MERCY = "application/vnd.github.mercy-preview+json";
}
