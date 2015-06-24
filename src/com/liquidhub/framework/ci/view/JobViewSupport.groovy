package com.liquidhub.framework.ci.view

import com.liquidhub.framework.ci.model.GitflowJobParameter


interface JobViewSupport {
	
	def defineParameter(GitflowJobParameter parameterDefinition)

}
