package edu.ucsf.rbvi.quickStyle.internal.tasks;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProvider;
import org.cytoscape.util.color.PaletteProviderManager;
import org.cytoscape.util.swing.CyColorPaletteChooser;
import org.cytoscape.util.swing.CyColorPaletteChooserFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.swing.RequestsUIHelper;
import org.cytoscape.work.swing.TunableUIHelper;
import org.cytoscape.work.swing.util.UserAction;
import org.cytoscape.work.util.ListSingleSelection;

import org.cytoscape.service.util.CyServiceRegistrar;

import edu.ucsf.rbvi.quickStyle.internal.model.ColorTunable;
import edu.ucsf.rbvi.quickStyle.internal.model.SizeTunable;
import edu.ucsf.rbvi.quickStyle.internal.utils.ModelUtils;
import edu.ucsf.rbvi.quickStyle.internal.utils.StyleWrapper;

public class QuickStyleTask extends AbstractTask implements ObservableTask, RequestsUIHelper {
	final CyServiceRegistrar registrar;
	final CyColorPaletteChooserFactory chooserFactory;
	protected TunableUIHelper tunableUIHelper;
	final Palette defaultPalette;

	@Tunable(description="Name of the style", tooltip="If blank, use the current style")
	public String styleName = "";

	// @Tunable(description="Network", context="nogui")
	public CyNetwork network = null;

	@ContainsTunables
	public NodeSizeTunable nodeSizeTunable = null;

	@ContainsTunables
	public EdgeWidthTunable edgeWidthTunable = null;

	@ContainsTunables
	public NodeColorTunable nodeColorTunable = null;

	@ContainsTunables
	public EdgeColorTunable edgeColorTunable = null;

	Map<String, Palette> paletteMap;

	public QuickStyleTask(final CyServiceRegistrar registrar, CyNetwork net,
	                      List<String> nodeColorColumns, 
	                      List<String> edgeColorColumns, List<String> nodeSizeColumns, 
	                      List<String> edgeWidthColumns) {
		this.registrar = registrar;
		network = net;
		if (net == null)
			network = ((CyApplicationManager)registrar.getService(CyApplicationManager.class)).getCurrentNetwork();

		chooserFactory = registrar.getService(CyColorPaletteChooserFactory.class);

		paletteMap = new HashMap<>();
		PaletteProviderManager ppm = registrar.getService(PaletteProviderManager.class);
		List<PaletteProvider> providers = ppm.getPaletteProviders(BrewerType.DIVERGING, false);
		List<String> palettes = new ArrayList<>();
		for (var provider: providers) {
			String providerName = provider.getProviderName();
			List<String> paletteNames = provider.listPaletteNames(BrewerType.DIVERGING, false);
			for (String name: paletteNames) {
				String title = providerName+" "+name;
				paletteMap.put(title, provider.getPalette(name));
				palettes.add(title);
			}
		}

		defaultPalette = paletteMap.get("ColorBrewer Red-Blue");

		ListSingleSelection<String> nodesize = 
			ModelUtils.getNumericNodeColumns(network, nodeSizeColumns);
		if (nodesize != null) {
			nodeSizeTunable = new NodeSizeTunable(nodesize);
		}

		ListSingleSelection<String> nodecolor = 
			ModelUtils.getNumericNodeColumns(network, nodeColorColumns);
		if (nodecolor != null) {
			nodeColorTunable = new NodeColorTunable(nodecolor, palettes);
		}

		ListSingleSelection<String> edgewidth = 
			ModelUtils.getNumericEdgeColumns(network, edgeWidthColumns);
		if (edgewidth != null) {
			edgeWidthTunable = new EdgeWidthTunable(edgewidth);
		}

		ListSingleSelection<String> edgecolor = 
			ModelUtils.getNumericEdgeColumns(network, edgeColorColumns);
		if (edgecolor != null) {
			edgeColorTunable = new EdgeColorTunable(edgecolor, palettes);
		}

	}

	public QuickStyleTask(final CyServiceRegistrar registrar, CyNetwork net) {
		this(registrar, net, null, null, null, null);
	}

	public QuickStyleTask(final CyServiceRegistrar registrar) {
		this(registrar, null, null, null, null, null);
	}

	public void run(TaskMonitor monitor) {
		CyTable nodeTable = network.getDefaultNodeTable();
		CyTable edgeTable = network.getDefaultEdgeTable();

		// Create the new style
		StyleWrapper style = new StyleWrapper(registrar);
		style.createVisualStyle(styleName);

		// Create the mappings
		style.createMapping(nodeTable, BasicVisualLexicon.NODE_FILL_COLOR, 
		                    (ColorTunable)nodeColorTunable);
		style.createMapping(nodeTable, BasicVisualLexicon.NODE_SIZE, 
		                    (SizeTunable)nodeSizeTunable);
		style.createMapping(edgeTable, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, 
		                    (ColorTunable)edgeColorTunable);
		style.createMapping(edgeTable, BasicVisualLexicon.EDGE_WIDTH, 
		                    (SizeTunable)edgeWidthTunable);
		// Add style to view
		style.applyStyle(network);
	}

	Palette getPalette(String palette) {
		return paletteMap.get(palette);
	}

	public void setUIHelper(TunableUIHelper helper) {
		tunableUIHelper = helper;
	}

	@SuppressWarnings("unchecked")
	public <R> R getResults(Class<? extends R> type) {
		if (type.equals(String.class)) {
			String response = "Style: "+styleName+"\n";
			return (R)response;
		} else if (type.equals(JSONResult.class)) {
			return (R)new JSONResult() {
				public String getJSON() { return "{\"style\":\""+styleName+"\"}"; }
			};
		}
		return (R)styleName;
	}

	public List<Class<?>> getResultClasses() {
		return Arrays.asList(JSONResult.class, String.class);
	}

	public class NodeSizeTunable implements SizeTunable {
		@Tunable(description="Column for node size", groups={"Node Style"})
		public ListSingleSelection<String> nodeSizeColumn = null;

		public NodeSizeTunable(ListSingleSelection<String> nodeSize) {
			this.nodeSizeColumn = nodeSize;
		}

		public String getValue() {
			if (nodeSizeColumn != null) 
				return nodeSizeColumn.getSelectedValue();
			return null;
		}
	}

	public class EdgeWidthTunable implements SizeTunable {
		@Tunable(description="Column for edge width", groups={"Edge Style"})
		public ListSingleSelection<String> edgeWidthColumn = null;

		public EdgeWidthTunable(ListSingleSelection<String> edgeSize) {
			this.edgeWidthColumn = edgeSize;
		}

		public String getValue() {
			if (edgeWidthColumn != null) 
				return edgeWidthColumn.getSelectedValue();
			return null;
		}
	}

	public class NodeColorTunable implements ColorTunable, ActionListener {
		@Tunable(description="Column for node color", groups={"Node Style"})
		public ListSingleSelection<String> nodeColorColumn = null;

		@Tunable(description="Node color palette", groups={"Node Style"}, context="nogui")
		public ListSingleSelection<String> nodeColorPalette = null;

		@Tunable(description="Color palette", groups={"Node Style"}, context="gui")
		public UserAction colorPalette = null;

		public NodeColorTunable(ListSingleSelection<String> nodeColor, List<String> palettes) {
			this.nodeColorColumn = nodeColor;
			this.nodeColorPalette = new ListSingleSelection<String>(palettes);
			this.colorPalette = new UserAction(this);
		}

		public String getValue() {
			if (nodeColorColumn != null)
				return nodeColorColumn.getSelectedValue();
			return null;
		}

		public Palette getPalette() {
			return paletteMap.get(nodeColorPalette.getSelectedValue());
		}

		public void actionPerformed(ActionEvent e) {
			// Get the color palette chooser
			CyColorPaletteChooser chooser = 
							chooserFactory.getColorPaletteChooser(BrewerType.DIVERGING, true);

			Palette palette = 
							chooser.showDialog(tunableUIHelper.getParent(), 
			                           "Choose Color Palette", defaultPalette, 9);
			nodeColorPalette.setSelectedValue(palette.getPaletteProvider().getProviderName()+" "+
			                                  palette.getName());
		}
	}
	
	public class EdgeColorTunable implements ColorTunable, ActionListener {
		@Tunable(description="Column for edge color", groups={"Edge Style"})
		public ListSingleSelection<String> edgeColorColumn = null;

		@Tunable(description="Node color palette", groups={"Edge Style"}, context="nogui")
		public ListSingleSelection<String> edgeColorPalette = null;

		@Tunable(description="Color palette", groups={"Edge Style"}, context="gui")
		public UserAction colorPalette = null;

		public EdgeColorTunable(ListSingleSelection<String> edgeColor, List<String> palettes) {
			this.edgeColorColumn = edgeColor;
			this.edgeColorPalette = new ListSingleSelection<String>(palettes);
			this.colorPalette = new UserAction(this);
		}

		public String getValue() {
			if (edgeColorColumn != null)
				return edgeColorColumn.getSelectedValue();
			return null;
		}

		public Palette getPalette() {
			return paletteMap.get(edgeColorPalette.getSelectedValue());
		}

		public void actionPerformed(ActionEvent e) {
			// Get the color palette chooser
			CyColorPaletteChooser chooser = 
							chooserFactory.getColorPaletteChooser(BrewerType.DIVERGING, true);

			Palette palette = 
							chooser.showDialog(tunableUIHelper.getParent(), 
			                           "Choose Color Palette", defaultPalette, 9);
			edgeColorPalette.setSelectedValue(palette.getPaletteProvider().getProviderName()+" "+
			                                  palette.getName());
		}
	}
}
