package com.liquidhub.framework.providers.jenkins

import com.liquidhub.framework.JobSectionConfigurer
import com.liquidhub.framework.ci.model.Email
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.config.model.JobConfig

class ExtendedEmailNotificationSectionConfigurer implements JobSectionConfigurer {


	Closure configure(JobGenerationContext context, JobConfig jobConfig, Email email=null){
		
		context.logger.debug 'Email definition context is '+email

		def regularEmailRecipients = email.sendTo
		def escalationEmailRecipients = email.escalateTo
		def emailSubject = email.subject

		return {

			extendedEmail(regularEmailRecipients, emailSubject) {

				trigger(triggerName: 'Success', sendToRecipientList: true, sendToDevelopers:true)
				//Only to dev contributors
				trigger(triggerName: 'FirstFailure', includeCulprits: true, sendToRecipientList: true, sendToDevelopers:true, subject : 'Action Required!!!'+emailSubject)
				//To everyone who was explicitly specified using a list

				def failureEmailList  = [regularEmailRecipients, escalationEmailRecipients].join(",")

				trigger(triggerName: 'Failure', recipientList: failureEmailList, sendToRecipientList: true, sendToDevelopers:true,includeCulprits: true, subject : 'Action Required!!!'+emailSubject)
				configure { node ->
					node  << attachBuildLog(true)
					//We attach a default pre send script on master which allows messages to be flagged important when they fail
					node << presendScript($DEFAULT_PRESEND_SCRIPT)
				}
			}
		}

	}


}
