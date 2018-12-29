# gitolite2gitlabMigrator

This will be an migrator which parses the gitolite config and imports users, groups and repos to GitLab.

**THIS IS STILL UNDER CONSTRUCTION**

**YOU CAN FORK THIS AND ADAPT IT TO FIT YOUR NEEDS IF YOU WANT TO USE THIS ALPHA-VERSION**

---

Build
=====
Just run `mvn package`

USAGE
=====
`java -jar gitolite2gitlabmigrator-1.0-SNAPSHOT-jar-with-dependencies.jar -c gitolite.conf -u http://urlToYourGitlab.com -t xxxGitlabTokenxxx `

Commandline Options:

    @CommandLine.Option(names = {"-c", "--c", "-conf", ".-conf"}, required = true, description = "The Path to the gitolite.conf")   

    @CommandLine.Option(names = {"-u", "--u", "-url", "--url"}, required = true, description = "The base url of GitLab")

    @CommandLine.Option(names = {"-t", "--t", "-token", "--token"}, required = true, description = "The authenication token for GitLab")