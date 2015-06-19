package com.liquidhub.framework.ci.model

import com.liquidhub.framework.ci.job.generator.JobGenerator
import com.liquidhub.framework.ci.logger.Logger
import com.liquidhub.framework.config.model.JobConfig;
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
 * There is physically no difference in these branch types, the differences exist based on usage context. This approach allows us to 
 * customize the jobs we generate for a given branch name/branch type
 *  
 * @author Rahul Mishra,LiquidHub
 *
 */
class JobGeneratorPipelineFilter {

	private static Logger logger

	List<JobGenerator> configuredJobGenerators = []

	JobGeneratorPipelineFilter(JobGenerationContext context){

		logger = JobGenerationContext.logger

		def currentBranch = context.scmRepository.repoBranchName

		def buildPipelinePreferences = context.configuration.buildPipelinePreferences

		//Step 1 : See if any branch name specific job generations have been configured, find the first not null result
		def repositoryBranchPipelineSettings = buildPipelinePreferences.findResult {it?.branchName == currentBranch ? it : null}

		//Step 2: See if any job generators have been configured against this branch type
		if(!repositoryBranchPipelineSettings){
			logger.debug 'No Job generators were configured against the branch name, evaluating branch types'
			repositoryBranchPipelineSettings = buildPipelinePreferences.findResult {it?.branchType == context.scmRepository.branchType ? it: null}
		}


		//Should we even be considerate about gitflow job generation? YES, if the branch has a control over setting up repository level configuration
		final boolean factorGitflowJobGeneration = GitFlowBranchTypes.type(currentBranch)?.requiresRepositorySetup()

		//This doesn't matter if gitflow job generation is not even a factor
		final boolean useGitflow = context.getVariable([SeedJobParameters.USE_GITFLOW])?.toBoolean()

		logger.debug 'Should gitflow job generation decisions be made ? '+factorGitflowJobGeneration
		logger.debug 'Is Gitflow job generation enabled ? '+useGitflow


		findGenerators(repositoryBranchPipelineSettings){generatorClass ->

			logger.debug 'Now looking for an instance of '+generatorClass

			JobGenerator generator = JobGeneratorRegistry.findGenerator(generatorClass)

			if(!generator){
				logger.warn 'No generator found. Please verify that the class name is correct. If your class name is correct, please add your generator to the registry'
				throw new RuntimeException('No generator in JobGeneratorRegistry configured for class '+generatorClass)
			}

			/*
			 * If this is a gitflow base generator, then the fact whether we generate the jobs depends on the instruction
			 * If gitflow job generation is not even a factor, include the generator
			 */
			final boolean includeGenerator = factorGitflowJobGeneration ? (generator.supportsGitflow() ? useGitflow : true) : true

			logger.debug 'Is '+ generator.class.simpleName+' being included ? '+includeGenerator

			if(includeGenerator){
				configuredJobGenerators << generator
			}


		}

		logger.debug 'Generators being used '+configuredJobGenerators

		//Step 3: If still no match, lets think defaults
		if(configuredJobGenerators.isEmpty()){

			logger.debug 'No Job generators were configured against the branch type or the branch name, reverting to internal defaults'
			configuredJobGenerators << JobGeneratorRegistry.CI_JOB_GENERATOR.instance

		}


	}

	/**
	 * Finds the generators configured in the pipeline settings and applies the the inclusion filter criteria
	 * 
	 * @param repositoryBranchPipelineSettings
	 * @param generatorInclusionFilter
	 * 
	 * @return
	 */
	protected def findGenerators(repositoryBranchPipelineSettings, generatorInclusionFilter){

		if(repositoryBranchPipelineSettings){ //If the branch settings exist, apply the filter criteria on them
			repositoryBranchPipelineSettings.pipeline.each{findGeneratorClasses(it, 'generatorClass', generatorInclusionFilter)  }
		}

	}



	protected def findGeneratorClasses(Map map, Object key, generatorInclusionFilter ) {
		map.get(key) ? generatorInclusionFilter(map[key]) : map.findResults { String k, v -> if( k.matches('startConfig|finishConfig')) findGeneratorClasses(v, key, generatorInclusionFilter) }
	}

}
