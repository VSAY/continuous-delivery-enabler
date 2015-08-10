package com.liquidhub.framework.ci.view.generator.impl

import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.SeedJobParameters
import com.liquidhub.framework.ci.view.generator.ViewGenerator
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.GitflowBranchingConfig

class SectionedJobViewGenerator implements ViewGenerator{

	private BasicListViewGenerator listViewGenerator = new BasicListViewGenerator()

	private static final String FEATURE_JOB_KEY = 'feature'
	private static final String RELEASE_JOB_KEY = 'release'
	private static final String HOTFIX_JOB_KEY = 'hotfix'

	@Override
	public def generateView(JobGenerationContext ctx) {

		//This view will be named after the repository for which it is being created
		def repositoryName = ctx.scmRepository.repositorySlug

		def gitflowJobRegExpConfig = [:]

		Configuration configuration = ctx.configuration

		//Store the job regexp against the configuration key in a custom index
		gitflowJobRegExpConfig[FEATURE_JOB_KEY] = createJobInclusionRegExp(repositoryName, configuration.gitflowFeatureBranchConfig)
		gitflowJobRegExpConfig[RELEASE_JOB_KEY] = createJobInclusionRegExp(repositoryName, configuration.gitflowReleaseBranchConfig, configuration?.milestoneReleaseConfig?.jobPrefix)
		gitflowJobRegExpConfig[HOTFIX_JOB_KEY] = createJobInclusionRegExp(repositoryName, configuration.gitflowHotfixBranchConfig)

		ctx.generateView(repositoryName, 'sectionedView', createSectionView(ctx, repositoryName, gitflowJobRegExpConfig))

	}


	public def createSectionView(JobGenerationContext ctx, repositoryName, gitflowJobRegExpConfig){

		//This is a REST API call and executes remotely
		def repositoryResponse = ctx.scmAPIClient.listRepositoryBranches(repositorySlug : repositoryName)


		//Our goal is to have a section for each of the following branch types
		def featureBranchJobRegExp = '' //There can be more than one feature branch
		def releaseBranchJobRegExp =''//Only one release branch is allowed at a given time
		def hotfixBranchJobRegexp  =''//Only one hotfix branch is allowed at a given time
		
		

		if(repositoryResponse && repositoryResponse.data.values){
			
			repositoryResponse.data.values.each{ repositoryBranch ->
				
				//Gitflow jobs are named with a '/', our job names switch to hyphens, we need to capture such jobs under the branch	
				def branchDisplayId = repositoryBranch.displayId.replace("/","-")
				
				ctx.logger.debug('branch display id is '+branchDisplayId)
				
					switch(branchDisplayId){

					case ~/feature.*/:
						featureBranchJobRegExp = featureBranchJobRegExp + "(${repositoryName}-${branchDisplayId}.*)|"
						break

					case ~/hotfix.*/:
						hotfixBranchJobRegexp = "${repositoryName}-${branchDisplayId}.*"
						break

					case ~/release.*/:
						releaseBranchJobRegExp =  "${repositoryName}-${branchDisplayId}.*"
						break

				}

			}
		}
		

		def sectionViewConfiguration = ctx.configuration.viewConfig.sectionViews

		def templateParams = [repositoryName: repositoryName, generationDateTime: new Date()]

		def configBaseMount = ctx.getVariable(SeedJobParameters.FRAMEWORK_CONFIG_BASE_MOUNT)

		def sectionViewTemplateFilePath = [configBaseMount, sectionViewConfiguration.projectDescriptionTemplatePath].join(File.separator)

		def sectionViewDescription = ctx.templateEngine.withContentFromTemplatePath(sectionViewTemplateFilePath, templateParams)


		return {
			//Create sections dynamically, one for each  branch listed from the repository
			description sectionViewDescription

			sections{


				listView listViewGenerator.createView("Master(${repositoryName}) ", "${repositoryName}-master-.*")
				
				listView listViewGenerator.createView("Develop(${repositoryName}) ", "${repositoryName}-develop-.*")
				
				listView listViewGenerator.createView("Release(${repositoryName}) ", releaseBranchJobRegExp)
				
				listView listViewGenerator.createView("Hotfix(${repositoryName}) ", hotfixBranchJobRegexp)
				
				listView listViewGenerator.createView("Features(${repositoryName}) ", featureBranchJobRegExp)

				listView listViewGenerator.createView("ManageDeployments(${repositoryName})", "$repositoryName.*[dD]eploy.*")

				listView listViewGenerator.createView("ManageFeatures(${repositoryName})", gitflowJobRegExpConfig[FEATURE_JOB_KEY])

				listView listViewGenerator.createView("ManageReleases(${repositoryName})",gitflowJobRegExpConfig[RELEASE_JOB_KEY])

				listView listViewGenerator.createView("ManageEmergencyHotfix(${repositoryName})", gitflowJobRegExpConfig[HOTFIX_JOB_KEY])


			}

		}

	}

	/**
	 * Creates a more sophisiticated grepping pattern for gitflow jobs
	 *
	 * @param repositoryName
	 * @param gitflowConfig
	 * @return
	 */
	protected createJobInclusionRegExp(repositoryName,GitflowBranchingConfig gitflowConfig, additionalRegExp=null){

		def startJobExpr = gitflowConfig.startConfig.jobPrefix
		def finishJobExpr = gitflowConfig.finishConfig.jobPrefix

		if(additionalRegExp){
			"(${startJobExpr}|${finishJobExpr}|${additionalRegExp})$repositoryName"
		}else{
			"(${startJobExpr}|${finishJobExpr})$repositoryName"
		}

	}




}
