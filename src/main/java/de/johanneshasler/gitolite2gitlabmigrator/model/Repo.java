package de.johanneshasler.gitolite2gitlabmigrator.model;

import de.johanneshasler.gitolite2gitlabmigrator.ConfigData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Repo{

    private String name;
    private String gitWebOwner;
    private String gitWebDescription;

    private List<BranchPermission> branchPermissions = new ArrayList<>();

    public Repo(){}

    public Repo(String name, Repo repo){
        this.name = name;
        this.gitWebOwner = repo.getGitWebOwner();
        this.gitWebDescription = repo.getGitWebDescription();
        this.branchPermissions = repo.getBranchPermissions(); //this is okay, because grouped repos share their permissions
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGitWebOwner() {
        return gitWebOwner;
    }

    public void setGitWebOwner(String gitWebOwner) {
        this.gitWebOwner = gitWebOwner;
    }

    public String getGitWebDescription() {
        return gitWebDescription;
    }

    public void setGitWebDescription(String gitWebDescription) {
        this.gitWebDescription = gitWebDescription;
    }

    public List<BranchPermission> getBranchPermissions() {
        return branchPermissions;
    }

    public void setBranchPermissions(List<BranchPermission> branchPermissions) {
        this.branchPermissions = branchPermissions;
    }

    public Accessor isOwnedByAccessor() {
        if (getBranchPermissions() != null && !getBranchPermissions().isEmpty()) {
            final Set<Accessor> accessor = new HashSet<>();
            branchPermissions.stream().forEach(bp -> accessor.addAll(bp.getAccessors().stream().filter(accessor1 -> name.contains(accessor1.getName())).collect(Collectors.toSet())));
            if (accessor.size() == 1) {
                return accessor.iterator().next();
            }
        }
        return null;
    }

    public void removeUserFromBranchPermissions(Accessor user){
        List<BranchPermission> branchPermissionsToRemove = new ArrayList<>();
        for (BranchPermission bp : branchPermissions){
            bp.getAccessors().remove(user);
            if (bp.getAccessors().isEmpty())
                branchPermissionsToRemove.add(bp);
        }
        branchPermissions.removeAll(branchPermissionsToRemove);
    }
}
