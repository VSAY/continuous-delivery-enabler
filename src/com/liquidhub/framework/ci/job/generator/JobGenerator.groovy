package com.liquidhub.framework.ci.job.generator;

import com.liquidhub.framework.ci.model.JobGenerationContext


/**
 * A base expectation from a job generator. All job implementations MUST be stateless
 * 
 * @author Rahul Mishra,LiquidHub
 *
 */

public interface JobGenerator {
	
	def generateJob(JobGenerationContext ctx)
	
    boolean supportsGitflow()

}
