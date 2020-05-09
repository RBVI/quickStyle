package edu.ucsf.rbvi.quickStyle.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import org.cytoscape.service.util.CyServiceRegistrar;

public class QuickStyleDialogTaskFactory extends AbstractTaskFactory {

	final CyServiceRegistrar registrar;
  public QuickStyleDialogTaskFactory(final CyServiceRegistrar registrar) {
		this.registrar = registrar;
  }

  public boolean isReady() {
    return true;
  }

  public TaskIterator createTaskIterator() {
    return new TaskIterator(new QuickStyleDialogTask(registrar));
  }
}

