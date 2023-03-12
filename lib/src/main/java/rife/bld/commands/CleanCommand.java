package rife.bld.commands;

import rife.bld.BuildHelp;
import rife.bld.Project;
import rife.bld.commands.exceptions.CommandCreationException;
import rife.tools.FileUtils;
import rife.tools.StringUtils;
import rife.tools.exceptions.FileUtilsErrorException;

import java.util.List;

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
        this(project, null);
    }

    public CleanCommand(Project project, List<String> arguments) {
        if (arguments != null && arguments.size() != 0) {
            throw new CommandCreationException("ERROR: No arguments are expected for downloading.");
        }

        project_ = project;
    }

    public void execute() {
        try {
            FileUtils.deleteDirectory(project_.buildMainDirectory());
            FileUtils.deleteDirectory(project_.buildProjectDirectory());
            FileUtils.deleteDirectory(project_.buildTestDirectory());
        } catch (FileUtilsErrorException e) {
            throw new RuntimeException(e);
        }
    }
}
