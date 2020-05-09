package edu.ucsf.rbvi.quickStyle.internal.tasks;

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
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.util.ListSingleSelection;

import org.cytoscape.service.util.CyServiceRegistrar;

import edu.ucsf.rbvi.quickStyle.internal.utils.ModelUtils;
import edu.ucsf.rbvi.quickStyle.internal.utils.StyleWrapper;

public class QuickStyleTask extends AbstractTask implements ObservableTask {
	final CyServiceRegistrar registrar;

	@Tunable(description="Name of the style", tooltip="If blank, use the current style")
	public String styleName = "";

	// @Tunable(description="Network", context="nogui")
	public CyNetwork network = null;

	@Tunable(description="Column for node size", groups={"Node Style"})
	public ListSingleSelection<String> nodeSizeColumn = null;

	@Tunable(description="Column for node color", groups={"Node Style"})
	public ListSingleSelection<String> nodeColorColumn = null;

	@Tunable(description="Node color palette", groups={"Node Style"})
	public ListSingleSelection<String> nodeColorPalette = null;

	@Tunable(description="Column for edge width", groups={"Edge Style"})
	public ListSingleSelection<String> edgeWidthColumn = null;

	@Tunable(description="Edge color column", groups={"Edge Style"})
	public ListSingleSelection<String> edgeColorColumn = null;

	@Tunable(description="Edge color palette", groups={"Edge Style"})
	public ListSingleSelection<String> edgeColorPalette = null;

	Map<String, Palette> paletteMap;

	public QuickStyleTask(final CyServiceRegistrar registrar, CyNetwork net,
	                      List<String> nodeColorColumns, 
	                      List<String> edgeColorColumns, List<String> nodeSizeColumns, 
	                      List<String> edgeWidthColumns) {
		this.registrar = registrar;
		network = net;
		if (net == null)
			network = ((CyApplicationManager)registrar.getService(CyApplicationManager.class)).getCurrentNetwork();

		nodeSizeColumn = ModelUtils.getNumericNodeColumns(network, nodeSizeColumns);
		nodeColorColumn = ModelUtils.getNumericNodeColumns(network, nodeColorColumns);
		edgeColorColumn = ModelUtils.getNumericEdgeColumns(network, edgeColorColumns);
		edgeWidthColumn = ModelUtils.getNumericEdgeColumns(network, edgeWidthColumns);

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
		nodeColorPalette = new ListSingleSelection<>(palettes);
		edgeColorPalette = new ListSingleSelection<>(palettes);
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
		style.createMapping(nodeTable, BasicVisualLexicon.NODE_FILL_COLOR, nodeColorColumn, 
		                    getPalette(nodeColorPalette));
		style.createMapping(nodeTable, BasicVisualLexicon.NODE_SIZE, nodeSizeColumn);
		style.createMapping(edgeTable, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, 
		                    edgeColorColumn, getPalette(edgeColorPalette));
		style.createMapping(edgeTable, BasicVisualLexicon.EDGE_WIDTH, edgeWidthColumn);
		// Add style to view
		style.applyStyle(network);
	}

	Palette getPalette(ListSingleSelection<String> palette) {
		return paletteMap.get(palette.getSelectedValue());
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
}
