package com.liquidhub.framework.ci.view.generator.impl

import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.view.generator.ViewGenerator

/**
 * Creates a basic list view of all jobs for the repository
 * 
 * @author Rahul Mishra,LiquidHub
 *
 */

class BasicListViewGenerator implements ViewGenerator{

	@Override
	public def generateView(JobGenerationContext ctx) {

		def viewName = ctx.repositoryName+'-view'
		def jobRegExp = ctx.repositoryName+'.*'

		ctx.generateView(viewName, 'listView', createView(viewName, ))
	}


	public def createView(viewName, jobRegExp){

		return 	{
			name(viewName)
			jobs { regex(jobRegExp) } //Our repository jobs are named by this convention
			columns {
				status()
				weather()
				name()
				lastSuccess()
				lastFailure()
				lastDuration()
				buildButton()
				lastBuildConsole() //  requires the Extra Columns Plugin
				configureProject() // requires the Extra Columns Plugin
			}

		}
	}




}
