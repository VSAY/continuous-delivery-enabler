package com.liquidhub.framework.ci.job.generator.impl

import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.DEVELOPMENT_VERSION
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.ALLOW_SNAPSHOTS_WHILE_FINISHING_HOTFIX
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.DEVELOPMENT_VERSION
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.HOTFIX_BRANCH
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.HOTFIX_VERSION
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.KEEP_HOTFIX_BRANCH
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.NO_DEPLOY
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.SKIP_HOTFIX_TAGGING
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.SQUASH_COMMITS
import static com.liquidhub.framework.ci.view.ViewElementTypes.BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.TEXT
import static com.liquidhub.framework.providers.jenkins.OperatingSystemCommandAdapter.adapt

import com.liquidhub.framework.ci.model.GitflowJobParameter
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.ParameterListingScript
import com.liquidhub.framework.ci.view.ViewElementTypes
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.scm.model.GitFlowBranchTypes
import com.liquidhub.framework.scm.model.SCMRemoteRefListingRequest
import com.liquidhub.framework.scm.model.SCMRepository

class GitflowFinishHotfixJobGenerator extends BaseGitflowJobGenerationTemplateSupport{

	@Override
	def getJobConfig(Configuration configuration){
		configuration.gitflowHotfixBranchConfig.finishConfig
	}

     @Override
	def defineJobParameters(JobGenerationContext context, JobConfig jobConfig){

		def parameters = []

		SCMRepository repository = context.scmRepository

		def repoUrl = repository.repoUrl, authDigest = repository.authorizedUserDigest

		SCMRemoteRefListingRequest request = new SCMRemoteRefListingRequest(targetUrl: repoUrl, authorizedUserDigest: authDigest, branchFilterText:  'hotfix/')

		request.listFullRefNames=true
		def descriptionScript = branchNamesListingProvider.getScript(['requestParam':request])

		request.listFullRefNames=false
		def valueScript = branchNamesListingProvider.getScript(['requestParam':request])

		parameters << new GitflowJobParameter(name: HOTFIX_BRANCH, 
			              valueListingScript: new ParameterListingScript(text:valueScript), 
						  labelListingScript: new ParameterListingScript(text:descriptionScript),
						  elementType: ViewElementTypes.SINGLE_SELECT_CHOICES)

		parameters << new GitflowJobParameter(name: DEVELOPMENT_VERSION,description : 'What do you want the next development version to be?', elementType:TEXT)
		parameters << new GitflowJobParameter(name: KEEP_HOTFIX_BRANCH,elementType:BOOLEAN_CHOICE, defaultValue:false)
		parameters << new GitflowJobParameter(name: SQUASH_COMMITS, elementType:BOOLEAN_CHOICE, defaultValue:false)
			
		parameters << [ALLOW_SNAPSHOTS_WHILE_FINISHING_HOTFIX, SKIP_HOTFIX_TAGGING].collect{
			new GitflowJobParameter(name: it, elementType: READ_ONLY_BOOLEAN_CHOICE, defaultValue: false)
		}
	}

	def configureBuildSteps(JobGenerationContext context, JobConfig jobConfig){

		return{
			context.generatingOnWindows ? batchFile(adapt(CHECK_OUT_HOTFIX)) : shell(CHECK_OUT_HOTFIX)
			maven context.configurers('maven').configure(context, jobConfig)
		}
	}
	
	@Override
	protected def determineRegularEmailSubject(JobGenerationContext ctx, JobConfig jobConfig){
		'Closed Hotfix branch ${ENV, var="releaseVersion"}  on '+ctx.repositoryName
	}

	@Override
	protected def determineFailureEmailSubject(JobGenerationContext ctx, JobConfig jobConfig){
		'Action Required !!! Failed to finish hotfix branch ${ENV, var="releaseVersion"} on '+ctx.repositoryName
	}
	
	
	@Override
	protected def configureAdditionalPublishers(JobGenerationContext ctx, JobConfig jobConfig){

		//When release finishes and the code is merged to master, we trigger the master branch health job automatically
		def downstreamMasterHealthJobName = ctx.jobNameCreator.createJobName(ctx.repositoryName, GitFlowBranchTypes.MASTER, 'master', ctx.configuration.healthConfig)
				
		return {
			downstreamParameterized {
				trigger(downstreamMasterHealthJobName, 'SUCCESS')
			
			}
		}

	}
	
	private static final def CHECK_OUT_HOTFIX = 'git checkout hotfix/${hotfixBranch}'
}
