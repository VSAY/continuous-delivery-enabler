package com.liquidhub.framework.model

import com.liquidhub.framework.ci.job.generator.JobGenerator
import com.liquidhub.framework.ci.logger.Logger
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.scm.model.GitFlowBranchTypes

/**
 * Makes decision about the job generators to be used for a given generation context.
 * 
 * Investigates if explicit configurations have been made against an explicit branch name. Falls back to configurations against branch type.
 * 
 * Based on gitflow, branch types are classified as 
 *  - Feature
 *  - Release
 *  - HotFix
 * 
 * There is physically no difference in these branch types, the differences exist based on usage context. This approach allows us to a unique policy per usage context.
 *  
 * @author Rahul Mishra,LiquidHub
 *
 */
class JobGeneratorPipelineFilter {

	JobGenerator[] configueredJobGenerators= []
	
	private static Logger logger

	JobGeneratorPipelineFilter(JobGenerationContext context){

		logger = JobGenerationContext.logger

		def currentBranch = context.scmRepository.repoBranchName
		
		Configuration configuration = context.configuration


		//Step 1 : See if any branch name specific job generations have been configured
		def repositoryBranchPipelineSettings = configuration.buildPipelinePreferences.grep({it.branchName == currentBranch})

		//Step 2: See if any job generators have been configured against this branch type
		if(!repositoryBranchPipelineSettings){
			logger.debug 'No Job generators were configured against the branch name, evaluating branch types'
			repositoryBranchPipelineSettings = configuration.buildPipelinePreferences.grep({it.branchType == context.scmRepository.branchType})
		}

		//Should we even be considerate about gitflow job generation? YES, if the branch has a control over setting up repository level configuration
		final boolean factorGitflowJobGeneration = GitFlowBranchTypes.type(currentBranch)?.requiresRepositorySetup()
		
		
		final boolean useGitflow = context.config?.useGitflow?.toBoolean() //This doesn't matter if gitflow job generation is not even a factor

		logger.debug 'Should gitflow job generation decisions be made ? '+factorGitflowJobGeneration
		logger.debug 'Is Gitflow job generation enabled ? '+useGitflow?.toBoolean()

		//When the branch indicates repository set up, we consider whether we should allow gitflow style job management
//		repositoryBranchPipelineSettings.pipeline.each{setting->
//			setting.each{configuration->
//
////				def generator = JOB_BUILDER_FACTORY[configuration.generatorClass]
////				def gitflowBasedGenerator = generator instanceof BaseGitflowJobGenerationTemplateSupport
////
////				logger.debug 'Is '+ configuration.generatorClass+' a gitflow based generator ? '+gitflowBasedGenerator
////
////				boolean includeGenerator = true
////				/*
////				 * Our rule section which indicates when a generator can/ cannot become a part of a generation pipeline. 
////				 * The configuration section just 'expresses' an intent, we need to validate if the intent aligns with the rules we want
////				 * Include gitflow jobs only when gitflow job generation is a factor, the generator is gitflow based AND a preference to use gitflow has been
////				 * indicated
////				 */
////
////				if( factorGitflowJobGeneration){
////					//If this is a gitflow base generator, then the fact whether we generate the jobs depends on the instruction
////					includeGenerator = gitflowBasedGenerator ? useGitflow : true
////
////				}else{ //If gitflow job generation is not even a factor, include the generator
////					includeGenerator = true
////
////				}
////
////               logger.debug 'final decision on inclusion '+includeGenerator
////				if(includeGenerator){
////					logger.debug 'Including '+generator.class.getSimpleName()
////					configueredJobGenerators.push(generator)
////				}else{
////					logger.debug 'Did not include '+generator.class.getSimpleName()
////				}
////			}
//			
//		}
		
//		//Step 3: If still no match
//		if(configueredJobGenerators.isEmpty()){
//			logger.debug 'No Job generators were configured against the branch type or the branch name, reverting to internal defaults'
//			configueredJobGenerators.push(JOB_BUILDER_FACTORY['com.ibx.frontoffice.jenkins.job.generator.impl.ContinuousIntegrationJobGenerator'])
//		}


	}

//	/*
//	 * Manages a static mapping of the class name against the class instance.
//	 *
//	 * Helps avoid using a class loader and we don't need that level of dynamic behavior yet.
//	 */
//	def static JOB_BUILDER_FACTORY = [
//		'com.ibx.frontoffice.jenkins.job.generator.impl.ContinuousIntegrationJobGenerator': new ContinuousIntegrationJobGenerator(),
//		'com.ibx.frontoffice.jenkins.job.generator.impl.CodeQualityJobGenerator': new CodeQualityJobGenerator(),
//		'com.ibx.frontoffice.jenkins.job.generator.impl.GitflowReleaseBranchJobGenerator':new GitflowReleaseBranchJobGenerator(),
//		'com.ibx.frontoffice.jenkins.job.generator.impl.IntegrationTestJobGenerator':new IntegrationTestJobGenerator(),
//		'com.ibx.frontoffice.jenkins.job.generator.impl.DeploymentJobGenerator':new DeploymentJobGenerator(),
//		'com.ibx.frontoffice.jenkins.job.generator.impl.GitflowFeatureBranchJobGenerator':new GitflowFeatureBranchJobGenerator(),
//		'com.ibx.frontoffice.jenkins.job.generator.impl.GitflowHotfixBranchJobGenerator':new GitflowHotfixBranchJobGenerator()
//	]

}
