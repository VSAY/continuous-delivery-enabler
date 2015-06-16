package com.liquidhub.framework.scm.model

/**
 * Represents the branch types forumlated in git flow, the branches have no physical distinction within git except the context in which they are used
 * 
 * 
 * @author Rahul Mishra, Liquidhub
 *
 */
enum GitFlowBranchTypes {

	HOTFIX('hotfix/'),
	RELEASE('release/'),
	SUPPORT('support/'),
	FEATURE('feature/'),
	DEVELOP('develop'),
	MASTER('master')

	//The prefix conventionally attached to the branch type
	final String prefix

	public GitFlowBranchTypes(String prefix){
		this.prefix = prefix
	}
	
	
	
	public boolean requiresRepositorySetup(){
		
		if(this == GitFlowBranchTypes.MASTER){
			return true
		}
		
		return false
	}


	public static GitFlowBranchTypes type(branchName){
		GitFlowBranchTypes.values().findResult{branchName.toLowerCase().startsWith(it.prefix) ? it : null}

	}


}
