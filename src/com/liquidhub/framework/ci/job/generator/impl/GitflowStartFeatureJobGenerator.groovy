package com.liquidhub.framework.ci.job.generator.impl

import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.ALLOW_SNAPSHOTS_WHILE_CREATING_FEATURE
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.ENABLE_FEATURE_VERSIONS
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.FEATURE_NAME
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.PUSH_FEATURES
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.START_COMMIT
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.FEATURE_OWNER_EMAIL
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.FEATURE_PRODUCTION_DATE


import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.TEXT


import com.liquidhub.framework.ci.model.BuildEnvironmentVariables
import com.liquidhub.framework.ci.model.GitflowJobParameter
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.ParameterListingScript
import com.liquidhub.framework.ci.view.ViewElementTypes
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig

import com.liquidhub.framework.scm.model.SCMRepository

import static com.liquidhub.framework.providers.jenkins.OperatingSystemCommandAdapter.adapt

class GitflowStartFeatureJobGenerator extends BaseGitflowJobGenerationTemplateSupport {


	def getJobConfig(Configuration configuration){
		return configuration.gitflowFeatureBranchConfig.startConfig
	}

	protected def defineJobParameters(JobGenerationContext context, JobConfig jobConfig){

		def parameters = []

		SCMRepository scmRepository = context.scmRepository

		def featureParamDescription = 'The name of the feature you intend to start.Please do not prefix feature/  .It is done automatically'

		parameters << new GitflowJobParameter(name: FEATURE_NAME, description: featureParamDescription, elementType: TEXT)
		parameters << new GitflowJobParameter(name: START_COMMIT,description: generateCommitDescription(scmRepository.changeSetUrl),defaultValue: "'develop'", elementType: TEXT)
		parameters << new GitflowJobParameter(name: FEATURE_OWNER_EMAIL,elementType: TEXT)
		parameters << new GitflowJobParameter(name: FEATURE_PRODUCTION_DATE,elementType: TEXT)
		
		parameters << [ALLOW_SNAPSHOTS_WHILE_CREATING_FEATURE, ENABLE_FEATURE_VERSIONS, PUSH_FEATURES].collect{
			new GitflowJobParameter(name: it, elementType: READ_ONLY_BOOLEAN_CHOICE, defaultValue: true)
		}
	}



	@Override
	protected def determineRegularEmailSubject(JobGenerationContext ctx, JobConfig jobConfig){
		'New Feature branch [ ${ENV, var="featureName"} ] created on '+ctx.repositoryName+' repository'
	}

	@Override
	protected def determineFailureEmailSubject(JobGenerationContext ctx, JobConfig jobConfig){
		'Action Required !!! Failed to create feature branch ${ENV, var="featureName"}.'
	}
	
	/**
	 * @return the name of the branch which should be used to build the source code
	 */
	protected def identifySCMBranchForBuild(JobGenerationContext ctx){
		'develop'
	}


	def configureBuildSteps(JobGenerationContext ctx, JobConfig jobConfig){

		return {
			ctx.generatingOnWindows ? batchFile(adapt(CHECK_OUT_DEVELOP)) : shell(CHECK_OUT_DEVELOP)
			maven ctx.mavenBuildStepConfigurer().configure(ctx, jobConfig)
		}
	}


	protected def preparePropertiesForDownstreamJobLaunch(JobGenerationContext context){
		[gitRepoUrl: context.scmRepository.repoUrl, repoBranchName: 'feature/${featureName}']
	}

	protected def generateCommitDescription(gitRepoChangeSetUrl){
		"""
           |Your branch diverges/begins at this point. Enter a commit(SHA/short SHA) on the develop branch or enter the branch name 'develop' 
		   |(without the quotes) to branch off the latest development copy. You can copy the desired short SHA (from the commits column)
		   |at <a href='${gitRepoChangeSetUrl}/commits' target='_blank'>List of Commits on develop branch </a>.Avoid typing the commit, prefer copy and paste.
		""".stripMargin()
	}

	protected boolean configuresBranchInitiatingJob() {
		true
	}


	final String CHECK_OUT_DEVELOP = 'git checkout develop'
}

