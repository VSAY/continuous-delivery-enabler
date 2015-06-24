package com.liquidhub.framework.ci.job.generator.impl

import com.liquidhub.framework.ci.model.GitflowJobParameter
import com.liquidhub.framework.ci.model.GitflowJobParameters
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.ParameterListingScript
import com.liquidhub.framework.ci.view.ViewElementTypes
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.scm.model.SCMRepository

import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.ALLOW_SNAPSHOTS_WHILE_CREATING_HOTFIX
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.PUSH_HOTFIXES
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.RELEASE_VERSION
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.START_COMMIT

import static com.liquidhub.framework.providers.jenkins.OperatingSystemCommandAdapter.adapt

class GitflowStartHotfixJobGenerator extends BaseGitflowJobGenerationTemplateSupport{
	
	
	@Override
	def getJobConfig(Configuration configuration){
		configuration.gitflowHotfixBranchConfig.startConfig
	}

	protected def defineJobParameters(JobGenerationContext context, JobConfig jobConfig){

		GitflowJobParameters parameters = new GitflowJobParameters()

		parameters.newTextField(name:START_COMMIT)
		parameters.newTextField(name:RELEASE_VERSION)
		parameters.newTextField(name:ALLOW_SNAPSHOTS_WHILE_CREATING_HOTFIX, defaultValue: 'develop', editable:false)
		parameters.newBooleanParam(name:PUSH_HOTFIXES, defaultValue: true, editable:false)


	}


	def configureBuildSteps(JobGenerationContext ctx, JobConfig jobConfig){

		return {
			ctx.generatingOnWindows ? batchFile(CHECK_OUT_DEVELOP) : shell(CHECK_OUT_DEVELOP)
			maven ctx.mavenBuildStepConfigurer().configure(ctx, jobConfig)

		}
	}
	
	protected boolean configuresBranchInitiatingJob() {
		true
	}
	
	protected def preparePropertiesForDownstreamJobLaunch(JobGenerationContext context){
		[gitRepoUrl: context.scmRepository.repoUrl, repoBranchName: 'hotfix/${releaseVersion}']
	}
	
	private static final String CHECK_OUT_DEVELOP = 'git checkout develop'
}
