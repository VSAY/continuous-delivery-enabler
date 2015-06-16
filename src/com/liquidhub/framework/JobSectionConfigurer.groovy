package com.liquidhub.framework

import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.model.JobGenerationContext

/**
 * The base contractual expectation from a configuration section provider. All provider implementations MUST be stateless
 * 
 */
interface JobSectionConfigurer {

	Closure configure(JobGenerationContext context, JobConfig jobConfig)
}
