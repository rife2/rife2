package rife.bld.commands;

import rife.bld.BuildHelp;
import rife.bld.Project;
import rife.bld.commands.exceptions.CommandCreationException;
import rife.bld.dependencies.*;
import rife.tools.StringUtils;

import java.util.List;

public class DownloadCommand {
    public static class Help implements BuildHelp {
        public String getDescription() {
            return "Downloads all dependencies of a RIFE2 application";
        }

        public String getHelp(String topic) {
            return StringUtils.replace("""
                Downloads all dependencies of a RIFE2 application
                            
                Usage : ${topic}""", "${topic}", topic);
        }
    }

    private final Project project_;

    public DownloadCommand(Project project) {
        this(project, null);
    }

    public DownloadCommand(Project project, List<String> arguments) {
        if (arguments != null && arguments.size() != 0) {
            throw new CommandCreationException("ERROR: No arguments are expected for downloading.");
        }

        project_ = project;
    }

    public void execute() {
        var compile_deps = NewProjectInfo.DEPENDENCIES.get(Scope.compile);
        if (compile_deps != null) {
            for (var dependency : compile_deps) {
                new DependencyResolver(NewProjectInfo.REPOSITORIES, dependency)
                    .downloadTransitivelyIntoFolder(project_.libCompileDirectory(), Scope.compile);
            }
        }

        var runtime_deps = NewProjectInfo.DEPENDENCIES.get(Scope.runtime);
        if (runtime_deps != null) {
            for (var dependency : runtime_deps) {
                new DependencyResolver(NewProjectInfo.REPOSITORIES, dependency)
                    .downloadTransitivelyIntoFolder(project_.libRuntimeDirectory(), Scope.runtime);
            }
        }

        var standalone_deps = NewProjectInfo.DEPENDENCIES.get(Scope.standalone);
        if (standalone_deps != null) {
            for (var dependency : standalone_deps) {
                new DependencyResolver(NewProjectInfo.REPOSITORIES, dependency)
                    .downloadTransitivelyIntoFolder(project_.libStandaloneDirectory(), Scope.compile, Scope.runtime);
            }
        }

        var test_deps = NewProjectInfo.DEPENDENCIES.get(Scope.test);
        if (test_deps != null) {
            for (var dependency : test_deps) {
                new DependencyResolver(NewProjectInfo.REPOSITORIES, dependency)
                    .downloadTransitivelyIntoFolder(project_.libTestDirectory(), Scope.compile, Scope.runtime);
            }
        }
    }
}
