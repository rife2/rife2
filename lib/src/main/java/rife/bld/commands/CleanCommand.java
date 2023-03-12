package rife.bld.commands;

import rife.bld.BuildHelp;
import rife.bld.Project;
import rife.tools.FileUtils;
import rife.tools.StringUtils;
import rife.tools.exceptions.FileUtilsErrorException;

public class CleanCommand {
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

    private final Project project_;

    public CleanCommand(Project project) {
        project_ = project;
    }

    public void execute() {
        try {
            FileUtils.deleteDirectory(project_.buildDistDirectory());
        } catch (FileUtilsErrorException e) {
            // no-op
        }

        try {
            FileUtils.deleteDirectory(project_.buildMainDirectory());
        } catch (FileUtilsErrorException e) {
            // no-op
        }

        try {
            FileUtils.deleteDirectory(project_.buildProjectDirectory());
        } catch (FileUtilsErrorException e) {
            // no-op
        }

        try {
            FileUtils.deleteDirectory(project_.buildTestDirectory());
        } catch (FileUtilsErrorException e) {
            // no-op
        }
    }
}
