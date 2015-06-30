package com.liquidhub.framework.ci.view.generator.impl

import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.view.generator.ViewGenerator


class BasicListViewGenerator implements ViewGenerator{

	@Override
	public def generateView(JobGenerationContext ctx) {
		ctx.generateView(ctx.repositoryName+'-view', 'listView', createView(ctx.repositoryName+'.*'))
	}


	public def createView(viewName, jobRegExp){

		return 	{

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
