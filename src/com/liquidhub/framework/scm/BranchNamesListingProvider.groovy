package com.liquidhub.framework.scm

import com.liquidhub.framework.ci.EmbeddedScriptProvider
import com.liquidhub.framework.scm.model.SCMRemoteRefListingRequest

class BranchNamesListingProvider implements EmbeddedScriptProvider {


	@Override
	public String getScript(Map bindings) {

		SCMRemoteRefListingRequest request = bindings['requestParam']

		request.with{
			bindVariablesInScript(targetUrl, authorizedUserDigest, branchFilterText, listFullRefNames)
		}
	}


	protected bindVariablesInScript(gitRepoUrl, credentialsHash, branchFilterText, listFullRefNames){
		if(!gitRepoUrl){
			throw new RuntimeException('gitRepoUrl must be specified while configuring the job, its required for listing the remote git references')
		}

		if(!credentialsHash){
			throw new RuntimeException('credentialsHash must be specified while configuring the job, its required for connecting to git for listing remote references')
		}


		if(branchFilterText){
			branchFilterText = "'"+branchFilterText+"'"
		}


		if(listFullRefNames) {//If the branch filter text is 'feature/' the full name listing for a feature 'f1' is refs/heads/feature/f1
			"""
           |import com.liquidhub.framework.git.SCMRepositoryExplorer as explorer
		   |explorer.listFullBranchNames('${gitRepoUrl}', '${credentialsHash}', ${branchFilterText}).join(',')
        """.stripMargin()
		
		}else{ //If the branch filter text is 'feature/' the short name listing for a feature 'f1' is 'f1'
			"""
           |import com.liquidhub.framework.git.SCMRepositoryExplorer as explorer
		   |explorer.listShortBranchNames('${gitRepoUrl}', '${credentialsHash}', ${branchFilterText}).collect{it.substring(${branchFilterText}.length())}.join(',')
        """.stripMargin()
		}
	}


	public static void main(String[] args){
		BranchNamesListingProvider provider = new BranchNamesListingProvider()
		println provider.getScript(['requestParam':[targetUrl:'http://stash.ibx.com/scm/rca/roam-web.git',authorizedUserDigest: '', branchFilterText: 'release']])
	}
}
