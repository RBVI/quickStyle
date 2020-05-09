package edu.ucsf.rbvi.quickStyle.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import org.cytoscape.service.util.CyServiceRegistrar;

public class QuickStyleTaskFactory extends AbstractNetworkTaskFactory implements TaskFactory {

	final CyServiceRegistrar registrar;
  public QuickStyleTaskFactory(final CyServiceRegistrar registrar) {
		this.registrar = registrar;
  }

  public boolean isReady() {
    return true;
  }

  public boolean isReady(CyNetwork network) {
		if (network != null)
    	return true;
		return false;
  }

  public TaskIterator createTaskIterator(CyNetwork network) {
    return new TaskIterator(new QuickStyleTask(registrar, network));
  }

  public TaskIterator createTaskIterator() {
    return new TaskIterator(new QuickStyleTask(registrar, null));
  }
}

