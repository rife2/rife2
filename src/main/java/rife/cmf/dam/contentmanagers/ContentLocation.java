package rife.cmf.dam.contentmanagers;

import rife.cmf.ContentRepository;

/**
 * Contains the location of content in a repository
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public record ContentLocation(String repository, String path) {
    /**
     * Splits the location string into a repository and a path.
     *
     * @param location a location string
     * @return the {@code ContentLocation} instance that was extracted
     * @since 1.0
     */
    public static ContentLocation split(String location) {
        if (null == location) throw new IllegalArgumentException("location can't be null");
        if (0 == location.length()) throw new IllegalArgumentException("location can't be empty");

        var colon_index = location.indexOf(":");

        String repository = null;
        String path;
        if (colon_index != -1) {
            repository = location.substring(0, colon_index);
            path = location.substring(colon_index + 1);
        } else {
            path = location;
        }

        if (null == repository ||
            0 == repository.length()) {
            repository = ContentRepository.DEFAULT;
        }

        if (0 == path.length()) throw new IllegalArgumentException("path can't be empty");
        if (!path.startsWith("/")) throw new IllegalArgumentException("path needs to be absolute");

        return new ContentLocation(repository, path);
    }
}
