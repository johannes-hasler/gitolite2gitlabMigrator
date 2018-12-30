package de.johanneshasler.gitolite2gitlabmigrator;

import de.johanneshasler.gitolite2gitlabmigrator.model.*;
import org.apache.commons.io.input.BOMInputStream;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class GitoliteConfigParser {

    private static final String ADMIN_REPO = "gitolite-admin";

    private static final String OWNER = "owner";
    private static final String DESCRIPTION = "desc";
    private static final String CATEGORY = "category";


    public ConfigData parse(InputStream inputStream) throws IOException {
        BufferedReader br = getReader(inputStream);
        ConfigData data = new ConfigData();
        String line;
        while ((line = br.readLine()) != null) {
            line = trimAndRemoveComment(line);
            if (line.length() == 0)
               continue;
            parseLine(line, data, br);
        }

        return data;
    }

    private BufferedReader getReader(InputStream inputStream){
        BOMInputStream bis = new BOMInputStream(inputStream);
        InputStreamReader isr = new InputStreamReader(bis);
        BufferedReader br = new BufferedReader(isr);
        return br;
    }

    private String trimAndRemoveComment(String s){
        s = s.trim();
        if (s.contains("#")){
            s = s.substring(0,s.indexOf("#"));
        }
        return s;
    }

    private void parseLine(String line, ConfigData data, BufferedReader br) throws IOException {
        if (line.startsWith("@")){
            parseGroup(line, data);
        }
        else if (line.startsWith("repo")){
            parseRepo(line, data, br);
        }
    }

    public void parseGroup(String line, ConfigData data) {
        int delimiter = line.indexOf('=');
        String groupName = line.substring(0, delimiter).trim();
        String memberPart = line.substring(delimiter + 1).trim();

        Group group = data.getGroups().computeIfAbsent(groupName, x -> new Group(groupName));

        for (String member : memberPart.split(" ")) {
            if (!member.startsWith("@"))
                group.getMembers().add(data.getUsers().computeIfAbsent(member, x -> new User(member)));
            else
                group.getMembers().add(data.getGroups().computeIfAbsent(member, x -> new Group(member)));
        }
    }

    public void parseRepo(String headline, ConfigData data, BufferedReader br) throws IOException{
        Repo repo = new Repo();
        repo.setName(headline.replaceFirst("repo", "").trim());
        String line;
        while((line = br.readLine()) != null){
            line = trimAndRemoveComment(line);
            if (line.length() == 0){
                continue;
            }
            if (line.startsWith("@") || line.startsWith("repo")){ //End of repo-definition
                break;
            }

            if(line.contains("\"")){
//                if(line.contains(DESCRIPTION))
//                //TODO Handle gitweb config
                continue;
            }
            String[] splittedLine = line.split("=");
            splittedLine = trimStringsInArray(splittedLine);

            int endOfPermission = getEndOfPermission(splittedLine[0]);

            String permission = splittedLine[0].substring(0,endOfPermission);
            String branchname = "";
            if(splittedLine[0].length() > endOfPermission)
                branchname = splittedLine[0].substring(endOfPermission+1);
            Set<Accessor> accessors = getAccessors(data, splittedLine[1]);
            if(!ADMIN_REPO.equals(repo.getName()))
                repo.getBranchPermissions().add(new BranchPermission(permission, branchname, accessors));
            else {
                if(!permission.equals("R") || !permission.equals("-"))
                    for (Accessor accessor : accessors){
                        accessor.setAdmin(true);
                    }
            }
        }
        if(!ADMIN_REPO.equals(repo.getName()))
            data.getRepos().put(repo.getName(), repo);
        if (line != null)
            parseLine(line,data,br);
    }

    private Set<Accessor> getAccessors(ConfigData data, String s) {
        Set<Accessor> accessors = new HashSet<>();
        for (String accessor : s.split(" ")){
            if(accessor.startsWith("@")) {
                    accessors.add(data.getGroups().computeIfAbsent(accessor,x-> new Group(accessor)));
            }
            else
                accessors.add(data.getUsers().computeIfAbsent(accessor,x -> new User(accessor)));
        }
        return accessors;
    }

//    public Set<String> removeSubgroups(Set<String> members){
//        Set<String> subgroups =  members.stream().filter(m -> m.startsWith("@")).collect(Collectors.toSet());
//        members.removeAll(subgroups);
//        return subgroups;
//    }

    private int getEndOfPermission(String s) {
        int endOfPermission = s.indexOf(' ');
        return endOfPermission == -1 ? s.length() : endOfPermission;
    }

    private String[] trimStringsInArray(String[] original){
        String[] trimmedCopy = new String[original.length];
        for (int i = 0; i < original.length; i++) {
            trimmedCopy[i] = original[i].trim();
        }
        return trimmedCopy;
    }

}
