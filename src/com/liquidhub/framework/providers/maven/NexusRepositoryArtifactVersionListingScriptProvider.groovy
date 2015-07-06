package com.liquidhub.framework.providers.maven

import java.util.Map

import com.liquidhub.framework.ci.EmbeddedScriptProvider;

class NexusRepositoryArtifactVersionListingScriptProvider implements EmbeddedScriptProvider {


	@Override
	public String getScript(Map bindings) {
		
		def baseRepositoryUrl = bindings['baseRepositoryUrl'],mavenGroupId=bindings['groupId'], mavenArtifactId = bindings['artifactId']
		
	
		def releaseRepositoryUrl = createRepositoryUrl(baseRepositoryUrl, 'releases', mavenGroupId, mavenArtifactId)

		def snapshotRepositoryUrl = createRepositoryUrl(baseRepositoryUrl, 'snapshots', mavenGroupId, mavenArtifactId)


		//The following is a script snippet which will get embeddedded into the deployment jobs to download artifact versions
		//The following variable bindings are allowed : releaseVersionCountToDisplay, snapshotVersionCountToDisplay

		"""
        |try{
        |def snapshotMetadataUrl = $snapshotRepositoryUrl
		|def releaseMetadataUrl = $releaseRepositoryUrl
		|def artifactVersions = []
        |
		|if(releaseVersionCountToDisplay.toInteger() > 0){
		|  artifactVersions << listVersions(releaseMetadataUrl, releaseVersionCountToDisplay)
		|}
		|
		|if(snapshotVersionCountToDisplay.toInteger() > 0){
		| artifactVersions << listVersions(snapshotMetadataUrl, snapshotVersionCountToDisplay)
		|}
        |
		| artifactVersions.flatten()	
	    |} catch(Exception e){
        |    return ['Could not find any artifact versions. Has anything been uploaded to the artifact repository yet ? ']
        |} 
        |
	    |def listVersions(repositoryLocation, versionCountToDisplay){
		|  def xml = repositoryLocation.toURL().text
		|  def metadata =  new XmlParser().parseText(xml)
		|  def listedVersions= metadata.versioning.versions.version.collect { it.text() }
		|  listedVersions.reverse().take(versionCountToDisplay as int)
		|} 
	    """.stripMargin()
	}


	protected  def createRepositoryUrl(baseRepositoryUrl, repositoryType, mavenGroupId, mavenArtifactId){
		/*
		 * The group id needs to be slashed as part of the url, so instead of com.ibx.frontoffice it should be it com/ibx/frontoffice
		 */
		def transformedGroupId = mavenGroupId.replace(".","/")

		return "'${baseRepositoryUrl}/service/local/repositories/${repositoryType}/content/${transformedGroupId}/${mavenArtifactId}/maven-metadata.xml'"
	}
}
