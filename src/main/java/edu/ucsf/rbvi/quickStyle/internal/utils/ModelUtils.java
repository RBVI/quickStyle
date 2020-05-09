package edu.ucsf.rbvi.quickStyle.internal.utils;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

import org.cytoscape.work.util.ListSingleSelection;

public class ModelUtils {

	public static ListSingleSelection<String> getNumericNodeColumns(CyNetwork net, List<String> nodeColumns) {
		CyTable table = net.getDefaultNodeTable();
		return getNumericColumns(table, nodeColumns);
	}

	public static ListSingleSelection<String> getNumericEdgeColumns(CyNetwork net, List<String> edgeColumns) {
		CyTable table = net.getDefaultEdgeTable();
		return getNumericColumns(table, edgeColumns);
	}

	public static ListSingleSelection<String> getNumericColumns(CyTable table, List<String> columns) {
		List<String> cols = new ArrayList<>();
		if (columns == null)
			return getNumericColumns(table);

		if (columns == null || columns.isEmpty())
			return null;

		for (String col: columns) {
			CyColumn c = table.getColumn(col);
			if (c != null && Number.class.isAssignableFrom(c.getType()))
				cols.add(col);
		}

		return new ListSingleSelection<String>(cols);

	}

	public static ListSingleSelection<String> getNumericColumns(CyTable table) {
		List<String> cols = new ArrayList<>();
		cols.add("");
		for (CyColumn col: table.getColumns()) {
			if (col.getName().equals("SUID"))
				continue;
			if (Number.class.isAssignableFrom(col.getType()))
				cols.add(col.getName());
		}
		return new ListSingleSelection<String>(cols);
	}
}
