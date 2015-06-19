package com.liquidhub.framework.ci.job.generator.impl


import com.liquidhub.framework.ci.EmbeddedScriptProvider
import com.liquidhub.framework.ci.OSCommandAdapter
import com.liquidhub.framework.ci.model.BuildEnvironmentVariables
import com.liquidhub.framework.ci.model.GeneratedJobParameters
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.VersionDeterminationRequest
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.providers.jenkins.OperatingSystemCommandConfigurer
import com.liquidhub.framework.scm.MilestoneVersionDeterminationScriptProvider
import com.liquidhub.framework.scm.ReleaseChoiceOptionsScriptProvider
import com.liquidhub.framework.scm.model.SCMRemoteRefListingRequest
import com.liquidhub.framework.scm.model.SCMRepository



class GitflowFinishReleaseJobGenerator extends BaseGitflowJobGenerationTemplateSupport {

	private static final String MILESTONE_NAMING_STRATEGY='m'

	private EmbeddedScriptProvider releaseOptionsProvider = new ReleaseChoiceOptionsScriptProvider()
	private EmbeddedScriptProvider nextMilestoneChoiceProvider = new MilestoneVersionDeterminationScriptProvider()


	def getJobConfig(Configuration configuration){
		configuration.gitflowReleaseBranchConfig.finishConfig
	}

	@Override
	protected def configureJobParameterExtensions(JobGenerationContext context, JobConfig jobConfig){

		SCMRepository repository = context.scmRepository

		def repoUrl = repository.repoUrl, authDigest = repository.authorizedUserDigest

		SCMRemoteRefListingRequest request = new SCMRemoteRefListingRequest(
				targetUrl: repoUrl,
				authorizedUserDigest: authDigest,
				branchFilterText:  'release/'
				)


		def descriptionScript = descriptionListingProvider.getScript(['requestParam':request])

		def valueScript = valueListingProvider.getScript(['requestParam':request])

		def versionDeterminationRequest = new VersionDeterminationRequest(gitRepoUrl: repoUrl, authorizedUserDigest : authDigest, versionNamingStrategy: MILESTONE_NAMING_STRATEGY)

		def releaseChoicesScript = releaseOptionsProvider.getScript(['requestParam':versionDeterminationRequest])

		def vh = context.viewHelper

		def releaseBranchParam = vh.createChoiceOptionsView(GeneratedJobParameters.RELEASE_BRANCH , 'The release you intend to finish', valueScript, descriptionScript, null)
		def releaseVersionParam = vh.createChoiceOptionsView(GeneratedJobParameters.RELEASE_VERSION , 'The version you intend to release, a milestone or the final build? If you do not see any options above, your project has already had a final release', releaseChoicesScript, null,null)

		def nextMilestoneParamDescription = '''
					|This is the next development version after you make this milestone release. If you are making a general/final release
                    |this field does not apply, your development version will be guided by the version on the develop branch.
					|You cannot edit this field, but if you think the value in this field is incorrect, please contact your administrator

					'''.stripMargin()

		def developmentVersionDeterminationScript = nextMilestoneChoiceProvider.getScript(['requestParam':versionDeterminationRequest])

		def nextMilestoneVersionParam = vh.createSimpleTextBox(GeneratedJobParameters.NEXT_MILESTONE_DEV_VERSION, nextMilestoneParamDescription,developmentVersionDeterminationScript,true)

		releaseBranchParam >> releaseVersionParam >> nextMilestoneVersionParam
	}


	@Override
	protected def configureSteps(JobGenerationContext ctx, JobConfig jobConfig){

		def mvnConfigurer = ctx.configurers('maven')
		def osConfigurer = ctx.configurers('os')

		def configureOSCommand = {command,parameters=[:] ->
			osConfigurer.configure(ctx, jobConfig, command, parameters)
		}

		def configureMavenCommand = {goals ->

			def config = goals instanceof JobConfig ? goals :  ['goals': goals] as JobConfig

			mvnConfigurer.configure(ctx, config)
		}


		return{

			configureOSCommand(CHECKOUT_RELEASE_BRANCH)

			maven configureMavenCommand(UPDATE_RELEASE_BRANCH_VERSION)

			configureOSCommand(COMMIT_ALL_FILES)

			conditionalSteps{//Run the following steps for a milestone release
				
				condition{ expression('.*(M|RC|m|rc).*','${ENV,var="releaseVersion"}') } //We do not want variable substitution before it is embedded into configuration
				runner("DontRun") //For any other values, look at runner classes of Run Condition Plugin

				def substitutionParameters = [releasePushUrl: ctx.scmRepository.repoUrl]

				configureOSCommand(PERFORM_MILESTONE_VERSION_RELEASE, substitutionParameters)

				maven configureMavenCommand(DEPLOY_TO_RELEASE_REPOSITORY)

				maven configureMavenCommand(UPDATE_TO_NEXT_MILESTONE_VERSION)

				configureOSCommand(COMMIT_ALL_FILES_AND_PUSH)

			}

			conditionalSteps{
				//Run the following steps for a full release

				condition {expression('(?:(\\d+)\\.)?(?:(\\d+)\\.)?(\\*|\\d+)','${ENV,var="releaseVersion"}')} //1-3 dot-separated digits, following the maven versioning scheme with no qualifiers
				runner("DontRun") //For any other values, look at runner classes of Run Condition Plugin

				maven configureMavenCommand(jobConfig)

				configureOSCommand(CHECKOUT_MASTER_BRANCH)

				maven configureMavenCommand(UPDATE_TO_NEXT_MILESTONE_VERSION) //Lets push this to the artifact repository

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


}
