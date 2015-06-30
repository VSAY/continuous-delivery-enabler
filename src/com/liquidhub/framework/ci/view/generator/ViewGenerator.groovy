package com.liquidhub.framework.ci.view.generator

import com.liquidhub.framework.ci.model.JobGenerationContext

interface ViewGenerator {

	def generateView(JobGenerationContext context)
	
}
