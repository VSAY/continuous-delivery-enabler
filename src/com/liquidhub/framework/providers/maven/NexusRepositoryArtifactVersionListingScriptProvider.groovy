package com.liquidhub.framework.providers.maven

import java.util.Map

import com.liquidhub.framework.ci.EmbeddedScriptProvider;

class NexusRepositoryArtifactVersionListingScriptProvider implements EmbeddedScriptProvider {


	@Override
	public String getScript(Map bindings) {
		
		def baseRepositoryUrl = bindings['baseRepositoryUrl'],mavenGroupId=bindings['groupId'], mavenArtifactId = bindings['artifactId']
		def snapshotVersionCountToDisplay=bindings['snapshotVersionCountToDisplay'],releaseVersionCountToDisplay=bindings['releaseVersionCountToDisplay']
		def applyFeatureVersionExclusionFilter=bindings['applyFeatureVersionExclusionFilter']
		

		"""
        | import com.liquidhub.framework.maven.MavenRepositoryExplorer
        | def snapshotVersions = MavenRepositoryExplorer.listRecentSnapshotVersions($baseRepositoryUrl, $mavenGroupId, $mavenArtifactId,$snapshotVersionCountToDisplay,$applyFeatureVersionExclusionFilter)
		| def releaseVersions = MavenRepositoryExplorer.listRecentReleaseVersions($baseRepositoryUrl, $mavenGroupId, $mavenArtifactId,$releaseVersionCountToDisplay)
		| def allVersions = [] << releaseVersions << snapshotVersions
		| allVersions.flatten()
	    """.stripMargin()
	}
	
}
