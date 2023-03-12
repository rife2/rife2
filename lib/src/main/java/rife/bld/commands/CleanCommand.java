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

    public final Project project;

    public CleanCommand(Project project) {
        this.project = project;
    }

    public void execute() {
        try {
            FileUtils.deleteDirectory(project.buildDistDirectory());
        } catch (FileUtilsErrorException e) {
            // no-op
        }

        try {
            FileUtils.deleteDirectory(project.buildMainDirectory());
        } catch (FileUtilsErrorException e) {
            // no-op
        }

        try {
            FileUtils.deleteDirectory(project.buildProjectDirectory());
        } catch (FileUtilsErrorException e) {
            // no-op
        }

        try {
            FileUtils.deleteDirectory(project.buildTestDirectory());
        } catch (FileUtilsErrorException e) {
            // no-op
        }
    }
}
