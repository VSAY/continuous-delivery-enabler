package com.liquidhub.framework.ci.model

import com.liquidhub.framework.ci.EmbeddedScriptProvider
import com.liquidhub.framework.providers.maven.NexusRepositoryArtifactVersionListingScriptProvider

enum MavenArtifactRepositoryRegistry {

	NEXUS(new NexusRepositoryArtifactVersionListingScriptProvider())
	
	public MavenArtifactRepositoryRegistry(EmbeddedScriptProvider instance){
		this.instance = instance
	}
	
	EmbeddedScriptProvider instance

}
