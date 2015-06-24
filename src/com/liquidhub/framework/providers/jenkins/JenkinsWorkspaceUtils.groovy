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


	@Override
	public Object isRunningOnWindows() {
		windowsOS
	}
	
	private final boolean windowsOS = (System.getenv()['OS']?.toLowerCase() =~ /.*windows.*/).matches() //If the OS belongs to the windows family
	

}
