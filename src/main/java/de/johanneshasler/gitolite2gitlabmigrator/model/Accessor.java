package de.johanneshasler.gitolite2gitlabmigrator.model;

public abstract class Accessor {
    protected String name;
    protected boolean admin = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @Override
    public String toString() {
        return name;
    }
}
