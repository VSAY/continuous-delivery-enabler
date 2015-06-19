package com.liquidhub.framework.scm

import com.liquidhub.framework.ci.EmbeddedScriptProvider
import com.liquidhub.framework.ci.model.VersionDeterminationRequest

abstract class VersionDeterminationScriptProvider implements EmbeddedScriptProvider {


	@Override
	public String getScript(Map bindings) {
		VersionDeterminationRequest request = bindings['requestParam']

		def gitRepoUrl = request.gitRepoUrl, authorizedUserDigest = request.authorizedUserDigest,versionNamingStrategy = request.versionNamingStrategy

		if(!gitRepoUrl){
			throw new RuntimeException('gitRepoUrl must be specified while configuring the job, its required for listing the remote git references')
		}

		if(!authorizedUserDigest){
			throw new RuntimeException('authorizedUserDigest must be specified while configuring the job, its required for connecting to git for listing remote references')
		}

		if(versionNamingStrategy){
			versionNamingStrategy = "'"+versionNamingStrategy+"'"
		}

		getVersionChoicesScript(gitRepoUrl, authorizedUserDigest, versionNamingStrategy)
	}


	abstract getVersionChoicesScript(gitRepoUrl, authorizedUserDigest, versionNamingStrategy)
}
