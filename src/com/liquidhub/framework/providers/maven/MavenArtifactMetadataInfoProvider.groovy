package com.liquidhub.framework.providers.maven

import com.liquidhub.framework.ci.logger.Logger
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.SeedJobParameters
import com.liquidhub.framework.model.MavenArtifact


/**
 * Analyzes the maven project object models (poms) to identify deployment and application configuration artifacts.
 * 
 * For multi module projects, this analysis is recursive. 
 * 
 * 
 * 
 * @author Rahul Mishra, LiquidHub
 *
 */
class MavenArtifactMetadataInfoProvider {

	XmlParser xmlParser = new XmlParser()
	
	MavenArtifact deployable
	
	MavenArtifact configurationArtifact

	private static Logger logger


	public MavenArtifactMetadataInfoProvider(JobGenerationContext context){

		logger = context.logger

		this.configurationJARNamingPattern = context.configuration.deploymentConfig.appConfigurationArtifactIdentificationPattern

		this.analyzeProjectMetadata('',context, true) //We assume that the project path is at the root of the worskspace
	}


	/**
	 * Analyzes the project and its sub modules to determine the deployment candidate and the artifact which hosts the projects configuration
	 * 
	 * @param projectPath  The project path where we begin the analysis -- we expect a pom.xml at this path
	 * 
	 *
	 */
	protected MavenArtifact analyzeProjectMetadata(String projectPath,JobGenerationContext context,boolean rootProject=false){

		def pomMetadata = loadPOM(context, projectPath)

		if(!pomMetadata){
			//If we cannot load a pom, maven determinations cannot be at play
			return
		}

		def groupId = pomMetadata?.groupId ? pomMetadata.groupId.text() : pomMetadata?.parent?.groupId.text()

		deployable = new MavenArtifact(groupId: groupId, artifactId: pomMetadata.artifactId.text())


		logger.debug 'Analyzing pom @ '+projectPath + File.separator +POM_FILE_NAME

		def thisArtifactPackaging = pomMetadata?.packaging  ? pomMetadata.packaging.text() : 'jar' //Switch to maven default when packaging is not specified

		switch(thisArtifactPackaging){

			case ['ear', 'war']:
				deployable.packaging = pomMetadata.packaging.text()
				logger.debug 'Found deployable with following characteristics '+deployable
				return deployable


			case 'jar':
				logger.debug 'Packaging is of type  jar, assuming that a jar cannot be deployed yet, skipping '
				if((pomMetadata.artifactId =~ configurationJARNamingPattern).matches() ){ //if a jar is named as 'conf' something, its a configuration jar
					this.configurationArtifact = new MavenArtifact(groupId: groupId, artifactId: pomMetadata.artifactId, packaging: 'jar')
				}
				return [:] //For now, we don't consider a JAR to be deployable

			case ('pom'):

				def childModules = pomMetadata.modules.module

				if(!childModules){
					logger.debug 'Found a pom with no modules, this is a corporate style pom and cannot be deployed'
					return [:] //If this is a POM and does not have modules, this is a corporate style pom and cannot be deployed
				}

				for(def moduleName: childModules){

					logger.debug 'Now analyzing module '+moduleName.text()

					def deploymentMetadata = analyzeProjectMetadata(projectPath+File.separator+moduleName.text(), context)
					if(deploymentMetadata){
						this.deployable = deploymentMetadata
					}else{
						logger.debug('No deployment metadata found, continuing ')
					}

				}
				break

			case 'pom':


			default:
				logger.debug 'Cannot handle packaging type of '+thisArtifactPackaging+' to decipher information yet. Framework enhancements required'
				break

		}

	}



	protected def loadPOM(JobGenerationContext context, String projectPath){

		def pomFilePath = projectPath.trim() == '' ? POM_FILE_NAME : projectPath+File.separator+POM_FILE_NAME

		try{
			pomFilePath = context.getVariable(SeedJobParameters.TARGET_PROJECT_BASE_MOUNT)+File.separator+pomFilePath 
			
			def pomContent = context.workspaceUtils.fileReader(pomFilePath)

			xmlParser.parseText(pomContent)

		}catch(Exception e){
			/*
			 * It's completely possible that we don't find a maven pom (if it's not a maven project,
			 * we stay silent and we expect user to provide the metadata manually
			 */
			logger.debug 'Could not determine maven artifact metadata. No POM found at '+pomFilePath
		}
	}



	private static final String POM_FILE_NAME = 'pom.xml'

	private static String configurationJARNamingPattern
}
