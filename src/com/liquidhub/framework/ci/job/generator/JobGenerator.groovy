package com.liquidhub.framework.ci.job.generator;

import com.liquidhub.framework.ci.JobGeneratorFactory
import com.liquidhub.framework.model.JobGenerationContext



public interface JobGenerator {
	
	def generateJob(JobGenerationContext ctx)
	
    boolean supportsGitflow()

}
