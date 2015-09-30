package com.liquidhub.framework.ci.job.deployment

import static com.liquidhub.framework.ci.model.JobPermissions.ItemBuild
import static com.liquidhub.framework.ci.model.JobPermissions.ItemCancel
import static com.liquidhub.framework.ci.model.JobPermissions.ItemConfigure
import static com.liquidhub.framework.ci.model.JobPermissions.ItemDiscover
import static com.liquidhub.framework.ci.model.JobPermissions.ItemRead
import static com.liquidhub.framework.ci.model.JobPermissions.ItemWorkspace
import static com.liquidhub.framework.ci.model.JobPermissions.RunDelete
import static com.liquidhub.framework.ci.model.JobPermissions.RunUpdate
import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_TEXT

import com.liquidhub.framework.ci.EmbeddedScriptProvider
import com.liquidhub.framework.ci.job.generator.impl.BaseJobGenerationTemplate
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.ParameterListingScript
import com.liquidhub.framework.ci.model.SeedJobParameters
import com.liquidhub.framework.ci.model.DeploymentJobParameters
import com.liquidhub.framework.ci.view.ViewElementTypes
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.DeploymentJobConfig
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.config.model.RoleConfig
import com.liquidhub.framework.providers.jenkins.JenkinsJobViewSupport


class TomcatDeploymentTemplate extends GenericDeploymentTemplate{

	TomcatDeploymentTemplate(DeploymentJobConfig environmentConfig){
		super.environmentConfig = environmentConfig
	}

		protected def configureJobParameterExtensions(JobGenerationContext ctx, JobConfig jobConfig){

		def parameters = []

		def mvnGroupId = ctx.deployable.groupId
		def mvnArtifactId = ctx.deployable.artifactId
		def packaging = ctx.deployable.packaging
		

		JenkinsJobViewSupport.logger = ctx.logger

		DeploymentJobConfig deploymentConfig = ctx.configuration.deploymentConfig
		def artifactRepositoryUrl = deploymentConfig.artifactRepositoryUrl

		//ctx.logger.debug 'environment config is '+environmentConfig

		def contextRoot=environmentConfig.appContextRoot
		def deploymentHost = environmentConfig.deploymentHost
		def deployDirPath = environmentConfig.deployDirPath

		EmbeddedScriptProvider scriptProvider = deploymentConfig.getDeploymentArtifactListingProvider()

		def bindings = [:]
		
		bindings['baseRepositoryUrl'] = "'${artifactRepositoryUrl}'"
		bindings['groupId']=   "'${mvnGroupId}'"
		bindings['artifactId']= "'${mvnArtifactId}'"
		bindings['releaseVersionCountToDisplay']=environmentConfig.releaseVersionCountToDisplay
		bindings['snapshotVersionCountToDisplay']=environmentConfig.snapshotVersionCountToDisplay
		bindings['applyFeatureVersionExclusionFilter']=environmentConfig.applyFeatureVersionExclusionFilter
		
		def mavenMetadataDownloadScript = scriptProvider.getScript(bindings)

		def deploymentJobParameters = [
			DeploymentJobParameters.GROUP_ID.properties << [defaultValue:  "'${mvnGroupId}'"],
			DeploymentJobParameters.ARTIFACT_ID.properties << [defaultValue: "'${mvnArtifactId}'"],
			DeploymentJobParameters.PACKAGING.properties << [defaultValue: "'${packaging}'"],
			DeploymentJobParameters.ARTIFACT_VERSION.properties << [valueListingScript: new ParameterListingScript(text: mavenMetadataDownloadScript)],
			DeploymentJobParameters.DEPLOYMENT_HOST.properties <<  [defaultValue: "'${deploymentHost}'"],
			DeploymentJobParameters.DEPLOY_DIR_PATH.properties <<  [defaultValue: "'${deployDirPath}'"],
		]

		def parameterDefinitions = {}
		deploymentJobParameters.reverse().each{ parameterDefinitions = parameterDefinitions <<  ctx.viewHelper.defineParameter(it) }

		return parameterDefinitions
	}
			
}
