package com.liquidhub.framework.ci.job.generator.impl


import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.ALLOW_SNAPSHOTS_WHILE_FINISHING_RELEASE
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.KEEP_RELEASE_BRANCH
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.RELEASE_BRANCH
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.RELEASE_TAG_MESSAGE
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.SKIP_RELEASE_BRANCH_MERGE
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.SKIP_RELEASE_TAGGING
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.SQUASH_COMMITS
import static com.liquidhub.framework.ci.view.ViewElementTypes.BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_TEXT
import static com.liquidhub.framework.ci.view.ViewElementTypes.TEXT
import static com.liquidhub.framework.ci.model.JobPermissions.ItemBuild
import static com.liquidhub.framework.ci.model.JobPermissions.ItemCancel
import static com.liquidhub.framework.ci.model.JobPermissions.ItemConfigure
import static com.liquidhub.framework.ci.model.JobPermissions.ItemDiscover
import static com.liquidhub.framework.ci.model.JobPermissions.ItemRead
import static com.liquidhub.framework.ci.model.JobPermissions.ItemWorkspace
import static com.liquidhub.framework.ci.model.JobPermissions.RunDelete
import static com.liquidhub.framework.ci.model.JobPermissions.RunUpdate

import static com.liquidhub.framework.providers.jenkins.OperatingSystemCommandAdapter.adapt

import java.util.Map;

import com.liquidhub.framework.ci.EmbeddedScriptProvider
import com.liquidhub.framework.ci.model.GitflowJobParameter
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.ParameterListingScript
import com.liquidhub.framework.ci.view.ViewElementTypes
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.config.model.RoleConfig;
import com.liquidhub.framework.scm.DevelopmentMilestoneVersionScriptProvider
import com.liquidhub.framework.scm.MilestoneReleaseVersionScriptProvider
import com.liquidhub.framework.scm.model.GitFlowBranchTypes
import com.liquidhub.framework.scm.model.SCMRemoteRefListingRequest
import com.liquidhub.framework.scm.model.SCMRepository



class GitflowFinishReleaseJobGenerator extends BaseGitflowJobGenerationTemplateSupport {



	private EmbeddedScriptProvider releaseOptionsProvider = new MilestoneReleaseVersionScriptProvider()
	private EmbeddedScriptProvider nextMilestoneChoiceProvider = new DevelopmentMilestoneVersionScriptProvider()


	def getJobConfig(Configuration configuration){
		configuration.gitflowReleaseBranchConfig.finishConfig
	}


	protected def defineJobParameters(JobGenerationContext context, JobConfig jobConfig){

		def parameters = []

		SCMRepository repository = context.scmRepository

		def repoUrl = repository.repoUrl, authDigest = repository.authorizedUserDigest

		SCMRemoteRefListingRequest request = new SCMRemoteRefListingRequest(
				targetUrl: repoUrl,
				authorizedUserDigest: authDigest,
				branchFilterText:  'release/'
				)

		request.listFullRefNames = true
		def descriptionScript = branchNamesListingProvider.getScript(['requestParam':request])

		request.listFullRefNames = false
		def valueScript = branchNamesListingProvider.getScript(['requestParam':request])

		parameters << new GitflowJobParameter(
				name: RELEASE_BRANCH,
				description : 'The release you intend to finish',
				valueListingScript: new ParameterListingScript(text:valueScript),
				labelListingScript: new ParameterListingScript(text:descriptionScript),
				elementType: ViewElementTypes.SINGLE_SELECT_CHOICES
				)

		parameters << new GitflowJobParameter(name: ALLOW_SNAPSHOTS_WHILE_FINISHING_RELEASE, elementType: READ_ONLY_BOOLEAN_CHOICE, defaultValue:false)
		parameters << new GitflowJobParameter(name: KEEP_RELEASE_BRANCH, elementType: BOOLEAN_CHOICE, defaultValue:true)
		parameters << new GitflowJobParameter(name: SQUASH_COMMITS, elementType: BOOLEAN_CHOICE, defaultValue:false)
	}



	@Override
	def configureBuildSteps(JobGenerationContext ctx, JobConfig jobConfig){
		
		def mvnConfigurer = ctx.configurers('maven')
		
		def configureMavenCommand = {goals ->
			
						def config = goals instanceof JobConfig ? goals :  ['goals': goals, 'activeMavenProfiles':jobConfig?.activeMavenProfiles] as JobConfig
			
						mvnConfigurer.configure(ctx, config)
					}


		return{

			ctx.generatingOnWindows ? batchFile(adapt(CHECKOUT_RELEASE_BRANCH)) : shell(CHECKOUT_RELEASE_BRANCH)
			maven configureMavenCommand(UPDATE_RELEASE_BRANCH_VERSION)
			ctx.generatingOnWindows ? batchFile(adapt(COMMIT_ALL_FILES)) : shell(COMMIT_ALL_FILES)
			maven configureMavenCommand(jobConfig)
			ctx.generatingOnWindows ? batchFile(adapt(WRITE_MASTER_MERGE_COMMIT_CODE_TO_FILE)) : shell(WRITE_MASTER_MERGE_COMMIT_CODE_TO_FILE)
			ctx.generatingOnWindows ? batchFile(adapt(WRITE_DEVELOP_MERGE_COMMIT_CODE_TO_FILE)) : shell(WRITE_DEVELOP_MERGE_COMMIT_CODE_TO_FILE)
			environmentVariables{
				propertiesFile('finishrelease_env_properties') //This is the file we create in the previous step
				envs(['SCM_CHANGESET_URL': ctx.scmRepository.changeSetUrl])
			}
		}
	}

	@Override
	protected def determineRegularEmailSubject(JobGenerationContext ctx, JobConfig jobConfig){
		'release/${ENV, var="releaseBranch"}  branch on '+ctx.repositoryName+' has merged into master and develop'
	}

	@Override
	protected def determineFailureEmailSubject(JobGenerationContext ctx, JobConfig jobConfig){
		'Action Required !!! Failed to finish release branch ${ENV, var="releaseBranch"} on '+ctx.repositoryName
	}
	
	/**
	 * @return the name of the branch which should be used to build the source code
	 */
	protected def identifySCMBranchForBuild(JobGenerationContext ctx){
		'release/${releaseBranch}'
	}
	
	
	@Override
	protected def configureAdditionalPublishers(JobGenerationContext ctx, JobConfig jobConfig){

		//When release finishes and the code is merged to master, we trigger the master branch health job automatically
		def downstreamMasterHealthJobName = ctx.jobNameCreator.createJobName(ctx.repositoryName, GitFlowBranchTypes.MASTER, 'master', ctx.configuration.healthConfig)
		
		//When release finishes and the master is merged to develop, we trigger the develop ci  job automatically
		def downstreamDevelopCIJobName = ctx.jobNameCreator.createJobName(ctx.repositoryName, GitFlowBranchTypes.DEVELOP, 'develop', ctx.configuration.continuousIntegrationConfig)
		
		final boolean triggerWithNoParameters = true
		
		return {
			downstreamParameterized {
				trigger(downstreamMasterHealthJobName, 'SUCCESS', triggerWithNoParameters)
				trigger(downstreamDevelopCIJobName, 'SUCCESS', triggerWithNoParameters)
			}
		}

	}

	@Override
	protected Map grantAdditionalPermissions(JobGenerationContext ctx,RoleConfig roleConfig){
		def parameters = [:]
		parameters.put(roleConfig.releaseManagerRole, [ItemBuild, ItemCancel, ItemDiscover, ItemRead, RunUpdate, RunDelete, ItemWorkspace])
		return parameters
	}


	static final def CHECKOUT_RELEASE_BRANCH = 'git checkout release/${releaseBranch}'
	
	static final def UPDATE_RELEASE_BRANCH_VERSION = 'versions:set -DnewVersion=${releaseBranch} versions:commit'

	static final def COMMIT_ALL_FILES = 'git commit --all -m "Updated Maven POM versions for release"'
	
	/*
	 * Release branch merges to master first, so the first merge commit in history is the merge of MASTER and RELEASE branch, so we skip nothing and request the latest merge commit
	 */
	private static final String WRITE_MASTER_MERGE_COMMIT_CODE_TO_FILE = 'echo MASTER_MERGE_COMMIT=$(echo `git log ---max-count=1 --skip=0 --merges --pretty=format:%h`) > finishrelease_env_properties'
	
	/*
	 * Master branch merges to develop next, so the next merge commit in history is the merge of MASTER and DEVELOP branch, so we skip 'one' and request the second last merge commit
	 */
	private static final String WRITE_DEVELOP_MERGE_COMMIT_CODE_TO_FILE = 'echo DEVELOP_MERGE_COMMIT=$(echo `git log --max-count=1 --skip=1 --merges --pretty=format:%h`) >> finishrelease_env_properties'
}
