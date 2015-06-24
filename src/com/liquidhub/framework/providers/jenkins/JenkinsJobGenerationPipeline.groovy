package com.liquidhub.framework.providers.jenkins

import com.liquidhub.framework.ci.JobFactory
import com.liquidhub.framework.ci.logger.Logger
import com.liquidhub.framework.ci.logger.PrintStreamLogger
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.JobGeneratorPipelineFilter
import com.liquidhub.framework.ci.model.SeedJobParameters
import com.liquidhub.framework.ci.view.JobViewSupport
import com.liquidhub.framework.config.ConfigurationManager
import com.liquidhub.framework.config.JobGenerationWorkspaceUtils
import com.liquidhub.framework.config.impl.YAMLConfigurationManager
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.providers.stash.GitRepository
import com.liquidhub.framework.scm.model.SCMRepository
/**
 * Assembles all implementation specific classes into a job generation pipeline. We expect to create one for each CI tool that we support
 * 
 * @author Rahul Mishra,LiquidHub
 *
 */
class JenkinsJobGenerationPipeline {

	def fabricate(Binding binding, dslFactory){

		Logger logger = new PrintStreamLogger(binding[SeedJobParameters.LOGGER_OUTPUT_STREAM.bindingName])
		
		JobFactory factory = new JenkinsJobFactory(dslFactory) //This is the job generator factory
		
		logger.info 'Job factory initialized '
		
		JobGenerationWorkspaceUtils workspaceUtils = new JenkinsWorkspaceUtils(dslFactory) //A utility which knows how to read files from a workspace
		
		logger.info 'Workspace utilities initialized '

		YAMLConfigurationManager.logger = logger

		def bindingVariables = binding.getVariables() //A list of variables which were bound from the seed job to this framework

		ConfigurationManager configurationManager = new YAMLConfigurationManager(workspaceUtils: workspaceUtils, buildEnvVars : bindingVariables)

		logger.info 'Analyzing scm repository information'

		SCMRepository scmRepository = new GitRepository(bindingVariables)
		
		logger.info 'Analyzed git repository information'

		Configuration configuration =  configurationManager.loadConfigurationForRepositoryBranch(scmRepository.getProjectKey(), scmRepository.getRepoBranchName())
		
		logger.info 'Finished loading configuration '

		JobGenerationContext.logger = logger
		
		JobViewSupport viewSupport = new JenkinsJobViewSupport()

		JobGenerationContext ctx = new JobGenerationContext(factory, workspaceUtils,configuration, scmRepository, viewSupport)
		
		logger.info 'Created job generation context'

		JobGeneratorPipelineFilter jobGeneratorFilter = new JobGeneratorPipelineFilter(ctx)
		
		logger.info 'JobGeneratorPipelineFilter created successfully'
		
		logger.info 'Launching job generators'

		jobGeneratorFilter.configuredJobGenerators*.generateJob(ctx)
		
		logger.info 'Job configuration generated successfully'
	}
}
