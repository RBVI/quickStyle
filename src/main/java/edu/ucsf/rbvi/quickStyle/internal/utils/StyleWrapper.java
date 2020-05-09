package edu.ucsf.rbvi.quickStyle.internal.utils;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.color.Palette;
import org.cytoscape.view.model.ContinuousRange;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;

import org.cytoscape.work.util.ListSingleSelection;

public class StyleWrapper {

	final CyServiceRegistrar registrar;
	final VisualMappingFunctionFactory continuousFunctionFactory;
	final VisualStyleFactory factory;
	final VisualMappingManager vmm;
	VisualStyle style;
	boolean existingStyle = false;

	double MINIMUM_NODE_SIZE = 10.0;
	double MAXIMUM_NODE_SIZE = 60.0;
	double MINIMUM_EDGE_WIDTH = 1.0;
	double MAXIMUM_EDGE_WIDTH = 10.0;

	public StyleWrapper(final CyServiceRegistrar registrar) {
		this.registrar = registrar;
		factory = registrar.getService(VisualStyleFactory.class);
		continuousFunctionFactory = registrar.getService(VisualMappingFunctionFactory.class,"(mapping.type=continuous)"); 
		vmm = registrar.getService(VisualMappingManager.class); 
	}

	public VisualStyle createVisualStyle(String styleName) {
		if (styleName == null || styleName.equals("")) {
			style = vmm.getCurrentVisualStyle();
			existingStyle = true;
		} else {
			Set<VisualStyle> styles = vmm.getAllVisualStyles();
			for (VisualStyle st: styles) {
				if (st.getTitle().equals(styleName)) {
					style = st;
					existingStyle = true;
					return style;
				}
			}
			style = factory.createVisualStyle(styleName);
		}
		return style;
	}

	public void createMapping(CyTable table, VisualProperty<Paint> property, 
	                          ListSingleSelection<String> tunable, Palette palette) {

		if (tunable == null) return;
		String column = tunable.getSelectedValue();
		if (column == "") return;

		Class<?> type = table.getColumn(column).getType();

		ContinuousMapping mapping = (ContinuousMapping)
						continuousFunctionFactory.createVisualMappingFunction(column, type, property);

		// Get the range of the values
		ContinuousRange<Double> range = getRange(table, column, type);

		Color[] colors = palette.getColors(9);

		// Two-tailed or one-tailed?
		Double min = range.getMin();
		Double max = range.getMax();
		if (min < 0.0 && max > 0.0) {
			// two-tailed
			max = Math.max(max, -min);
			min = -max;

			addPoint(mapping, type, min, new BoundaryRangeValues<Paint>(colors[8], colors[7], colors[7]));
			addPoint(mapping, type, 0.0, new BoundaryRangeValues<Paint>(colors[5], colors[5], colors[5]));
			addPoint(mapping, type, max, new BoundaryRangeValues<Paint>(colors[1], colors[1], colors[0]));
		} else if (min < 0.0) {
			// One-tailed -- all negative
			addPoint(mapping, type, min, new BoundaryRangeValues<Paint>(colors[8], colors[7], colors[7]));
			addPoint(mapping, type, 0.0, new BoundaryRangeValues<Paint>(colors[5], colors[5], colors[5]));
		} else {
			// One-tailed -- all positive
			addPoint(mapping, type, 0.0, new BoundaryRangeValues<Paint>(colors[5], colors[5], colors[5]));
			addPoint(mapping, type, max, new BoundaryRangeValues<Paint>(colors[1], colors[1], colors[0]));
		}
		style.addVisualMappingFunction(mapping);
	}

	public void createMapping(CyTable table, VisualProperty<Double> property, 
	                          ListSingleSelection<String> tunable) {
		if (tunable == null) return;
		String column = tunable.getSelectedValue();
		if (column == "") return;


		Class<?> type = table.getColumn(column).getType();

		ContinuousMapping mapping = (ContinuousMapping)
						continuousFunctionFactory.createVisualMappingFunction(column, type, property);

		// Get the range of the values
		ContinuousRange<Double> range = getRange(table, column, type);

		Double min = range.getMin();
		Double max = range.getMax();
		if (min < 0.0)
			max = max - min;

		if (property.equals(BasicVisualLexicon.NODE_SIZE)) {
			addPoint(mapping, type, 0.0, 
			         new BoundaryRangeValues<Double>(MINIMUM_NODE_SIZE, MINIMUM_NODE_SIZE, MINIMUM_NODE_SIZE));
			addPoint(mapping, type, max, 
			         new BoundaryRangeValues<Double>(MAXIMUM_NODE_SIZE, MAXIMUM_NODE_SIZE, MAXIMUM_NODE_SIZE));
		} else {
			addPoint(mapping, type, 0.0, 
			         new BoundaryRangeValues<Double>(MINIMUM_EDGE_WIDTH, MINIMUM_EDGE_WIDTH, MINIMUM_EDGE_WIDTH));
			addPoint(mapping, type, max, 
			         new BoundaryRangeValues<Double>(MAXIMUM_EDGE_WIDTH, MAXIMUM_EDGE_WIDTH, MAXIMUM_EDGE_WIDTH));
		}

		style.addVisualMappingFunction(mapping);

	}

	public void applyStyle(CyNetwork network) {
		CyNetworkViewManager viewManager = registrar.getService(CyNetworkViewManager.class);
		if (!existingStyle) {
			vmm.addVisualStyle(style);
		}
		vmm.setCurrentVisualStyle(style);

		for (CyNetworkView view: viewManager.getNetworkViews(network)) {
			style.apply(view);
		}
	}

	private void addPoint(ContinuousMapping mapping, Class<?> type, Double point, BoundaryRangeValues<?> brv) {
		if (type.equals(Double.class))
			mapping.addPoint(point, brv);
		else if (type.equals(Integer.class))
			mapping.addPoint(point.intValue(), brv);
		else if (type.equals(Float.class))
			mapping.addPoint(point.floatValue(), brv);
	}

	private ContinuousRange<Double> getRange(CyTable table, String column, Class<?> type) {
		Double max = Double.MIN_VALUE;
		Double min = Double.MAX_VALUE;
		for (Object obj: table.getColumn(column).getValues(type)) {
			Number val;
			if (obj == null) continue;
			if (obj instanceof Number)
				val = (Number)obj;
			else
				continue;

			if (val.doubleValue() > max) max = val.doubleValue();
			if (val.doubleValue() < min) min = val.doubleValue();
		}
		return new ContinuousRange<Double>(Double.class, min, max, true, true);
	}
}

