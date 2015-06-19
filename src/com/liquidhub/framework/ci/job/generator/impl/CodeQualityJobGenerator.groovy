package com.liquidhub.framework.ci.job.generator.impl



import com.liquidhub.framework.ci.job.generator.JobGenerator
import com.liquidhub.framework.ci.model.JobGenerationContext;
import com.liquidhub.framework.config.model.Configuration;
import com.liquidhub.framework.config.model.JobConfig;

class CodeQualityJobGenerator extends BaseJobGenerationTemplate{


	def getJobConfig(Configuration configuration){
		configuration.codeQualityConfig
	}

	protected def configureAdditionalPublishers(JobGenerationContext ctx, JobConfig jobConfig){

		return {
			findbugs('**/findbugsXml.xml', true) {
				thresholds(
						unstableTotal: [all: 1, high: 2, normal: 3, low: 4]
						)
			}
			pmd('**/pmd.xml') { shouldDetectModules true }
			checkstyle('**/checkstyle-result.xml') { shouldDetectModules true }
			dry('**/cpd.xml', 80, 20) { useStableBuildAsReference true }
		}
	}
}


