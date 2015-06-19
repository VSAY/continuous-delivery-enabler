package com.liquidhub.framework.scm

import com.liquidhub.framework.ci.EmbeddedScriptProvider
import com.liquidhub.framework.scm.model.SCMRemoteRefListingRequest

class ShortBranchNamesListingScriptProvider implements EmbeddedScriptProvider {


	@Override
	public String getScript(Map bindings) {

		SCMRemoteRefListingRequest request = bindings['requestParam']

		request.with{
			bindVariablesInScript(targetUrl, authorizedUserDigest, branchFilterText)
		}
	}


	protected bindVariablesInScript(gitRepoUrl, credentialsHash, branchFilterText){
		
		if(!gitRepoUrl){
			throw new RuntimeException('gitRepoUrl must be specified while configuring the job, its required for listing the remote git references')
		}

		if(!credentialsHash){
			throw new RuntimeException('credentialsHash must be specified while configuring the job, its required for connecting to git for listing remote references')
		}
	
		if(branchFilterText){
			branchFilterText = "'"+branchFilterText+"'"
		}

		"""
           |import com.ibx.frontoffice.stash.utils.GitRepositoryExplorer
           |def explorer = new GitRepositoryExplorer('${credentialsHash}')
           |explorer.listShortBranchNames('${gitRepoUrl}',${branchFilterText}).collect{it.substring(${branchFilterText}.length())}.join(',')
 
        """.stripMargin()
	}
	
}
