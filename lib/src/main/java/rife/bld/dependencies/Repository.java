package rife.bld.dependencies;

public record Repository(String url) {
    public static final Repository MAVEN_CENTRAL = new Repository("https://repo1.maven.org/maven2/");

    public String getArtifactUrl(Dependency dependency) {
        var group_path = dependency.groupId().replace(".", "/");
        var result = new StringBuilder(url);
        if (!url.endsWith("/")) {
            result.append("/");
        }
        return result.append(group_path).append("/").append(dependency.artifactId()).append("/").toString();
    }
}