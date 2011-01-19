package fiji.plugin.trackmate.visualization.trackscheme;

import static fiji.plugin.trackmate.visualization.trackscheme.TrackSchemeFrame.DEFAULT_CELL_HEIGHT;
import static fiji.plugin.trackmate.visualization.trackscheme.TrackSchemeFrame.DEFAULT_CELL_WIDTH;
import static fiji.plugin.trackmate.visualization.trackscheme.TrackSchemeFrame.X_COLUMN_SIZE;
import static fiji.plugin.trackmate.visualization.trackscheme.TrackSchemeFrame.Y_COLUMN_SIZE;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jfree.chart.renderer.InterpolatePaintScale;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.traverse.DepthFirstIterator;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;

import fiji.plugin.trackmate.Feature;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotImp;

public class JGraphTimeLayout implements JGraphLayout {

	
	
	private UndirectedGraph<Spot, DefaultWeightedEdge> graph;
	private List<Set<Spot>> tracks;
	private JGraphModelAdapter<Spot, DefaultWeightedEdge> adapter;
	private int[] columnWidths;
	protected InterpolatePaintScale colorMap = InterpolatePaintScale.Jet;
	/*
	 * CONSTRUCTOR
	 */
	

	public JGraphTimeLayout(UndirectedGraph<Spot, DefaultWeightedEdge> graph, JGraphModelAdapter<Spot, DefaultWeightedEdge> adapter) {
		this.graph = graph;
		this.adapter = adapter;
		this.tracks = new ConnectivityInspector<Spot, DefaultWeightedEdge>(graph).connectedSets();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run(JGraphFacade graphFacade) {
		
		HashMap<Set<Spot>, Color> trackColors = new HashMap<Set<Spot>, Color>(tracks.size());
		int counter = 0;
		int ntracks = tracks.size();
		for(Set<Spot> track : tracks) {
			trackColors.put(track, colorMap.getPaint((float) counter / (ntracks-1)));
			counter++;
		}
				
		SortedSet<Float> instants = new TreeSet<Float>();
		for (Spot s : graph.vertexSet())
			instants.add(s.getFeature(Feature.POSITION_T));
			
		TreeMap<Float, Integer> columns = new TreeMap<Float, Integer>();
		for(Float instant : instants)
			columns.put(instant, -1);
		
		TreeMap<Float, Integer> rows = new TreeMap<Float, Integer>();
		Iterator<Float> it = instants.iterator();
		int rowIndex = 1; // Start at 1 to let room for column headers
		while (it.hasNext()) {
			rows.put(it.next(), rowIndex);
			rowIndex++;
		}

		int currentColumn = 1;
		int previousColumn = 0;
		Spot previousSpot = null;
		int columnIndex = 0;
		columnWidths = new int[tracks.size()];
		Color trackColor = null;
		
		
		for (Set<Spot> track : tracks) {
			
			// Get track color
			trackColor = trackColors.get(track);
			
			// Sort by ascending order
			SortedSet<Spot> sortedTrack = new TreeSet<Spot>(SpotImp.frameComparator);
			sortedTrack.addAll(track);
			Spot root = sortedTrack.first();
			
			DepthFirstIterator<Spot, DefaultWeightedEdge> iterator = new DepthFirstIterator<Spot, DefaultWeightedEdge>(graph, root);
			while (iterator.hasNext()) {
				Spot spot = iterator.next();
				
				// Determine in what column to put the spot
				Float instant = spot.getFeature(Feature.POSITION_T);
				int freeColumn = columns.get(instant) + 1;
				
				// If we have no direct edge with the previous spot, we add 1 to the current column
				if (!graph.containsEdge(spot, previousSpot))
					currentColumn = currentColumn + 1;
				previousSpot = spot;
				
				int targetColumn = Math.max(freeColumn, currentColumn);
				currentColumn = targetColumn;
				
				// Keep track of column filling
				columns.put(instant, targetColumn);
				
				// Get corresponding JGraph cell 
				Object facadeTarget = adapter.getVertexCell(spot);
				SpotView vView = (SpotView) graphFacade.getCellView(facadeTarget);
				
				// Tune aspect of cell according to context
				vView.setColor(trackColor);
				
				// Move the corresponding cell in the facade
				graphFacade.setLocation(facadeTarget, ( targetColumn) * X_COLUMN_SIZE - DEFAULT_CELL_WIDTH/2, (0.5 + rows.get(instant)) * Y_COLUMN_SIZE - DEFAULT_CELL_HEIGHT/2);
				int height = Math.min(DEFAULT_CELL_WIDTH, spot.getIcon().getIconHeight());
				graphFacade.setSize(facadeTarget, DEFAULT_CELL_WIDTH, height);
				
				Object[] objEdges = graphFacade.getEdges(facadeTarget);
				for(Object obj : objEdges) {
//					org.jgraph.graph.DefaultWeightedEdge edge = (org.jgraph.graph.DefaultWeightedEdge) obj;
					EdgeView eView = (EdgeView) graphFacade.getCellView(obj);
					eView.getAttributes().put(GraphConstants.LINECOLOR, trackColor);
				}
			}
		
			for(Float instant : instants)
				columns.put(instant, currentColumn+1);
			
			columnWidths[columnIndex] = currentColumn - previousColumn + 1;
			columnIndex++;
			previousColumn = currentColumn;			
			
		}  // loop over tracks
		
	}

	/**
	 * Return the width in column units of each track after they are arranged by this GraphLayout.
	 */
	public int[] getTrackColumnWidths() {
		return columnWidths;
	}

}
