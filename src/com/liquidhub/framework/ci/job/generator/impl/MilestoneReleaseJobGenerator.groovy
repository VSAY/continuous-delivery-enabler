package com.liquidhub.framework.ci.job.generator.impl

import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.ALLOW_SNAPSHOTS_WHILE_FINISHING_RELEASE
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.KEEP_RELEASE_BRANCH
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.MILESTONE_RELEASE_VERSION
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.MILESTONE_TAG_MESSAGE
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.NEXT_MILESTONE_DEV_VERSION
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.RELEASE_BRANCH
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.RELEASE_TAG_MESSAGE
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.SKIP_RELEASE_TAGGING
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.SQUASH_COMMITS
import static com.liquidhub.framework.ci.view.ViewElementTypes.BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_TEXT
import static com.liquidhub.framework.ci.view.ViewElementTypes.TEXT
import static com.liquidhub.framework.providers.jenkins.OperatingSystemCommandAdapter.adapt

import com.liquidhub.framework.ci.model.BuildEnvironmentVariables
import com.liquidhub.framework.ci.model.GitflowJobParameter
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.ParameterListingScript
import com.liquidhub.framework.ci.model.VersionDeterminationRequest
import com.liquidhub.framework.ci.view.ViewElementTypes
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.scm.DevelopmentMilestoneVersionScriptProvider
import com.liquidhub.framework.scm.MilestoneReleaseVersionScriptProvider
import com.liquidhub.framework.scm.model.SCMRemoteRefListingRequest
import com.liquidhub.framework.scm.model.SCMRepository


class MilestoneReleaseJobGenerator extends BaseGitflowJobGenerationTemplateSupport {
	private static final String MILESTONE_NAMING_STRATEGY='m'

	private DevelopmentMilestoneVersionScriptProvider developmentMilestoneScriptProvider = new DevelopmentMilestoneVersionScriptProvider()
	private MilestoneReleaseVersionScriptProvider releaseMilestoneVersionScriptProvider = new MilestoneReleaseVersionScriptProvider()


	def getJobConfig(Configuration configuration){
		configuration.milestoneReleaseConfig
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

		def versionDeterminationRequest = new VersionDeterminationRequest(gitRepoUrl: repoUrl, authorizedUserDigest : authDigest, versionNamingStrategy: MILESTONE_NAMING_STRATEGY)

		def releaseChoicesScript = releaseMilestoneVersionScriptProvider.getScript(['requestParam':versionDeterminationRequest])

		parameters << new GitflowJobParameter(name: MILESTONE_RELEASE_VERSION, valueListingScript: new ParameterListingScript(text:releaseChoicesScript),elementType: READ_ONLY_TEXT)

		def developmentVersionDeterminationScript = developmentMilestoneScriptProvider.getScript(['requestParam':versionDeterminationRequest])

		parameters << new GitflowJobParameter(name: NEXT_MILESTONE_DEV_VERSION, valueListingScript: new ParameterListingScript(text:developmentVersionDeterminationScript),elementType: READ_ONLY_TEXT)

		//	parameters << new GitflowJobParameter(name: MILESTONE_TAG_MESSAGE, elementType: TEXT)
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

			def releasePushUrlParams = [releasePushUrl: ctx.scmRepository.releasePushUrl]

			def performMilestoneReleaseCmd = adapt(PERFORM_MILESTONE_VERSION_RELEASE, releasePushUrlParams)

			ctx.generatingOnWindows ? batchFile(performMilestoneReleaseCmd) : shell(performMilestoneReleaseCmd)

			maven configureMavenCommand(DEPLOY_TO_RELEASE_REPOSITORY)

			maven configureMavenCommand(UPDATE_TO_DEVELOPMENT_MILESTONE_SNAPSHOT)

			def commitAllFilesCommand = adapt(COMMIT_ALL_FILES_AND_PUSH, releasePushUrlParams)

			ctx.generatingOnWindows ? batchFile(commitAllFilesCommand):shell(commitAllFilesCommand)
		}
	}


	@Override
	protected def determineRegularEmailSubject(JobGenerationContext ctx,JobConfig jobConfig){

		'New milestone ${ENV,var="milestoneReleaseVersion"} created for '+ctx.repositoryName +' repository'
	}

	@Override
	protected def determineFailureEmailSubject(JobGenerationContext ctx,JobConfig jobConfig){

		'Action Required!!!! Milestone creation failed for '+ctx.repositoryName
	}
	
	/**
	 * @return the name of the branch which should be used to build the source code
	 */
	protected def identifySCMBranchForBuild(JobGenerationContext ctx){
		'release/${releaseBranch}'
	}

	static final def CHECKOUT_RELEASE_BRANCH = 'git checkout release/${releaseBranch}'

	static final def UPDATE_RELEASE_BRANCH_VERSION = 'versions:set -DnewVersion=${milestoneReleaseVersion} versions:commit'

	static final def COMMIT_ALL_FILES = 'git commit --all -m "Updated Maven POM versions for milestone release"'

	static final def DEPLOY_TO_RELEASE_REPOSITORY = 'deploy'

	static final def UPDATE_TO_DEVELOPMENT_MILESTONE_SNAPSHOT='versions:set -DnewVersion=${nextMilestoneDevelopmentVersion} versions:commit'

	static final def DEPLOY='deploy'

	static final def PERFORM_MILESTONE_VERSION_RELEASE =
	'''
			|git tag ${milestoneReleaseVersion}
			|git push #releasePushUrl#  HEAD:release/${releaseBranch}
			|git push #releasePushUrl# ${milestoneReleaseVersion}
		'''.stripMargin()

	static final def COMMIT_ALL_FILES_AND_PUSH =
	'''
			|git commit --all -m "Updated Maven POM with milestone snapshot versions"
			|git push  #releasePushUrl# HEAD:release/${releaseBranch}
		'''.stripMargin()
}
