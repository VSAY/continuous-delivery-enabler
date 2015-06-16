package com.liquidhub.framework.providers.jenkins

import com.liquidhub.framework.JobSectionConfigurer
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.model.JobGenerationContext

class ExtendedEmailNotificationSectionConfigurer implements JobSectionConfigurer {


	Closure configure(JobGenerationContext context, JobConfig jobConfig, emailDefContext=null){
		
		def contributorEmails = emailDefContext.contributorEmails
		def escalationEmails = emailDefContext.escalationEmails
		def emailSubject = emailDefContext.emailSubject

		return {

			extendedEmail(contributorEmails, emailSubject) {

				trigger(triggerName: 'Success', sendToRecipientList: true, sendToDevelopers:true)
				//Only to dev contributors
				trigger(triggerName: 'FirstFailure', includeCulprits: true, sendToRecipientList: true, sendToDevelopers:true, subject : 'Action Required!!!'+emailSubject)
				//To everyone who was explicitly specified using a list

				def failureEmailList  = [contributorEmails, escalationEmails].join(",")

				trigger(triggerName: 'Failure', recipientList: failureEmailList, sendToRecipientList: true, sendToDevelopers:true,includeCulprits: true, subject : 'Action Required!!!'+emailSubject)
				configure { node ->
					node  << attachBuildLog(true)
					//We attach a default pre send script on master which allows messages to be flagged important when they fail
					node << presendScript($DEFAULT_PRESEND_SCRIPT)
				}
			}
		}

	}

	def configureEmailNotifications(contributorEmails, escalationEmails, emailSubject){


	}
}
