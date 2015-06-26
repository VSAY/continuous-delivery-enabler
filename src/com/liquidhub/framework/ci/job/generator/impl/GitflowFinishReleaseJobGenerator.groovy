package com.liquidhub.framework.ci.job.generator.impl


import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.NEXT_MILESTONE_DEV_VERSION
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.RELEASE_BRANCH
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.RELEASE_VERSION
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.ALLOW_SNAPSHOTS_WHILE_FINISHING_RELEASE
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.KEEP_RELEASE_BRANCH
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.SKIP_RELEASE_BRANCH_MERGE
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.SQUASH_COMMITS
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.RELEASE_TAG_MESSAGE
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.SKIP_RELEASE_TAGGING

import com.liquidhub.framework.ci.EmbeddedScriptProvider
import com.liquidhub.framework.ci.model.BuildEnvironmentVariables
import com.liquidhub.framework.ci.model.GitflowJobParameter
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.ParameterListingScript
import com.liquidhub.framework.ci.model.VersionDeterminationRequest
import com.liquidhub.framework.ci.view.ViewElementTypes
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.scm.MilestoneVersionDeterminationScriptProvider
import com.liquidhub.framework.scm.ReleaseChoiceOptionsScriptProvider
import com.liquidhub.framework.scm.model.SCMRemoteRefListingRequest
import com.liquidhub.framework.scm.model.SCMRepository
import static com.liquidhub.framework.providers.jenkins.OperatingSystemCommandAdapter.adapt
import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.TEXT
import static com.liquidhub.framework.ci.view.ViewElementTypes.BOOLEAN_CHOICE

import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.TEXT
import static com.liquidhub.framework.ci.view.ViewElementTypes.BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_TEXT



class GitflowFinishReleaseJobGenerator extends BaseGitflowJobGenerationTemplateSupport {

	private static final String MILESTONE_NAMING_STRATEGY='m'

	private EmbeddedScriptProvider releaseOptionsProvider = new ReleaseChoiceOptionsScriptProvider()
	private EmbeddedScriptProvider nextMilestoneChoiceProvider = new MilestoneVersionDeterminationScriptProvider()


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

		def versionDeterminationRequest = new VersionDeterminationRequest(gitRepoUrl: repoUrl, authorizedUserDigest : authDigest, versionNamingStrategy: MILESTONE_NAMING_STRATEGY)

		def releaseChoicesScript = releaseOptionsProvider.getScript(['requestParam':versionDeterminationRequest])

		parameters << new GitflowJobParameter(
				name: RELEASE_VERSION,
				description : 'The version you intend to release, a milestone or the final build? If you do not see any options above, your project has already had a final release',
				valueListingScript: new ParameterListingScript(text:releaseChoicesScript),
				elementType: ViewElementTypes.SINGLE_SELECT_CHOICES
				)


		def developmentVersionDeterminationScript = nextMilestoneChoiceProvider.getScript(['requestParam':versionDeterminationRequest])

		parameters << new GitflowJobParameter(
				name: NEXT_MILESTONE_DEV_VERSION,
				description : '''
					|This is the next development version after you make this milestone release. If you are making a general/final release
                    |this field does not apply, your development version will be guided by the version on the develop branch.
					|You cannot edit this field, but if you think the value in this field is incorrect, please contact your administrator

					'''.stripMargin(),
				valueListingScript: new ParameterListingScript(text:developmentVersionDeterminationScript),
				elementType: READ_ONLY_TEXT
				)

		parameters << new GitflowJobParameter(name: ALLOW_SNAPSHOTS_WHILE_FINISHING_RELEASE, elementType: READ_ONLY_BOOLEAN_CHOICE, defaultValue:false)
		parameters << new GitflowJobParameter(name: KEEP_RELEASE_BRANCH, elementType: READ_ONLY_BOOLEAN_CHOICE, defaultValue:true)
		parameters << new GitflowJobParameter(name: SKIP_RELEASE_BRANCH_MERGE, elementType: BOOLEAN_CHOICE,defaultValue: false)
		parameters << new GitflowJobParameter(name: SQUASH_COMMITS, elementType: BOOLEAN_CHOICE, defaultValue:false)
		parameters << new GitflowJobParameter(name: SKIP_RELEASE_TAGGING, elementType: READ_ONLY_BOOLEAN_CHOICE, defaultValue:false)
		parameters << new GitflowJobParameter(name: RELEASE_TAG_MESSAGE, elementType: TEXT)
	}



	@Override
	def configureBuildSteps(JobGenerationContext ctx, JobConfig jobConfig){

		def mvnConfigurer = ctx.configurers('maven')

		def configureMavenCommand = {goals ->

			def config = goals instanceof JobConfig ? goals :  ['goals': goals] as JobConfig

			mvnConfigurer.configure(ctx, config)
		}


		return{

			ctx.generatingOnWindows ? batchFile(adapt(CHECKOUT_RELEASE_BRANCH)) : shell(CHECKOUT_RELEASE_BRANCH)

			maven configureMavenCommand(UPDATE_RELEASE_BRANCH_VERSION)

			ctx.generatingOnWindows ? batchFile(adapt(COMMIT_ALL_FILES)) : shell(COMMIT_ALL_FILES)

			conditionalSteps{
				//Run the following steps for a milestone release

				condition{ expression('.*(M|RC|m|rc).*','${ENV,var="releaseVersion"}') } //We do not want variable substitution before it is embedded into configuration
				runner("DontRun") //For any other values, look at runner classes of Run Condition Plugin

				def releasePushUrlParams = [releasePushUrl: ctx.scmRepository.releasePushUrl]

				def performMilestoneReleaseCmd = adapt(PERFORM_MILESTONE_VERSION_RELEASE, releasePushUrlParams)

				ctx.generatingOnWindows ? batchFile(performMilestoneReleaseCmd) : shell(performMilestoneReleaseCmd)

				maven configureMavenCommand(DEPLOY_TO_RELEASE_REPOSITORY)

				maven configureMavenCommand(UPDATE_TO_NEXT_MILESTONE_VERSION)

				def commitAllFilesCommand = adapt(COMMIT_ALL_FILES_AND_PUSH, releasePushUrlParams)

				ctx.generatingOnWindows ? batchFile(commitAllFilesCommand):shell(commitAllFilesCommand)

			}

			conditionalSteps{
				//Run the following steps for a full release

				condition {expression('(?:(\\d+)\\.)?(?:(\\d+)\\.)?(\\*|\\d+)','${ENV,var="releaseVersion"}')} //1-3 dot-separated digits, following the maven versioning scheme with no qualifiers
				runner("DontRun") //For any other values, look at runner classes of Run Condition Plugin

				maven configureMavenCommand(jobConfig)

				ctx.generatingOnWindows ? batchFile(adapt(CHECKOUT_MASTER_BRANCH)):shell(CHECKOUT_MASTER_BRANCH)

				maven configureMavenCommand(DEPLOY) //Lets push this to the artifact repository

			}
		}

	}

	protected def determineEmailSubject(ctx, jobConfig){

		'Release # ${PROJECT_VERSION} finish '+ BuildEnvironmentVariables.BUILD_STATUS.paramValue+'!'

	}


	static final def PERFORM_MILESTONE_VERSION_RELEASE =
	'''
			|git tag ${releaseVersion}
			|git push #releasePushUrl#  HEAD:release/${releaseBranch}
			|git push #releasePushUrl# ${releaseVersion}
		'''.stripMargin()

	static final def COMMIT_ALL_FILES_AND_PUSH =
	'''
			|git commit --all -m "Updated Maven POM with milestone snapshot versions"
			|git push  #releasePushUrl# HEAD:release/${releaseBranch}
		'''.stripMargin()


	static final def CHECKOUT_MASTER_BRANCH = 'git checkout master'

	static final def CHECKOUT_RELEASE_BRANCH = 'git checkout release/${releaseBranch}'

	static final def UPDATE_RELEASE_BRANCH_VERSION = 'versions:set -DnewVersion=${releaseVersion} versions:commit'

	static final def COMMIT_ALL_FILES = 'git commit --all -m "Updated Maven POM versions for release"'

	static final def DEPLOY_TO_RELEASE_REPOSITORY = 'deploy -DupdateReleaseInfo=true'

	static final def UPDATE_TO_NEXT_MILESTONE_VERSION='versions:set -DnewVersion=${nextMilestoneDevelopmentVersion} versions:commit'

	static final def DEPLOY='deploy'


}
