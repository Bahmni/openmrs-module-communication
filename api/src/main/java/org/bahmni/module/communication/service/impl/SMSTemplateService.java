package org.bahmni.module.communication.service.impl;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SMSTemplateService {

    public String message(String templateKey, Map<String, String> placeholderValues) {
        String formattedMessage = template(templateKey);

        Matcher matcher = Pattern.compile("\\{([^}]*)\\}").matcher(formattedMessage);
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            formattedMessage = formattedMessage.replace(String.format("{%s}", placeholder), placeholderValue(placeholderValues, placeholder));
        }

        return formattedMessage.replace("\\n", System.lineSeparator());
    }

    private String template(String key) {
        String template = Context.getAdministrationService().getGlobalProperty(key);
        if (!StringUtils.isBlank(template)) return template;
        return Context.getMessageSourceService().getMessage(key,
                null, new Locale("en"));
    }

    private String placeholderValue(Map<String, String> placeholderValues, String placeholder) {
        return placeholderValues.get(placeholder.toLowerCase().replaceAll("\\s", ""));
    }
}
