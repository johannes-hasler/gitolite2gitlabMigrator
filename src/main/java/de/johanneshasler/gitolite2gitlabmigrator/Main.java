package de.johanneshasler.gitolite2gitlabmigrator;

import java.io.FileInputStream;
import java.io.IOException;

public class Main {

    public Main(CLIParameters parameters) throws IOException {
        GitoliteConfigParser parser = new GitoliteConfigParser();
        FileInputStream fis = new FileInputStream(parameters.getPathToGitoliteConf());
        ConfigData data = parser.parse(fis);
        DataPreparator.prepareData(data);

        GitlabService gitlabService = new GitlabService();
        gitlabService.migrate(data, parameters.getGitlabURL(), parameters.getGitlabToken());
    }

}
