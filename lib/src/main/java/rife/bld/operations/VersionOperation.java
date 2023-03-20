package rife.bld.operations;

import rife.Version;
import rife.bld.wrapper.Wrapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Outputs the version of the build system.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5.2
 */
public class VersionOperation extends AbstractOperation<VersionOperation> {
    /**
     * Configures a version operation from command-line arguments.
     *
     * @param arguments the arguments that will be considered
     * @return this operation instance
     * @since 1.5.2
     */
    public VersionOperation fromArguments(List<String> arguments) {
        return this;
    }

    /**
     * Performs the version operation.
     *
     * @throws IOException when an error occurred during the upgrade operation
     * @since 1.5.2
     */
    public void execute() {
        if (!silent()) {
            System.out.println("RIFE2 bld v" + Version.getVersion());
        }
    }
}
