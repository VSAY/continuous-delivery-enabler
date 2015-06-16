package com.liquidhub.framework.providers.jenkins

import com.liquidhub.framework.config.JobGenerationWorkspaceUtils

class JenkinsWorkspaceUtils implements JobGenerationWorkspaceUtils {
	
	def fileReaderImplementation
	
	JenkinsWorkspaceUtils(dslFactory){
		this.fileReaderImplementation = dslFactory
	}
	

	@Override
	public fileReader(filePath) {
		fileReaderImplementation.readFileFromWorkspace(filePath)
	}

}
