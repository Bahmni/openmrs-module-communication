package org.bahmni.module.communication.service.impl;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SMSTemplateService {

	public String templateMessage(String smsTemplate, Map<String, String> arguments) {
		String template = Context.getAdministrationService().getGlobalProperty(smsTemplate);
		String formattedMessage = StringUtils.isBlank(template) ? Context.getMessageSourceService().getMessage(smsTemplate,
		    null, new Locale("en")) : template;
		
		Pattern pattern = Pattern.compile("\\{([^}]*)\\}");
		Matcher matcher = pattern.matcher(formattedMessage);
		while (matcher.find()) {
			String placeholder = matcher.group(1);
			String modifiedPlaceholder = placeholder.toLowerCase().replaceAll("\\s", "");
			Object value = arguments.get(modifiedPlaceholder);
			placeholder = String.format("{%s}", placeholder);
			formattedMessage = formattedMessage.replace(placeholder, String.valueOf(value));
		}
		
		return formattedMessage.replace("\\n", System.lineSeparator());
	}
	
}
