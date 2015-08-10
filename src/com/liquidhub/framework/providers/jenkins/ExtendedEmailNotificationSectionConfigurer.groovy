package com.liquidhub.framework.providers.jenkins

import com.liquidhub.framework.JobSectionConfigurer
import com.liquidhub.framework.ci.model.Email
import com.liquidhub.framework.ci.model.EmailNotificationContext
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.config.model.JobConfig

class ExtendedEmailNotificationSectionConfigurer implements JobSectionConfigurer {


	Closure configure(JobGenerationContext context, JobConfig jobConfig, EmailNotificationContext notificationContext=null){

		context.logger.debug 'Email definition context is '+notificationContext

		def regularEmailRecipients = notificationContext.recipientList
		def contentTemplate = notificationContext.contentTemplate
		def emailSubject = notificationContext.subjectTemplate

		return {

			extendedEmail(regularEmailRecipients, emailSubject, contentTemplate) {

				notificationContext.emailTriggers.each{ triggerName, Email email ->
					trigger(triggerName: triggerName, sendToRecipientList: email.sendToRecipientList, sendToDevelopers:email.sendToDevelopers, includeCulprits: email.includeCulprits, subject: email.subject, content: email.body)
				}

				configure { node ->
					node  << attachBuildLog(true)
					//We attach a default pre send script on master which allows messages to be flagged important when they fail
					node << presendScript($DEFAULT_PRESEND_SCRIPT)
				}
			}
		}

	}


}
