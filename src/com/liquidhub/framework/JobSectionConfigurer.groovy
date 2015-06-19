package com.liquidhub.framework

import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.config.model.JobConfig

/**
 * The base contractual expectation from a configuration section provider. All provider implementations MUST be stateless
 * 
 */
interface JobSectionConfigurer {

	Closure configure(JobGenerationContext context, JobConfig jobConfig)
}
