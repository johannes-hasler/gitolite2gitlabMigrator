package de.johanneshasler.gitolite2gitlabmigrator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {

    public Main(CLIParameters parameters) throws IOException {
        GitoliteConfigParser parser = new GitoliteConfigParser();
        try {
            FileInputStream fis = new FileInputStream(parameters.getPathToGitoliteConf());
            ConfigData data = parser.parse(fis);
            DataPreparator.prepareData(data);

            GitlabService gitlabService = new GitlabService();
            gitlabService.migrate(data, parameters.getGitlabURL(), parameters.getGitlabToken(), parameters.getGitoliteBasePath());
        }catch (FileNotFoundException e){
            System.out.println("Invalid path to gitolite.conf");
        }

    }

}
