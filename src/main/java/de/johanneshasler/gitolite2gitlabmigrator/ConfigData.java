package de.johanneshasler.gitolite2gitlabmigrator;

import de.johanneshasler.gitolite2gitlabmigrator.model.Accessor;
import de.johanneshasler.gitolite2gitlabmigrator.model.Group;
import de.johanneshasler.gitolite2gitlabmigrator.model.Repo;
import de.johanneshasler.gitolite2gitlabmigrator.model.User;

import java.util.*;

public class ConfigData {

    private Map<String, User> users = new HashMap<>();
    private Map<String, Group> groups = new HashMap<>();

    private Map<String, Repo> repos = new HashMap<>();

    private static String adminGroup = "@admin";

    public static String getAdminGroup() {
        return adminGroup;
    }

    public void setAdminGroup(String adminGroup) {
        this.adminGroup = adminGroup;
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public Map<String, Group> getGroups() {
        return groups;
    }

    public Map<String, Repo> getRepos() {
        return repos;
    }

    public void removeUser(String s){
        users.remove(s);
    }

    public void removeUserFromBranchPermissions(Accessor user){
        for (Map.Entry<String, Repo> entry : repos.entrySet()){
            entry.getValue().removeUserFromBranchPermissions(user);
        }
    }
}
