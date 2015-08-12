package com.liquidhub.framework.ci.model

enum GitflowJobParameterNames {

	CONFIGURE_BRANCH_JOBS('configureBranchJobs', CONFIGURE_BRANCH_JOB_PARAM_DESCRIPTION),
	FEATURE_NAME('featureName'),
	START_COMMIT('startCommit'),
	FEATURE_OWNER_EMAIL('featureOwnerEmail', 'Enter the email address of the team lead who owns this feature'),
	FEATURE_PRODUCTION_DATE('featureProductionDate', 'Enter the intended release of feature in production expressed in month and year. It is perfectly acceptable if the date changes later.binding Use the format mm/yyyyy. E.g. for October 2015 release, enter 10/2015'),
	RELEASE_DATE('releaseDate', 'Enter the intended production release month and year. Use the format mm/yyyyy. E.g. for October 2015 release, enter 10/2015'),
	
	RELEASE_FROM_BRANCH('releaseFromBranch'),
	RELEASE_BRANCH('releaseBranch'),
	RELEASE_VERSION('releaseVersion','Default version to use when starting the branch'),
	MILESTONE_RELEASE_VERSION('milestoneReleaseVersion','The version which will be assigned to your milestone release.  If you do not see any options , your project has already had a final release'),
	DEVELOPMENT_VERSION('developmentVersion', 'Default development version to use when finishing the branch'),
	NEXT_MILESTONE_DEV_VERSION('nextMilestoneDevelopmentVersion','This is the next development version after you make this milestone release. You cannot edit this field, but if you think the value in this field is incorrect, please contact your administrator'),

	
	HOTFIX_BRANCH('hotfixBranch','The hotfix you intend to finish'),
	HOTFIX_VERSION('hotfixVersion'),
	
	
	
	ALLOW_SNAPSHOTS_WHILE_CREATING_FEATURE('allowSnapshots','Should we proceed with creating feature branches even if there are snapshot depenendencies in develop ? '),
	ALLOW_SNAPSHOTS_WHILE_CREATING_HOTFIX('allowSnapshots','Should we proceed with creating hotfix branches even if there are snapshot depenendencies ? '),
	ALLOW_SNAPSHOTS_WHILE_CREATING_RELEASE('allowSnapshots','Should we proceed with creating the release branch even if there are snapshot depenendencies? '),
	ALLOW_SNAPSHOTS_WHILE_FINISHING_RELEASE('allowSnapshots','Should we proceed with finishing the release branch even if there are snapshot depenendencies? '),
	ALLOW_SNAPSHOTS_WHILE_FINISHING_HOTFIX('allowSnapshots','Should we proceed with finishing the hotfix branch even if there are snapshot depenendencies? '),
	PUSH_RELEASES('pushReleases','Push release branches to the remote upstream'),
	RELEASE_BRANCH_VERSION_SUFFIX('releaseBranchVersionSuffix','Suffix to append to versions on the release branch.'),
	ENABLE_FEATURE_VERSIONS('enableFeatureVersions', 'Append the feature name to the maven pom version on the feature branch. If your feature name is callidusSSOIntegration, your  maven pom version will include callidus_sso_integration. Notice the pattern and name your feature accordingly'),
	PUSH_FEATURES('pushFeatures', 'Push the feature branch to the remote upstream once it is created'),
	PUSH_HOTFIXES('pushHotfixes', 'Push this hotfix branch to repository'),
	KEEP_FEATURE_BRANCH('keepBranch', 'Keep the feature branch after finishing the feature/merging it to develop'),
	KEEP_RELEASE_BRANCH('keepBranch', 'Keep the release branch after finishing the release. You MUST delete it manually before you can create the next release branch'),
	KEEP_HOTFIX_BRANCH('keepBranch', 'Keep the hotfix branch after finishing the hotfix. You MUST delete it manually before you can create the next hotfix branch'),
	SKIP_FEATURE_MERGE_TO_DEVELOP('noFeatureMerge','Turn off merging changes from this feature branch to develop'),
	SQUASH_COMMITS('squashCommits','Squash all commits made on this branch into a single commit before merge'),
	SKIP_RELEASE_BRANCH_MERGE('noReleaseMerge', 'Turn off merging changes from the release branch to master and develop. This value is used only when you are doing a non milestone (GA) release'),
	SKIP_RELEASE_TAGGING('noTag', 'Turn off tagging the release in git'),
	RELEASE_TAG_MESSAGE('tagMessage','Commit message to use when tagging the release.'),
	MILESTONE_TAG_MESSAGE('milestoneTagMessage','Commit message to use when tagging the milestone.'),
	NO_DEPLOY('noDeploy','Turn off maven deployment. If false the "deploy" goal is called. If true the "install" goal is called'),
	SKIP_HOTFIX_TAGGING('noTag', 'Turn off tagging the hotfix in git'),
	

	GitflowJobParameterNames(parameterName, description=''){
		this.parameterName = parameterName
		this.description = description
	}

	def parameterName, description


	private static final CONFIGURE_BRANCH_JOB_PARAM_DESCRIPTION ='''
					|Do you want to generate the jobs for this branch after the branch is created? You can always create them later
					|using our job seeder, just pick the repository and enter the branch name.
					'''.stripMargin()
}
