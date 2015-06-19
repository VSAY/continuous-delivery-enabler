package com.liquidhub.framework.ci.model

enum GeneratedJobParameters {
	
	CONFIGURE_BRANCH_JOBS('configureBranchJobs'),
	FEATURE_NAME('featureName'),
	START_COMMIT('startCommit')
	
	
	GeneratedJobParameters(parameterName){
		this.parameterName = parameterName		
	} 
	
	def parameterName

}
