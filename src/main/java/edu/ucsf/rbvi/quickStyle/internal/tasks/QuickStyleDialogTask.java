package edu.ucsf.rbvi.quickStyle.internal.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.DialogTaskManager;

import org.cytoscape.service.util.CyServiceRegistrar;

import edu.ucsf.rbvi.quickStyle.internal.utils.ModelUtils;
import edu.ucsf.rbvi.quickStyle.internal.utils.StyleWrapper;

public class QuickStyleDialogTask extends AbstractTask {
	final CyServiceRegistrar registrar;
	final DialogTaskManager taskManager;

	@Tunable(description="Columns for node size", context="nogui")
	public String nodeSizeColumns = null;

	@Tunable(description="Columns for node color", context="nogui")
	public String nodeColorColumns = null;

	@Tunable(description="Columns for edge width", context="nogui")
	public String edgeWidthColumns = null;

	@Tunable(description="Columns for edge color", context="nogui")
	public String edgeColorColumns = null;

	public QuickStyleDialogTask(final CyServiceRegistrar registrar) {
		this.registrar = registrar;
		this.taskManager = registrar.getService(DialogTaskManager.class);
	}

	public void run(TaskMonitor monitor) {
		var nSizeColumns = getListFromString(nodeSizeColumns);
		var nColorColumns = getListFromString(nodeColorColumns);
		var eWidthColumns = getListFromString(edgeWidthColumns);
		var eColorColumns = getListFromString(edgeColorColumns);
		QuickStyleTask task = new QuickStyleTask(registrar, null, nColorColumns, eColorColumns, nSizeColumns, eWidthColumns);
		SwingUtilities.invokeLater(() -> {
			taskManager.execute(new TaskIterator(task));
		});
	}

	List<String> getListFromString(String input) {
		if (input == null||input == "") return new ArrayList<>();
		String[] tokens = input.split(",");
		return Arrays.asList(tokens);
	}

}
