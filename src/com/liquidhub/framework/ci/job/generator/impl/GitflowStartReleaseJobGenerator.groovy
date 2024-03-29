package com.liquidhub.framework.ci.job.generator.impl

import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.ALLOW_SNAPSHOTS_WHILE_CREATING_RELEASE
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.DEVELOPMENT_VERSION
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.PUSH_RELEASES
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.RELEASE_BRANCH_VERSION_SUFFIX
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.RELEASE_DATE
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.RELEASE_VERSION
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.START_COMMIT
import static com.liquidhub.framework.ci.model.JobPermissions.ItemBuild
import static com.liquidhub.framework.ci.model.JobPermissions.ItemCancel
import static com.liquidhub.framework.ci.model.JobPermissions.ItemConfigure
import static com.liquidhub.framework.ci.model.JobPermissions.ItemDiscover
import static com.liquidhub.framework.ci.model.JobPermissions.ItemRead
import static com.liquidhub.framework.ci.model.JobPermissions.ItemWorkspace
import static com.liquidhub.framework.ci.model.JobPermissions.RunDelete
import static com.liquidhub.framework.ci.model.JobPermissions.RunUpdate
import static com.liquidhub.framework.ci.view.ViewElementTypes.BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_TEXT
import static com.liquidhub.framework.ci.view.ViewElementTypes.TEXT
import static com.liquidhub.framework.providers.jenkins.OperatingSystemCommandAdapter.adapt

import com.liquidhub.framework.ci.model.GitflowJobParameter
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.view.ViewElementTypes
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.config.model.RoleConfig
import com.liquidhub.framework.scm.model.GitFlowBranchTypes


class GitflowStartReleaseJobGenerator extends BaseGitflowJobGenerationTemplateSupport {

	private static final String MILESTONE_NAMING_STRATEGY='m'

	def getJobConfig(Configuration configuration){
		configuration.gitflowReleaseBranchConfig.startConfig
	}


	protected def defineJobParameters(JobGenerationContext context, JobConfig jobConfig){

		def parameters = []

		parameters << new GitflowJobParameter(
				name: START_COMMIT,
				description: 'This is the starting point of the release branch.For now, this value cannot be edited',
				defaultValue: "'develop'",
				elementType:READ_ONLY_TEXT
				)

		parameters << new GitflowJobParameter(
				name: RELEASE_VERSION,
				description: 'This is version which will be assigned to your final release artifact, this will also be the name of your release branch. Follow the maven versioning scheme',
				elementType: TEXT
				)

		parameters << new GitflowJobParameter(
				name: DEVELOPMENT_VERSION,
				description: 'This is version which will be assigned to your develop branch, so if your release version is 0.1.14, this version SHOULD be 0.1.15-SNAPSHOT to avoid version clashes across branches',
				elementType: TEXT
				)


		parameters << new GitflowJobParameter(name: RELEASE_BRANCH_VERSION_SUFFIX, elementType: TEXT)
		parameters << new GitflowJobParameter(name: RELEASE_DATE,elementType: TEXT)
		
		parameters << [ALLOW_SNAPSHOTS_WHILE_CREATING_RELEASE, PUSH_RELEASES].collect {
			new GitflowJobParameter(name: it, elementType: READ_ONLY_BOOLEAN_CHOICE, defaultValue:true)
		}
	}


	def configureBuildSteps(JobGenerationContext ctx,JobConfig jobConfig){
		return{
			ctx.generatingOnWindows ? batchFile(adapt(CHECK_OUT_DEVELOP)) : shell(CHECK_OUT_DEVELOP)
			maven ctx.configurers('maven').configure(ctx, jobConfig)
		}
	}

	protected def preparePropertiesForDownstreamJobLaunch(JobGenerationContext context){
		[gitRepoUrl: context.scmRepository.repoUrl, repoBranchName: 'release/${releaseVersion}']
	}
	
	/**
	 * @return the name of the branch which should be used to build the source code
	 */
	protected def identifySCMBranchForBuild(JobGenerationContext ctx){
		'develop'
	}
	
	
	@Override
	protected Map grantAdditionalPermissions(JobGenerationContext ctx,RoleConfig roleConfig){
		def parameters = [:]
		parameters.put(roleConfig.releaseManagerRole, [ItemBuild, ItemCancel, ItemDiscover, ItemRead, RunUpdate, RunDelete, ItemWorkspace])
		return parameters
	}
	
	@Override
	protected def configureAdditionalPublishers(JobGenerationContext ctx, JobConfig jobConfig){

		//When feature finishes and the code is merged to develop, we trigger the develop branch ci job automatically
		def downstreamReleaseCIJobName = ctx.jobNameCreator.createJobName(ctx.repositoryName, GitFlowBranchTypes.RELEASE, 'release', ctx.configuration.continuousIntegrationConfig)

		return {
			buildPipelineTrigger(downstreamReleaseCIJobName)
		}

	}


	@Override
	protected def determineRegularEmailSubject(JobGenerationContext ctx, JobConfig jobConfig){
		'release/${ENV, var="releaseVersion"} branch for '+ctx.repositoryName+' repository is now open'
	}

	@Override
	protected def determineFailureEmailSubject(JobGenerationContext ctx, JobConfig jobConfig){
		'Action Required !!! Failed to open release branch release/${ENV, var="releaseVersion"} for '+ctx.repositoryName+' repository'
	}

	protected boolean configuresBranchInitiatingJob(){
		true
	}

	final String CHECK_OUT_DEVELOP = 'git checkout develop'
}
