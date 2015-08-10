package com.liquidhub.framework.ci

import com.liquidhub.framework.config.model.DeploymentJobConfig
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.scm.model.GitFlowBranchTypes

/**
 * Encapsulates the algorithm to build job names based on the specified contextual information.
 * 
 * The creator is agnostic of the job generation context which allows us to determine job names for downstream jobs
 * 
 * @author Rahul Mishra, LiquidHub
 *
 */
class JobNameBuilder {


	public createJobName(repositoryName,GitFlowBranchTypes branchType, branchName, JobConfig jobConfig, boolean gitflowGenerator=false){

		def baseName= jobConfig.jobName ?: suggestBaseNameForJob(jobConfig, repositoryName,branchType, branchName,gitflowGenerator)


		[jobConfig?.jobPrefix, baseName, jobConfig?.jobSuffix].findAll().join('')
	}


	public def suggestBaseNameForJob(jobConfig, repositoryName, branchType, branchName, gitflowGenerator){

		switch(branchType){

			case GitFlowBranchTypes.RELEASE :
				repositoryName+'-release'
				break


			case GitFlowBranchTypes.HOTFIX:
				repositoryName+'-hotfix'
				break

			case GitFlowBranchTypes.FEATURE:
				repositoryName+'-'+branchName
				break

			default: //There are a lot of situations which fall into this category, we handle them explicitly

				if(gitflowGenerator){
					repositoryName
				} else if (jobConfig instanceof DeploymentJobConfig){
					repositoryName+'-'+jobConfig.name.capitalize()
				}else{
					repositoryName+'-'+branchName
				}
				break
		}
	}
}
