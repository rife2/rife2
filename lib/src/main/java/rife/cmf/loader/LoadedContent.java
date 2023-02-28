package rife.cmf.loader;

import rife.cmf.MimeType;

/**
 * This holds the content that was loaded by a content loader backend and
 * also tracks the original mime type of the provided data. When the original
 * mime-type was not null, the provided data was in a mime-type that the
 * content management framework natively supports. This can be used to
 * prevent needless content conversion, which can be beneficial in the case
 * of lossy formats.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see ContentLoaderBackend
 * @since 1.4
 */
public record LoadedContent<InternalType>(MimeType originalMimeType, InternalType data) {
}
