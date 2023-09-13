package org.bahmni.module.communication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.communication.properties.CommunicationProperties;
import org.openmrs.module.BaseModuleActivator;

public class CommunicationActivator extends BaseModuleActivator {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * @see #started()
	 */
	@Override
	public void willStart() {
		CommunicationProperties.load();
	}
	
	@Override
	public void started() {
		log.info("Started Bahmni Communication module");
	}
	
	@Override
	public void stopped() {
		log.info("Stopped Bahmni Communication module");
	}
}
