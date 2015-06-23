//package com.liquidhub.framework.ci.job.generator.impl
//
//import com.liquidhub.framework.ci.model.JobGenerationContext
//import com.liquidhub.framework.config.model.Configuration
//import com.liquidhub.framework.config.model.JobConfig
//
//
//class GitflowStartHotfixJobGenerator extends BaseGitflowJobGenerationTemplateSupport{
//
//	@Override
//	def getJobConfig(Configuration configuration){
//		configuration.gitflowHotfixBranchConfig.startConfig
//	}
//
//
//	protected def configureJobParameterExtensions(JobGenerationContext context, JobConfig jobConfig){
//
//		def vh = context.viewHelper
//
//		def hotfixVersionParam = vh.createSimpleTextBox(GeneratedJobParameters.HOTFIX_VERSION, GeneratedJobParameters.HOTFIX_VERSION.description,'',false)
//
//		def startCommitParam = vh.createSimpleTextBox(GeneratedJobParameters.START_COMMIT, 'This is the branch from which the hotfix branch is created.This value cannot be edited', 'master',true)
//
//		def allowSnapshotsParam = vh.createSimpleCheckBox(GeneratedJobParameters.ALLOW_SNAPSHOTS,GeneratedJobParameters.ALLOW_SNAPSHOTS.description ,false)
//
//		def pushHotfixParam = vh.createSimpleCheckBox(GeneratedJobParameters.PUSH_HOTFIXES,GeneratedJobParameters.PUSH_HOTFIXES.description ,true)
//		
//		def configureBranchJobs = addChoiceToLaunchConfiguredBranchJob(context)
//
//		hotfixVersionParam >> startCommitParam >> allowSnapshotsParam >> pushHotfixParam >> configureBranchJobs
//	}
//
//
//
//	
//
//	def configureSteps(JobGenerationContext ctx, JobConfig jobConfig){
//
//		return {
//			shell ('git checkout hotfix/${hotfixVersion}')
//			maven ctx.configurers('maven').configure(ctx, jobConfig)
//
//		} >> addConditionalStepsForDownstreamJobLaunch(ctx, jobConfig)
//	}
//	
//	
//
//
//	
//	protected def preparePropertiesForDownstreamJobLaunch(JobGenerationContext context){
//		[gitRepoUrl: context.scmRepository.repoUrl, repoBranchName: 'hotfix/${hotfixVersion}']
//	}
//
//	
//}
