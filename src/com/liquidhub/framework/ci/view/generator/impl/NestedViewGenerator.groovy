package com.liquidhub.framework.ci.view.generator.impl

import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.providers.stash.GitRepository
/*
 * PLEASE READ THIS CAREFULLY BEFORE YOU CHANGE ANYTHING IN THIS FILE
 *
 * Developer Note:
 *
 * In Jenkins, views have a reference to jobs. So, a view specifies the jobs which need to be included.
 *
 * A nested view provides a hierarchy of views instead of laying them out as flat tabs and creating UI clutter. Our intent is to create a project view
 * which contains all the repositories of the corresponding stash project  and every repository view including all the jobs (across branches) for
 * that repository (unless such a behavior is explicitly disallowed by the user generating the view)
 *
 * Generating a nested view one repository at a time poses a technical problem. Since the generator works on one repository at a time it does not have a
 * context of other repositories in the project.Additionally, an implementation to make a partial/incremental update ends up with XML manipulation.
 * We work around that problem by identifying the list of all repositories which are already configured for the current stash project, adding the
 * current repository to the pre existing list and regnerating the whole view again.
 *
 */
/**
 * Creates a nested view mirroring the way repositories exist under a project within Stash. Helps reduce the visual clutter
 * on the user interface.
 *
 * The following terminologies apply
 *  - project : A collection of repositories. Has a one to one corelation with repositories hosted on Stash
 *  - projectView : A view which contains all the jobs of all the repositories contained within the project. Materializes as a Nested View
 *
 * @author Rahul Mishra, LiquidHub
 *
 */
class NestedViewGenerator extends SectionedJobViewGenerator{

	private SectionedJobViewGenerator sectionJobViewGenerator = new SectionedJobViewGenerator()

	/**
	 *
	 * Creates a nested view for the Stash Project listing all repositories of the project as sub views. Each repository sub view in turn
	 * contains jobs configured for that repository. Build monitors, radiators, and delivery pipelines manfiest as additional sub views on
	 * the project view for each repository.
	 *
	 *
	 *
	 */
	@Override
	public def generateView(JobGenerationContext ctx) {

		def projectDetails = ctx.scmAPIClient.findCurrentProjectName()

		def projectName

		if(projectDetails && projectDetails.data){
			projectName = projectDetails.data.name
		}

		//Find if any repositories pre existed in the project view, append the current repository to that list
		def existingRepositories = ctx.viewHelper.findPreConfiguredRepositoryNamesInProjectView(projectName)

		ctx.logger.debug('Existing repository '+existingRepositories)

		def allRepositories = existingRepositories << ctx.scmRepository.repositorySlug

		Configuration config = ctx.configuration

		ctx.generateView(projectName,'nestedView'){

			views{

				allRepositories.each{repositoryName ->
					ctx.logger.debug('Generating for repository '+repositoryName)
					def gitflowJobRegExpConfig = [:]

					//Store the job regexp against the configuration key in a custom index
					gitflowJobRegExpConfig[SectionedJobViewGenerator.FEATURE_JOB_KEY] = createJobInclusionRegExp(repositoryName, config.gitflowFeatureBranchConfig )
					gitflowJobRegExpConfig[SectionedJobViewGenerator.RELEASE_JOB_KEY] = createJobInclusionRegExp(repositoryName, config.gitflowReleaseBranchConfig, config?.milestoneReleaseConfig?.jobPrefix)
					gitflowJobRegExpConfig[SectionedJobViewGenerator.HOTFIX_JOB_KEY] = createJobInclusionRegExp(repositoryName, config.gitflowHotfixBranchConfig)

					sectionedView (repositoryName, sectionJobViewGenerator.createSectionView(ctx, repositoryName, gitflowJobRegExpConfig))
				}

			}
		}
	}


}
