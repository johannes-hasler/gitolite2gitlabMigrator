package de.johanneshasler.gitolite2gitlabmigrator.model;

import org.gitlab4j.api.models.AccessLevel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BranchPermission {

    public static final String R = "R";
    public static final String RW = "RW"; //not Owner
    public static final String RWP = "RW+"; //Owner
    public static final String M = "-";

    private String permission;
    private String branchname;
    private Set<Accessor> accessors = new HashSet<>();

    public BranchPermission(String permission, String branchname, Set<Accessor> accessors){
        this.permission = permission;
        this.branchname = branchname;
        this.accessors.addAll(accessors);
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getBranchname() {
        return branchname;
    }

    public void setBranchname(String branchname) {
        this.branchname = branchname;
    }

    public AccessLevel getAccessLevel(){
        return getAccessLevel(getPermission());
    }

    private AccessLevel getAccessLevel(String gitoliteAccess){
        if(R.equals(gitoliteAccess)){
            return AccessLevel.GUEST;
        }
        else if(RW.equals(gitoliteAccess)){
            return AccessLevel.DEVELOPER;
        }
        else if (RWP.equals(gitoliteAccess)){
            return AccessLevel.MAINTAINER;
        }
        return AccessLevel.NONE;
    }

    public Set<Accessor> getAccessors() {
        return accessors;
    }

    public void setAccessors(Set<Accessor> accessors) {
        this.accessors = accessors;
    }
}
