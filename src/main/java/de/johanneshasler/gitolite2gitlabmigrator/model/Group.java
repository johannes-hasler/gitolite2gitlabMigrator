package de.johanneshasler.gitolite2gitlabmigrator.model;

import java.util.HashSet;
import java.util.Set;

public class Group extends Accessor{

    private Set<Accessor> members;
    private boolean admin = false;
    private org.gitlab4j.api.models.Group gitlabGroup;

    public Group(String name){
        this.name = name;
        this.members = new HashSet<>();
    }


    @Override
    public String getName(){
        return name.substring(1);
    }

    public String getGitoliteName(){
        return name;
    }

    public Set<Accessor> getMembers() {
        return members;
    }

    public void setMembers(Set<Accessor> members) {
        this.members = members;
    }

    public org.gitlab4j.api.models.Group getGitlabGroup() {
        return gitlabGroup;
    }

    public void setGitlabGroup(org.gitlab4j.api.models.Group gitlabGroup) {
        this.gitlabGroup = gitlabGroup;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
