package de.johanneshasler.gitolite2gitlabmigrator;

import de.johanneshasler.gitolite2gitlabmigrator.model.Accessor;
import de.johanneshasler.gitolite2gitlabmigrator.model.BranchPermission;
import de.johanneshasler.gitolite2gitlabmigrator.model.Repo;
import org.gitlab4j.api.*;
import org.gitlab4j.api.models.*;

import java.util.*;

public class GitlabService {

    Map<String, Integer> userId = new HashMap<>();
    Map<String, Integer> groupId = new HashMap<>();

    String gitolitebasePath = "";
    String gitSuffix = ".git";
    String mailSuffix = "@syngenio.de";

    public void migrate(ConfigData data, String url, String token){
        GitLabApi gitLabApi = new GitLabApi(url, token);
        addUsers(gitLabApi,data);
        addGroups(gitLabApi,data);
        addProjects(gitLabApi, data);
        System.out.println("Migration finished");
    }

    public void addUsers(GitLabApi api, ConfigData data){
        System.out.println("-- Creating Users --");
        UserApi userApi = api.getUserApi();
        try {
            List<User> users = userApi.getUsers();
            for (User user : users){
                data.removeUser(user.getName());
                userId.put(user.getName(),user.getId());
            }
        }
        catch (GitLabApiException e){
            System.out.println("Error getting users: "+e.getMessage());
        }
        createUsers(userApi, data.getUsers());
    }

    public void createUsers(UserApi userApi,Map<String, de.johanneshasler.gitolite2gitlabmigrator.model.User> users){
        for (Map.Entry<String,de.johanneshasler.gitolite2gitlabmigrator.model.User> gitoliteUser : users.entrySet()){
            try {
                String name = gitoliteUser.getValue().getName();
                User user = userApi.createUser(new User().withEmail(name + mailSuffix).withUsername(name).withName(name).withExternal(gitoliteUser.getValue().isExternal()).withIsAdmin(gitoliteUser.getValue().isAdmin()), "GeneratePasswordHere", false);
                userId.put(name,user.getId());
            }
            catch (GitLabApiException e){
                System.out.println(" Error: User \""+ gitoliteUser + "\" :" + e.getMessage());
            }
        }
    }

    public void addGroups(GitLabApi api, ConfigData data){
        System.out.println("-- Creating Groups --");
        GroupApi groupApi = api.getGroupApi();
        List<Group> groups = new ArrayList<>();
        try {
           groups = groupApi.getGroups();
           for(Group group : groups){
               data.getGroups().remove("@"+group.getName());
               groupId.put(group.getName(), group.getId());
           }
        }
        catch (GitLabApiException e){
            System.out.println("Error getting groups: "+e.getMessage());
        }

        for(Map.Entry<String, de.johanneshasler.gitolite2gitlabmigrator.model.Group> entry : data.getGroups().entrySet()){

                String groupName = entry.getValue().getName();
                Optional<Group> exisitingGroup = groups.stream().filter(g -> groupName.equals(g.getName())).findFirst();
                Group group = null;
                List<Member> members = new ArrayList<>();

            try {
                if (!exisitingGroup.isPresent()) {
                    group = groupApi.addGroup(groupName, groupName);
                }
                else {
                    group = exisitingGroup.get();
                    members = groupApi.getMembers(group.getId());
                }
            }
            catch (GitLabApiException e) {
                System.out.println(" Error: " +"Creating/Getting Group: \""+ entry.getKey().substring(1) + "\" " + e.getMessage());
                continue;
            }
                groupId.put(groupName, group.getId());
            addMembersToGroup(groupApi,group.getPath(),entry.getValue().getMembers(),members);
        }
    }

    public void addMembersToGroup(GroupApi groupApi, String groupPath, Set<Accessor> accessors, List<Member> actualMembers) {
        Integer id;
        for (Accessor accessor : accessors) {
            if (actualMembers != null && actualMembers.stream().anyMatch(m -> accessor.getName().equals(m.getName())))
                continue;
            id = null;
            if (accessor instanceof de.johanneshasler.gitolite2gitlabmigrator.model.User) {
                id = userId.get(accessor.getName());
                if (id != null) {
                    try {
                        actualMembers.add(groupApi.addMember(groupPath, id, ((de.johanneshasler.gitolite2gitlabmigrator.model.User) accessor).isExternal() ? AccessLevel.DEVELOPER : AccessLevel.MAINTAINER));
                    } catch (GitLabApiException e) {
                        System.out.println("Error: Adding member \"" + accessor.getName() + "\" to group \"" + groupPath + "\": " + e.getMessage());
                    }
                } else
                    System.out.println("Warning: " + accessor.getName() + " has no id and will not be added to group \"" + groupPath + "\"");
            } else {
                de.johanneshasler.gitolite2gitlabmigrator.model.Group subgroup = (de.johanneshasler.gitolite2gitlabmigrator.model.Group) accessor;
                if (subgroup.getGitoliteName().equals(ConfigData.getAdminGroup())) {
                    continue;
                }
                addMembersToGroup(groupApi, groupPath, subgroup.getMembers(), actualMembers);
            }
        }
    }

    public void addProjects(GitLabApi api, ConfigData data){
        System.out.println("-- Creating Projects --");
        ProjectApi projectApi = api.getProjectApi();
        RepositoryApi repositoryApi = api.getRepositoryApi();
        ProtectedBranchesApi protectedBranchesApi = api.getProtectedBranchesApi();
        List<Project> projects = new ArrayList<>();
        try {
           projects  = projectApi.getProjects();
        } catch (GitLabApiException e){
            System.out.println("Error fetching projects");
        }

        for (Map.Entry<String, Repo> entry : data.getRepos().entrySet()){
            Repo repo = entry.getValue();
            if(projects.stream().anyMatch(p -> p.getName().equals(entry.getKey()))){
                continue;
            }
            Project project = new Project()
                    .withName(entry.getKey())
                    .withVisibility(Visibility.PRIVATE)
                    .withDescription(repo.getGitWebDescription());
            //Experimental
            //project.setHttpUrlToRepo(gitolitebasePath+repo.getName()+gitSuffix);
            Set<Accessor> members = new HashSet<>();
            Accessor owner = repo.isOwnedByAccessor();
            if(owner != null){
                Integer namespaceId;
                if (owner instanceof de.johanneshasler.gitolite2gitlabmigrator.model.Group)
                    namespaceId = groupId.get(owner.getName());
                else
                   namespaceId = userId.get(owner.getName());
                if(namespaceId != null){
                    project = project.withNamespaceId(namespaceId);
                    members.add(owner);
                }
            }
            try {
                project = projectApi.createProject(project);
            }
            catch (GitLabApiException e){

                System.out.println(" Error: " +"Repo: \""+ project.getName() + "\" " + e.getMessage());
                project = null;
            }
            if(project != null) {

                for (BranchPermission permission : repo.getBranchPermissions()) {
//                    createBranches(repositoryApi, project, permission);
                    //TODO Protect Branches
                    addMembers(projectApi, project, members, permission);
                }
            }
        }
    }

    private void createBranches(RepositoryApi repositoryApi, Project project, BranchPermission permission) {
        try {
            if (!"master".equals(permission.getBranchname()) && !permission.getBranchname().isEmpty()) {
                String branchname = permission.getBranchname();
                branchname = branchname.endsWith("/") ? branchname.substring(0, branchname.length() - 1) : branchname;
                repositoryApi.createBranch(project.getId(), branchname, "master");
            }

        } catch (GitLabApiException e) {
            System.out.println(" Error: " + "Branch: \"" + permission.getBranchname() + "\" " + e.getMessage());
        }
    }

    private void addMembers( ProjectApi projectApi, Project project, Set<Accessor> members, BranchPermission permission) {
        for (Accessor accessor : permission.getAccessors()) {
            try {
                if (!members.contains(accessor)) {
                    if (accessor instanceof de.johanneshasler.gitolite2gitlabmigrator.model.Group)
                        if (!((de.johanneshasler.gitolite2gitlabmigrator.model.Group) accessor).getGitoliteName().equals(ConfigData.getAdminGroup())) {
                            projectApi.shareProject(project.getId(), groupId.get(accessor.getName()), permission.getAccessLevel(), null);
                            members.add(accessor);
                        } else {
                            projectApi.addMember(project.getId(), userId.get(accessor.getName()), permission.getAccessLevel());
                            members.add(accessor);
                        }
                }
            } catch (GitLabApiException | IllegalArgumentException e) {
                System.out.println(" Error: " + "Adding member \"" + accessor.getName() + "\" to repo \"" + project.getName() + "\" " + e.getMessage());
            }
        }
    }
}
