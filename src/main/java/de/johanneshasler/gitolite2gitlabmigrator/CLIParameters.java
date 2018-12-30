package de.johanneshasler.gitolite2gitlabmigrator;

import picocli.CommandLine;

import java.io.FileInputStream;
import java.util.concurrent.Callable;

@CommandLine.Command(description = "This tool parses your gitolite.conf and creates groups, users and repos in GitLab"
                    , name = "gitolite2gitlabMigrator", version = "1.0", mixinStandardHelpOptions = true)
public class CLIParameters implements Callable<Void>{

    @CommandLine.Option(names = {"-c", "--c", "-conf", ".-conf"}, required = true, description = "The Path to the gitolite.conf")
    private String pathToGitoliteConf;

    @CommandLine.Option(names = {"-u", "--u", "-url", "--url"}, required = true, description = "The base url of GitLab")
    private String gitlabURL;

    @CommandLine.Option(names = {"-t", "--t", "-token", "--token"}, required = true, description = "The authentication token for GitLab")
    private String gitlabToken;

    @CommandLine.Option(names = {"-g", "--g", "-gitolite", "--gitolite"}, required = true, description = "The baseurl of gitolite including credentials and trailing slash")
    private String gitoliteBasePath;

    public static void main(String[] args) {
        CommandLine.call(new CLIParameters(), args);
    }

    public Void call() throws Exception {
        Main main = new Main(this);
        return null;
    }

    public String getPathToGitoliteConf() {
        return pathToGitoliteConf;
    }

    public String getGitlabURL() {
        return gitlabURL;
    }

    public String getGitlabToken() {
        return gitlabToken;
    }

    public String getGitoliteBasePath() {
        return gitoliteBasePath;
    }
}
