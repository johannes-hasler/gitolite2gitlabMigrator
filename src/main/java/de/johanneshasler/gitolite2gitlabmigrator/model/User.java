package de.johanneshasler.gitolite2gitlabmigrator.model;

public class User extends Accessor {

    private org.gitlab4j.api.models.User gitlabUser;
    boolean external = false;

    public User(String name){
        this.name = name;
    }

    public org.gitlab4j.api.models.User getGitlabUser() {
        return gitlabUser;
    }

    public void setGitlabUser(org.gitlab4j.api.models.User gitlabUser) {
        this.gitlabUser = gitlabUser;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

}
