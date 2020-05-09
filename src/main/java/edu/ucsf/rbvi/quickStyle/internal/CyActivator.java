package edu.ucsf.rbvi.quickStyle.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_EXAMPLE_JSON;
import static org.cytoscape.work.ServiceProperties.COMMAND_LONG_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.COMMAND_SUPPORTS_JSON;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;

import java.util.Properties;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

import edu.ucsf.rbvi.quickStyle.internal.tasks.VersionTaskFactory;
import edu.ucsf.rbvi.quickStyle.internal.tasks.QuickStyleDialogTaskFactory;
import edu.ucsf.rbvi.quickStyle.internal.tasks.QuickStyleTaskFactory;

// TODO: [Optional] Improve non-gui mode
public class CyActivator extends AbstractCyActivator {
	String JSON_EXAMPLE = "{\"SUID\":1234}";

	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		// Get a handle on the CyServiceRegistrar
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);

		// Get our version number
		Version v = bc.getBundle().getVersion();
		String version = v.toString(); // The full version

		{
			QuickStyleTaskFactory quickStyleTF = new QuickStyleTaskFactory(registrar);
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "quickstyle");
			props.setProperty(COMMAND, "style");
			props.setProperty(COMMAND_DESCRIPTION, 
										    "Create a style for most common properties");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
								"<html>The protein query retrieves a STRING network for one or more proteins. <br />"
								+ "STRING is a database of known and predicted protein interactions for <br />"
								+ "thousands of organisms, which are integrated from several sources, <br />"
								+ "scored, and transferred across orthologs. The network includes both <br />"
								+ "physical interactions and functional associations.</html>");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
    	props.setProperty(COMMAND_EXAMPLE_JSON, JSON_EXAMPLE);
			registerService(bc, quickStyleTF, TaskFactory.class, props);

		}
		
		{
			QuickStyleDialogTaskFactory quickStyleDTF = new QuickStyleDialogTaskFactory(registrar);
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "quickstyle");
			props.setProperty(COMMAND, "dialog");
			props.setProperty(COMMAND_DESCRIPTION, 
										    "Display a dialog for users to create a quick style");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
								"<html>The protein query retrieves a STRING network for one or more proteins. <br />"
								+ "STRING is a database of known and predicted protein interactions for <br />"
								+ "thousands of organisms, which are integrated from several sources, <br />"
								+ "scored, and transferred across orthologs. The network includes both <br />"
								+ "physical interactions and functional associations.</html>");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
    	props.setProperty(COMMAND_EXAMPLE_JSON, JSON_EXAMPLE);
			registerService(bc, quickStyleDTF, TaskFactory.class, props);
		}

		{
      QuickStyleTaskFactory quickStyleTF =
              new QuickStyleTaskFactory(registrar);
      Properties props = new Properties();
      props.setProperty(PREFERRED_MENU, "Apps");
      props.setProperty(TITLE, "QuickStyle");
      props.setProperty(MENU_GRAVITY, "100.0");
      props.setProperty(IN_MENU_BAR, "true");
      registerService(bc, quickStyleTF, NetworkTaskFactory.class, props);

		}
		/*
		{
			GetNetworkTaskFactory getNetwork = new GetNetworkTaskFactory(manager, "protein");
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "string");
			props.setProperty(COMMAND, "protein query");
			props.setProperty(COMMAND_DESCRIPTION, 
										    "Create a STRING network from multiple protein names/identifiers");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
								"<html>The protein query retrieves a STRING network for one or more proteins. <br />"
								+ "STRING is a database of known and predicted protein interactions for <br />"
								+ "thousands of organisms, which are integrated from several sources, <br />"
								+ "scored, and transferred across orthologs. The network includes both <br />"
								+ "physical interactions and functional associations.</html>");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
    	props.setProperty(COMMAND_EXAMPLE_JSON, JSON_EXAMPLE);

			registerService(bc, getNetwork, TaskFactory.class, props);
		}
		*/


		{
			VersionTaskFactory versionFactory = new VersionTaskFactory(version);
			Properties versionProps = new Properties();
			versionProps.setProperty(COMMAND_NAMESPACE, "quickstyle");
			versionProps.setProperty(COMMAND, "version");
			versionProps.setProperty(COMMAND_DESCRIPTION, 
										           "Returns the version of quickStyle");
			versionProps.setProperty(COMMAND_SUPPORTS_JSON, "true");
    	versionProps.setProperty(COMMAND_EXAMPLE_JSON, "{\"version\":\"2.1.0\"}");
			registerService(bc, versionFactory, TaskFactory.class, versionProps);
		}
	}

}
