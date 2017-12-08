
Releasing a new version of jgettext

Requirements:
* login for oss.sonatype.org (OSSRH)
* access to Tennera group in OSSRH: https://issues.sonatype.org/browse/OSSRH-3983
* GPG key, gpg agent set up (see https://dingyichen.wordpress.com/2015/11/10/fedora-maven-gpg-plugin-keep-asking-passphrase/ if your GPG and gpg agent don't match)


Steps:

* from command line:
 * git checkout master
 * git pull
 * mvn release:prepare release:perform
* in web browser:
 * log in to https://oss.sonatype.org/
 * visit https://oss.sonatype.org/#stagingRepositories
 * find repository starting with orgfedorahostedtennera
 * Close
 * hit refresh icon and wait...
 * Release and drop repository
