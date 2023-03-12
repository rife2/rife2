package rife.bld.operations;

import rife.bld.BuildHelp;
import rife.bld.Project;
import rife.tools.FileUtils;
import rife.tools.StringUtils;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.File;

public class CleanOperation {
    public static class Help implements BuildHelp {
        public String getDescription() {
            return "Cleans the RIFE2 build files";
        }

        public String getHelp(String topic) {
            return StringUtils.replace("""
                Cleans the RIFE2 build files.
                            
                Usage : ${topic}""", "${topic}", topic);
        }
    }

    private File buildDistDirectory_;
    private File buildMainDirectory_;
    private File buildProjectDirectory_;
    private File buildTestDirectory_;

    public CleanOperation() {
    }

    public void execute() {
        cleanDistDirectory();
        cleanMainDirectory();
        cleanProjectDirectory();
        cleanTestDirectory();
    }

    public void cleanDistDirectory() {
        try {
            FileUtils.deleteDirectory(buildDistDirectory());
        } catch (FileUtilsErrorException e) {
            // no-op
        }
    }

    public void cleanMainDirectory() {
        try {
            FileUtils.deleteDirectory(buildMainDirectory());
        } catch (FileUtilsErrorException e) {
            // no-op
        }
    }

    public void cleanProjectDirectory() {
        try {
            FileUtils.deleteDirectory(buildProjectDirectory());
        } catch (FileUtilsErrorException e) {
            // no-op
        }
    }

    public void cleanTestDirectory() {
        try {
            FileUtils.deleteDirectory(buildTestDirectory());
        } catch (FileUtilsErrorException e) {
            // no-op
        }
    }

    public CleanOperation fromProject(Project project) {
        return buildDistDirectory(project.buildDistDirectory())
            .buildMainDirectory(project.buildMainDirectory())
            .buildProjectDirectory(project.buildProjectDirectory())
            .buildTestDirectory(project.buildTestDirectory());
    }

    public CleanOperation buildDistDirectory(File directory) {
        buildDistDirectory_ = directory;
        return this;
    }

    public CleanOperation buildMainDirectory(File directory) {
        buildMainDirectory_ = directory;
        return this;
    }

    public CleanOperation buildProjectDirectory(File directory) {
        buildProjectDirectory_ = directory;
        return this;
    }

    public CleanOperation buildTestDirectory(File directory) {
        buildTestDirectory_ = directory;
        return this;
    }

    public File buildDistDirectory() {
        return buildDistDirectory_;
    }

    public File buildMainDirectory() {
        return buildMainDirectory_;
    }

    public File buildProjectDirectory() {
        return buildProjectDirectory_;
    }

    public File buildTestDirectory() {
        return buildTestDirectory_;
    }

}
