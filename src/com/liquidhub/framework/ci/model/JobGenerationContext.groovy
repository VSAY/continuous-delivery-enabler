package com.liquidhub.framework.ci.model

import com.liquidhub.framework.JobSectionConfigurer
import com.liquidhub.framework.ci.BuildEnvironmentAwareTemplateEngine
import com.liquidhub.framework.ci.JobFactory
import com.liquidhub.framework.ci.JobNameBuilder
import com.liquidhub.framework.ci.logger.Logger
import com.liquidhub.framework.ci.view.JobViewFactory
import com.liquidhub.framework.ci.view.JobViewSupport
import com.liquidhub.framework.config.JobGenerationWorkspaceUtils
import com.liquidhub.framework.config.model.BuildConfig
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.model.MavenArtifact
import com.liquidhub.framework.providers.maven.MavenArtifactMetadataInfoProvider
import com.liquidhub.framework.scm.RepositoryAPIClientFactory
import com.liquidhub.framework.scm.model.SCMRepository

/**
 * Represents the context in which jobs are generated, by default most properties in the context are immutable after it has been created
 * 
 * A context is unique for every job generation run. 
 * 
 * @author Rahul Mishra,LiquidHub
 *
 */
class JobGenerationContext {

	final JobFactory jobFactory
	
	final JobViewFactory viewFactory

	final JobGenerationWorkspaceUtils workspaceUtils

	final Configuration configuration

	private static Logger logger

	final SCMRepository scmRepository

	final def repositoryName, defaultRegularEmailRecipients,defaultEscalationEmailRecipients,repositoryBranchName, jobSeederName, scmCredentialsId,scmRepositoryConfigurationInstructions
	
	final boolean generatingOnWindows

	final BuildEnvironmentAwareTemplateEngine templateEngine

	final BuildConfig buildToolConfig

	final JobViewSupport viewHelper
	
	final def scmAPIClient
	
	final MavenArtifact deployable,configurationArtifact
	
	final JobNameBuilder jobNameCreator = new JobNameBuilder()
	
	JobGenerationContext(JobFactory jobFactory, JobViewFactory viewFactory, JobGenerationWorkspaceUtils workspaceUtils,Configuration configuration,SCMRepository scmRepository, JobViewSupport jobViewSupport){

		this.workspaceUtils = workspaceUtils
		this.jobFactory = jobFactory
		this.viewFactory = viewFactory
		this.configuration = configuration
		this.scmRepository = scmRepository
		this.viewHelper = jobViewSupport

		//Some additional common properties for easy access.
		this.repositoryName = scmRepository?.repositorySlug
		this.repositoryBranchName = scmRepository?.repoBranchName
		this.defaultEscalationEmailRecipients = scmRepository.repositorySlug+configuration.notificationConfig.defaultEscalationEmailSuffix
		this.defaultRegularEmailRecipients = scmRepository.repositorySlug+configuration.notificationConfig.defaultRecipientEmailSuffix
		this.buildToolConfig = configuration.buildConfig
		this.templateEngine = new BuildEnvironmentAwareTemplateEngine(this)
		this.jobSeederName = configuration.jobSeederName
		this.generatingOnWindows = workspaceUtils.isRunningOnWindows()
		this.scmCredentialsId = buildToolConfig.scm?.credentials
		this.scmRepositoryConfigurationInstructions=configuration.scmRepositoryConfigurationInstructions
		this.scmAPIClient = RepositoryAPIClientFactory.getInstance(this)
		
		MavenArtifactMetadataInfoProvider deploymentInfoProvider = new MavenArtifactMetadataInfoProvider(this)
		this.deployable = deploymentInfoProvider.deployable
		this.configurationArtifact = deploymentInfoProvider.configurationArtifact
		
	}


	public def generateJob(name, jobConfig){
		jobFactory.job(name, jobConfig)
	}
	
	public def generateView(name, type, Closure viewConfig){
		viewFactory.view(name, type, viewConfig)
	}


	public JobSectionConfigurer configurers(providerKey){
		configuration.configurableJobSections.provider(providerKey)
	}

	
	JobSectionConfigurer mavenBuildStepConfigurer(){
		configuration.configurableJobSections.provider('maven')
	}


	def getVariable(SeedJobParameters jobParameter){
		configuration.buildEnvProperties[jobParameter.bindingName]
	}
	

	public boolean hasDeployable(){
		deployable != null
	}	
}
