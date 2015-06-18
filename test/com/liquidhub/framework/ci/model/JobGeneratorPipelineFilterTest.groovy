package com.liquidhub.framework.ci.model;

import static org.junit.Assert.*

import org.junit.Test

import com.liquidhub.framework.ci.job.generator.impl.ContinuousIntegrationJobGenerator
import com.liquidhub.framework.ci.logger.PrintStreamLogger
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.scm.model.GitFlowBranchTypes
import com.liquidhub.framework.scm.model.SCMRepository

class JobGeneratorPipelineFilterTest {
	

	@Test
	public void createFilterForBranchName_WhenGeneratorsAreConfiguredCorrectly() {

		def scmRepository  = { repoBranchName: 'master' } as SCMRepository

		def buildPipelinePreferences = [
			[branchName: 'master', 'pipeline':['a':'b', generatorClass:ContinuousIntegrationJobGenerator.class.name]],
			[branchName: 'develop', 'pipeline':['1':'2', generatorClass:'FakeJobGeneratorClass']]
		]

		def configuration = new Configuration(buildPipelinePreferences: buildPipelinePreferences) as Configuration

		def context = [ scmRepository:  scmRepository ,
			configuration: configuration] as JobGenerationContext

		context.logger = new PrintStreamLogger(System.out)

		JobGeneratorPipelineFilter filter = new JobGeneratorPipelineFilter(context)
		
		def generators = filter.configuredJobGenerators
		
		context.logger.debug '----'+generators
		
		assert generators.size() == 1 //We want to make sure ONLY one generator was included
		assert generators[0] instanceof ContinuousIntegrationJobGenerator //We want to make sure only the desired one got included
	}
	
	@Test
	public void createFilterForBranchWhenNoGeneratorsAreConfigured() {

		def scmRepository  = { repoBranchName: 'feature/test1' } as SCMRepository

		def buildPipelinePreferences = [
			[branchName: 'master', 'pipeline':['a':'b', generatorClass:ContinuousIntegrationJobGenerator.class.name]],
			[branchName: 'develop', 'pipeline':['1':'2', generatorClass:'FakeJobGeneratorClass']]
		]

		def configuration = new Configuration(buildPipelinePreferences: buildPipelinePreferences) as Configuration

		def context = [ scmRepository:  scmRepository ,
			configuration: configuration] as JobGenerationContext

		context.logger = new PrintStreamLogger(System.out)

		JobGeneratorPipelineFilter filter = new JobGeneratorPipelineFilter(context)
		
		def generators = filter.configuredJobGenerators
				
		assert generators.size() == 1 //We want to make sure ONLY one generator was included
		assert generators[0] instanceof ContinuousIntegrationJobGenerator //We want to make sure only the desired one got included
	}
	
	
}
