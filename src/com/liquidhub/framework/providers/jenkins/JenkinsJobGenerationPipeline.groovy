package com.liquidhub.framework.providers.jenkins

import com.liquidhub.framework.ci.JobGeneratorFactory
import com.liquidhub.framework.ci.logger.Logger
import com.liquidhub.framework.ci.logger.PrintStreamLogger
import com.liquidhub.framework.config.ConfigurationManager
import com.liquidhub.framework.config.JobGenerationWorkspaceUtils
import com.liquidhub.framework.config.impl.YAMLConfigurationManager
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.model.JobGenerationContext
import com.liquidhub.framework.model.JobGeneratorPipelineFilter
import com.liquidhub.framework.model.SeedJobParameters
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

		JobGeneratorFactory factory = new JenkinsJobGeneratorFactory(dslFactory) //This is the job generator factory
		JobGenerationWorkspaceUtils workspaceUtils = new JenkinsWorkspaceUtils(dslFactory) //A utility which knows how to read files from a workspace

		YAMLConfigurationManager.logger = logger

		def bindingVariables = binding.getVariables() //A list of variables which were bound from the seed job to this framework

		ConfigurationManager configurationManager = new YAMLConfigurationManager(workspaceUtils: workspaceUtils, buildEnvVars : bindingVariables)

		SCMRepository scmRepository = new GitRepository(bindingVariables)

		Configuration configuration =  configurationManager.loadConfigurationForRepositoryBranch(scmRepository.getProjectKey(), scmRepository.getRepoBranchName())

		JobGenerationContext.logger = logger

		JobGenerationContext ctx = new JobGenerationContext(jobFactory: factory, workspaceUtils: workspaceUtils, scmRepository: scmRepository, configuration: configuration)

		JobGeneratorPipelineFilter jobGeneratorFilter = new JobGeneratorPipelineFilter(ctx)

		jobGeneratorFilter.configueredJobGenerators*.generateJob(ctx)
	}
}
