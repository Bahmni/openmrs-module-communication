package org.bahmni.module.communication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.BaseModuleActivator;

public class CommunicationActivator extends BaseModuleActivator {
    private final Log log = LogFactory.getLog(this.getClass());

    @Override
    public void started() {
        log.info("Started Bahmni Communication module");
    }

    @Override
    public void stopped() {
        log.info("Stopped Bahmni Communication module");
    }
}
