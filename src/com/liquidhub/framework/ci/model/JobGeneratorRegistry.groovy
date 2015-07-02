package com.liquidhub.framework.ci.model



import com.liquidhub.framework.ci.job.generator.JobGenerator
import com.liquidhub.framework.ci.job.generator.impl.ContinuousIntegrationJobGenerator
import com.liquidhub.framework.ci.job.generator.impl.DeploymentJobGenerator;
import com.liquidhub.framework.ci.job.generator.impl.GitflowFinishFeatureJobGenerator
import com.liquidhub.framework.ci.job.generator.impl.GitflowFinishHotfixJobGenerator
import com.liquidhub.framework.ci.job.generator.impl.GitflowFinishReleaseJobGenerator
import com.liquidhub.framework.ci.job.generator.impl.GitflowStartFeatureJobGenerator
import com.liquidhub.framework.ci.job.generator.impl.GitflowStartHotfixJobGenerator
import com.liquidhub.framework.ci.job.generator.impl.GitflowStartReleaseJobGenerator
import com.liquidhub.framework.ci.job.generator.impl.MilestoneReleaseJobGenerator


/**
 * A registry of all available job generators. For a job generator to be used by the framework, it must be registered here.
 * This allows us to work without relying on classloaders to initialize new instances of generators. It also helps us guarantee
 * that all generators are inherently singleton
 * 
 * @author Rahul Mishra,LiquidHub
 *
 */
enum JobGeneratorRegistry {

	CI_JOB_GENERATOR(new ContinuousIntegrationJobGenerator()),
	GITFLOW_FEATURE_START_JOB_GENERATOR(new GitflowStartFeatureJobGenerator()),
	GITFLOW_FEATURE_FINISH_JOB_GENERATOR(new GitflowFinishFeatureJobGenerator()),
	GITFLOW_RELEASE_START_JOB_GENERATOR(new GitflowStartReleaseJobGenerator()),
	GITFLOW_RELEASE_FINISH_JOB_GENERATOR(new GitflowFinishReleaseJobGenerator()),
	GITFLOW_HOTFIX_START_JOB_GENERATOR(new GitflowStartHotfixJobGenerator()),
	GITFLOW_HOTFIX_FINISH_JOB_GENERATOR(new GitflowFinishHotfixJobGenerator()),
	MILESTONE_RELEASE_JOB_GENERATOR(new MilestoneReleaseJobGenerator()),
	DEPLOYMENT_JOB_GENERATOR(new DeploymentJobGenerator())
	
	
	static {
		values().each{jobGenerator-> 
			classInstanceMapping[jobGenerator.instance.class.name] = jobGenerator.instance
		}
	}

	JobGeneratorRegistry(JobGenerator jobGenerator){
		this.instance = jobGenerator
	}


	static JobGenerator findGenerator(String className){
		classInstanceMapping[className]
	}
	
	
	public static void main(String[] args){
		println JobGeneratorRegistry.findGenerator('com.liquidhub.framework.ci.job.generator.impl.ContinuousIntegrationJobGenerator')
	}

	private def instance

	private static def classInstanceMapping = [:]
}
