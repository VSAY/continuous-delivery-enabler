package com.liquidhub.framework.ci.view

import com.liquidhub.framework.ci.model.JobParameter


interface JobViewSupport {
	
	def defineParameter(parameterDefinition)
	
	def findPreConfiguredRepositoryNamesInProjectView(projectName)

}
