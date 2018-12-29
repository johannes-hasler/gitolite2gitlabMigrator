package de.johanneshasler.gitolite2gitlabmigrator;

import de.johanneshasler.gitolite2gitlabmigrator.model.Accessor;
import de.johanneshasler.gitolite2gitlabmigrator.model.Group;
import de.johanneshasler.gitolite2gitlabmigrator.model.Repo;
import de.johanneshasler.gitolite2gitlabmigrator.model.User;

import java.util.*;
import java.util.stream.Collectors;

public class DataPreparator {

    static HashSet<String> reservedRepos = new HashSet<>(Arrays.asList(new String[]{"-", "badges", "blame", "blob", "builds", "commits", "create", "create_dir", "edit",
            "environments/folders","files", "find_file", "gitlab-lfs/objects", "info/lfs/objects", "new", "preview",
            "raw", "refs", "tree", "update", "wikis"}));
    static HashSet<String> reservedGroups = new HashSet<>(Arrays.asList(new String[]{"admin"}));

    static String repoSuffix = "_repo";
    static String groupSuffix = "_group";


    public static void prepareData(ConfigData data){
        System.out.println("Preparing gitolite data");
        markAdmins(data);
        markExternals(data);
        avoidDuplicates(data);
        removeCreatorsRepo(data);
        populateSubrepos(data);
    }

    private static void markExternals(ConfigData data){
        data.getUsers().entrySet().stream().filter(e -> e.getValue().getName().startsWith("ext")).forEach(e -> e.getValue().setExternal(true));
    }

    private static void markAdmins(ConfigData data){
        Group admins = data.getGroups().get(data.getAdminGroup());
        if(admins != null)
            admins.setAdmin(true);
        data.getGroups().entrySet().stream().filter(e -> e.getValue().isAdmin()).forEach(e -> e.getValue().getMembers().stream().forEach(member -> setAccessorAdmin(member)));
    }

    private static void setAccessorAdmin(Accessor accessor) {
        if (accessor instanceof User) {
            (accessor).setAdmin(true);
        } else if (accessor instanceof Group) {
            ((Group) accessor).getMembers().forEach(member -> setAccessorAdmin(member));
        }
    }
    private static void avoidDuplicates(ConfigData data){
        for(String reservedRepo : reservedRepos){
            if(data.getRepos().keySet().contains(reservedRepo)){
                data.getRepos().put(reservedRepo+repoSuffix, data.getRepos().remove(reservedRepo));
                System.out.println("Renaming reserved repo \""+reservedRepo+"\" to \""+reservedRepo+repoSuffix+"\"");
            }
        }
        for(String reservedGroup : reservedGroups){
            if(data.getGroups().keySet().contains("@"+reservedGroup)){
                Group group = data.getGroups().remove("@"+reservedGroup);
                group.setName("@"+reservedGroup+groupSuffix);
                data.getGroups().put(group.getGitoliteName(),group);
                System.out.println("Renaming reserved group \""+reservedGroup+"\" to \""+reservedGroup+groupSuffix+"\"");
            }
            if(reservedGroup.equals(data.getAdminGroup())){
                data.setAdminGroup(data.getAdminGroup()+groupSuffix);
            }
        }
        for(String user : data.getUsers().keySet()){
            if(data.getGroups().keySet().contains("@"+user)){
                System.out.println("Renaming group \""+"@"+user+"\" to @" + user+groupSuffix);
                Group group = data.getGroups().remove("@"+user);
                group.setName("@"+user+groupSuffix);
                data.getGroups().put(group.getGitoliteName(), group);
            }
        }
    }

    private static void removeCreatorsRepo(ConfigData data){
        Set<String> creatorRepos = data.getRepos().keySet().stream().filter(key -> key.contains("CREATOR")).collect(Collectors.toSet());
        creatorRepos.forEach(key -> data.getRepos().remove(key));
    }

//    private static void mergeBranchPermissions(ConfigData data){ //needed?
//        for (Map.Entry<String, Repo> entry : data.getRepos().entrySet()){
//            entry.getValue().getBranchPermissions().stream().map(bp -> bp.)
//        }
//    }


    private static void populateSubrepos(ConfigData data){
        Map<String,Repo> repos = new HashMap<>();
        Set<String> repoGroups = new HashSet<>();
        for (Map.Entry<String, Repo> entry : data.getRepos().entrySet()){
            if(entry.getKey().startsWith("@")){
                System.out.println("Resolving repo group: "+entry.getKey());
                repoGroups.add(entry.getKey());
                Group subrepos = data.getGroups().remove(entry.getKey());
                for(Accessor accessor : subrepos.getMembers()){
                    repos.put(accessor.getName(),new Repo(accessor.getName(),entry.getValue()));
                    data.getUsers().remove(accessor.getName());
                }
            }
        }
        data.getRepos().putAll(repos);
        for(String groupRepo : repoGroups){
            data.getRepos().remove(groupRepo);
        }
    }

}
