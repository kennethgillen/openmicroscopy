/*
 * org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewerUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.measurement.view;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries
import org.jhotdraw.draw.DelegationSelectionTool;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.Figure;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.iviewer.ImageViewport;
import org.openmicroscopy.shoola.agents.events.measurement.SelectPlane;
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.actions.MeasurementViewerAction;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ROIResult;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.ShapeList;
import org.openmicroscopy.shoola.util.ui.LoadingWindow;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.drawingtools.canvas.DrawingCanvasView;

/** 
 * The {@link MeasurementViewer} view.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class MeasurementViewerUI 
	extends TopWindow 
{

	/** The message displayed when a ROI cannot be retrieved. */
	static final String					RETRIEVE_MSG = "Cannot retrieve the " +
															"ROI";
	
	/** The message displayed when a ROI cannot be created. */
	static final String					CREATE_MSG = "Cannot create the ROI";

	/** The message displayed when a ROI cannot be deleted. */
	static final String					DELETE_MSG = "Cannot delete the ROI";
	
	/** 
	 * The message displayed when a an ROI exception occurred but cause 
	 * is unknown. 
	 */
	static final String					UNKNOWN_MSG = "An unknown, " +
										"unexpected error occurred in ";
	
	/** The default message. */
	static final String					DEFAULT_MSG = "";
	
	/** The default size of the window. */
	private static final Dimension		DEFAULT_SIZE = new Dimension(400, 300);
	
	/** The maximum size of the window. */
	private static final Dimension		MAXIMUM_SIZE = new Dimension(700, 300);
	
	/** The title for the measurement tool main window. */
	private static final String			WINDOW_TITLE = "Measurement Tool ";
	
	/** index to identify inspector tab. */
	public static final int				INSPECTOR_INDEX = 0;

	/** index to identify manager tab. */
	public static final int				MANAGER_INDEX = 1;
	
	/** index to identify results tab. */
	public static final int				RESULTS_INDEX = 2;
	
	/** index to identify graph tab. */
	public static final int				GRAPH_INDEX = 3;

	/** index to identify intensity tab. */
	public static final int				INTENSITY_INDEX = 4;
		
	/** index to identify calculation tab. */
	public static final int				CALCWIZARD_INDEX = 5;
	
	/** index to identify intensity results view tab. */
	public static final int				INTENSITYRESULTVIEW_INDEX = 6;
	
	/** Reference to the Model. */
	private MeasurementViewerModel 		model;

	/** Reference to the Control. */
	private MeasurementViewerControl	controller;
	
	/** Reference to the Component. */
	private MeasurementViewer			component;
	
	 /** The loading window. */
    private LoadingWindow   			loadingWindow;
    
	/** The tool bar. */
	private ToolBar						toolBar;
	
	/** The ROI inspector. */
	private ObjectInspector				roiInspector;
	
	/** The ROI manager. */
	private ObjectManager				roiManager;
	
	/** The Results component. */
	private MeasurementResults			roiResults;
	
	/** The graphing component. */
	private GraphPane					graphPane;
	
	/** The graphing component. */
	private IntensityView				intensityView;

	/** The graphing component. */
	private IntensityResultsView	 	intensityResultsView;
	
	/** The calculation Wizard component. */
	private CalculationWizard			calcWizard;
	
    /** Tab pane hosting the various panel. */
    private JTabbedPane					tabs;
 
    /** The status bar. */
    private StatusBar					statusBar;

    /** the creation option to create multiple figures in the UI. */
    private JCheckBoxMenuItem 			createMultipleFigure;
    
    /** the creation option to create single figures in the UI. */
    private JCheckBoxMenuItem 			createSingleFigure;
    
    /** The collection of components displaying the tables. */
    private List<ServerROITable>		roiTables;

    /**
     * Scrolls to the passed figure.
     * 
     * @param figure The figure to handle.
     */
    private void scrollToFigure(ROIFigure figure)
    {
    	EventBus bus = MeasurementAgent.getRegistry().getEventBus();
    	bus.post(new ImageViewport(model.getImageID(), model.getPixelsID(),
    			figure.getBounds().getBounds()));
    }
    
    /** 
     * Creates the menu bar.
     * 
     * @return The menu bar. 
     */
    private JMenuBar createMenuBar()
    {
    	JMenuBar menuBar = new JMenuBar(); 
    	menuBar.add(createControlsMenu());
    	menuBar.add(createOptionsMenu());
        return menuBar;
    }
    
    /**
     * Helper method to create the controls menu.
     * 
     * @return The controls sub-menu.
     */
    private JMenu createControlsMenu()
    {
        JMenu menu = new JMenu("Controls");
        menu.setMnemonic(KeyEvent.VK_C);
        MeasurementViewerAction a = 
        	controller.getAction(MeasurementViewerControl.LOAD);
        JMenuItem item = new JMenuItem(a);
        item.setText(a.getName());
        menu.add(item);
        a = controller.getAction(MeasurementViewerControl.SAVE);
        item = new JMenuItem(a);
        item.setText(a.getName());
        menu.add(item);
        a = controller.getAction(MeasurementViewerControl.ROI_ASSISTANT);
        item = new JMenuItem(a);
        item.setText(a.getName());
        menu.add(item);
        return menu;
    }
    
    /**
     * Helper method to create the Options menu.
     * 
     * @return The options sub-menu.
     */
    private JMenu createOptionsMenu()
    {
        JMenu menu = new JMenu("Options");
        ButtonGroup displayUnits = new ButtonGroup();
    	
        menu.setMnemonic(KeyEvent.VK_O);
        
        JMenu subMenu = new JMenu("Units");
        MeasurementViewerAction a = controller.getAction(
    			MeasurementViewerControl.IN_MICRONS);
        JCheckBoxMenuItem inMicronsMenu = new JCheckBoxMenuItem(a);
        inMicronsMenu.setText(a.getName());
        displayUnits.add(inMicronsMenu);
        subMenu.add(inMicronsMenu);
        
        a = controller.getAction(MeasurementViewerControl.IN_PIXELS);
        JCheckBoxMenuItem inPixelsMenu = new JCheckBoxMenuItem(a);
        inPixelsMenu.setText(a.getName());
        displayUnits.add(inPixelsMenu);
        subMenu.add(inPixelsMenu);
        inPixelsMenu.setSelected(true); //TODO: retrieve info
        
        menu.add(subMenu);
        
        ButtonGroup createFigureGroup = new ButtonGroup();
    	JMenu creationMenu = new JMenu("ROI Creation");
        a = controller.getAction(
    			MeasurementViewerControl.CREATESINGLEFIGURE);
        createSingleFigure = new JCheckBoxMenuItem(a);
        createSingleFigure.setText(a.getName());
        createFigureGroup.add(createSingleFigure);
        creationMenu.add(createSingleFigure);
        
        a = controller.getAction(
        		MeasurementViewerControl.CREATEMULTIPLEFIGURE);
        createMultipleFigure = new JCheckBoxMenuItem(a);
        createMultipleFigure.setText(a.getName());
        createFigureGroup.add(createMultipleFigure);
        creationMenu.add(createMultipleFigure);
        createMultipleFigure.setSelected(true); //TODO: retrieve info
        menu.add(creationMenu);
        return menu;
    }
    
    
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		roiTables = new ArrayList<ServerROITable>();
		statusBar = new StatusBar();
		toolBar = new ToolBar(component, this, controller, model);
		roiManager = new ObjectManager(this, model);
		roiInspector = new ObjectInspector(controller, model);
		roiResults = new MeasurementResults(controller, model, this);
		graphPane = new GraphPane(this, controller, model);
		intensityView = new IntensityView(this, model);
		intensityResultsView = new IntensityResultsView(this, model);
		calcWizard = new CalculationWizard(controller, model);
		tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		DrawingCanvasView canvasView = model.getDrawingView();
		canvasView.addMouseListener(new MouseAdapter() {
		    /**
		     * Sets the cursor.
			 * @see MouseListener#mouseEntered(java.awt.event.MouseEvent)
			 */
			public void mouseEntered(MouseEvent e)
			{
				Cursor cursor;
				if (model.getDrawingEditor().getTool() instanceof 
					DelegationSelectionTool)
					cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
				else
					cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
				getDrawingView().setCursor(cursor);
			}
			
		});
		
        tabs.setAlignmentX(LEFT_ALIGNMENT);
        tabs.addChangeListener(new ChangeListener()
		{
			// This method is called whenever the selected tab changes
			public void stateChanged(ChangeEvent evt)
			{
				if (model.isServerROI()) {
					updateDrawingArea();
				} else {
					if (inDataView())
						controller.analyseSelectedFigures();
				}
			}
		});
	}

	/** Builds and lays out the GUI. */
	private void buildGUI()
	{
		setJMenuBar(createMenuBar());
		tabs.addTab(roiManager.getComponentName(), 
					roiManager.getComponentIcon(), roiManager);
		tabs.addTab(roiInspector.getComponentName(), 
			roiInspector.getComponentIcon(), roiInspector);
		tabs.addTab(roiResults.getComponentName(), 
			roiResults.getComponentIcon(), roiResults);
		tabs.addTab(graphPane.getComponentName(), 
			graphPane.getComponentIcon(), graphPane);
		tabs.addTab(intensityView.getComponentName(), 
			intensityView.getComponentIcon(), intensityView);
		tabs.addTab(intensityResultsView.getComponentName(), 
			intensityResultsView.getComponentIcon(), intensityResultsView);
		Container container = getContentPane();
		container.setLayout(new BorderLayout(0, 0));
		container.add(toolBar, BorderLayout.NORTH);
		container.add(tabs, BorderLayout.CENTER);
		container.add(statusBar, BorderLayout.SOUTH);
	}
	
	/**
     * Creates a new instance.
     * The 
     * {@link #initialize(MeasurementViewer, MeasurementViewerControl,
     *  MeasurementViewerModel) initialize}
     * method should be called straight 
     * after to link this View to the Controller.
     * 
     * @param title The window title.
     */
	MeasurementViewerUI(String title)
    {
        super(WINDOW_TITLE+title);
        IconManager icons = IconManager.getInstance();
        setIconImage(icons.getImageIcon(IconManager.MEASUREMENT_TOOL).getImage());
        loadingWindow = new LoadingWindow(this);
    }
    
	/**
	 * Links this View to its Controller and Model.
	 * 
	 * @param component    Reference to the Component.
	 *                      Mustn't be <code>null</code>.
	 * @param controller    Reference to the Control.
	 *                      Mustn't be <code>null</code>.
	 * @param model         Reference to the Model.
	 *                      Mustn't be <code>null</code>.
	 */
    void initialize(MeasurementViewer component, 
    			MeasurementViewerControl controller, 
    			MeasurementViewerModel model)
    {
        if (component == null) throw new NullPointerException("No control.");
        if (controller == null) throw new NullPointerException("No control.");
        if (model == null) throw new NullPointerException("No model.");
        this.component = component;
        this.model = model;
        this.controller = controller;
        controller.attachListeners();
        initComponents();
        buildGUI();
    }

	/**
	 * Merge the ROIShapes with ids in the idList and the ROIShapes selected 
	 * in the shapeList from those ROI.
	 * 
	 * @param idList see above.
	 * @param shapeList see above.
	 */
	void mergeROI(List<Long> idList, List<ROIShape> shapeList)
	{
		try
		{
			model.nofityDataChanged(true);
			ROI newROI = model.cloneROI(idList.get(0));
			for (ROIShape shape : shapeList)
			{
				ROIShape newShape = new ROIShape(newROI, shape.getCoord3D(), 
						shape);
				if (getDrawing().contains(shape.getFigure()))
				{
					shape.getFigure().removeFigureListener(controller);
					getDrawing().removeDrawingListener(controller);
					getDrawing().remove(shape.getFigure());
					getDrawing().addDrawingListener(controller);
				}
				model.deleteShape(shape.getID(), shape.getCoord3D());
				if (newShape.getCoord3D().equals(model.getCurrentView()))
				{
					getDrawing().removeDrawingListener(controller);
					getDrawing().add(newShape.getFigure());
					newShape.getFigure().addFigureListener(controller);
					getDrawing().addDrawingListener(controller);
				}
				model.addShape(newROI.getID(), newShape.getCoord3D(), newShape);
			}
		}
		catch (Exception e)
		{
			if (e instanceof ROICreationException)
				handleROIException(e, CREATE_MSG);
			else if (e instanceof NoSuchROIException)
				handleROIException(e, RETRIEVE_MSG);
			else handleROIException(e, UNKNOWN_MSG+"Merging ROI");
		}
		
	}
	
	/**
	 * Splits the ROIShapes from the ROI with id and the ROIShapes selected in 
	 * the shapeList from that ROI.
	 * 
	 * @param id see above.
	 * @param shapeList see above.
	 */
	void splitROI(long id, ArrayList<ROIShape> shapeList)
	{
		try
		{
			model.nofityDataChanged(true);
			ROI newROI = model.cloneROI(id);
			for(ROIShape shape : shapeList)
			{
				ROIShape newShape = new ROIShape(newROI, shape.getCoord3D(), 
						shape);
				if (getDrawing().contains(shape.getFigure()))
				{
					shape.getFigure().removeFigureListener(controller);
					getDrawing().removeDrawingListener(controller);
					getDrawing().remove(shape.getFigure());
					getDrawing().addDrawingListener(controller);
				}
				model.deleteShape(shape.getID(), shape.getCoord3D());
				if(newShape.getCoord3D().equals(model.getCurrentView()))
				{
					getDrawing().removeDrawingListener(controller);
					this.getDrawing().add(newShape.getFigure());
					newShape.getFigure().addFigureListener(controller);
					getDrawing().addDrawingListener(controller);
				}
				model.addShape(newROI.getID(), newShape.getCoord3D(), newShape);
			}
		}
		catch (Exception e)
		{
			if(e instanceof ROICreationException)
				handleROIException(e, CREATE_MSG);
			else if(e instanceof NoSuchROIException)
				handleROIException(e, RETRIEVE_MSG);
			else 
				handleROIException(e, UNKNOWN_MSG+"Splitting ROI.");
		}
			
	}
	
	/**
	 * Duplicate the ROI with id and the ROIShapes selected in the shapeList 
	 * from that ROI.
	 * @param id see above.
	 * @param shapeList see above.
	 */
	void duplicateROI(long id, ArrayList<ROIShape> shapeList)
	{
		try
		{
			model.nofityDataChanged(true);
			ROI newROI = model.cloneROI(id);
			for(ROIShape shape : shapeList)
			{
				ROIShape newShape = new ROIShape(newROI, shape.getCoord3D(), 
						shape);
				if (newShape.getCoord3D().equals(model.getCurrentView()))
				{
					getDrawing().removeDrawingListener(controller);
					this.getDrawing().add(newShape.getFigure());
					newShape.getFigure().addFigureListener(controller);
					getDrawing().addDrawingListener(controller);
				}
				model.addShape(newROI.getID(), newShape.getCoord3D(), newShape);
			}
		}
		catch (Exception e)
		{
			handleROIException(e, CREATE_MSG);
		}
	}
	
	/**
	 * Deletes the ROI with id and the ROIShapes selected in the shapeList.
	 * 
	 * @param shapeList see above.
	 */
	void deleteROIShapes( ArrayList<ROIShape> shapeList)
	{
		try
		{
			model.nofityDataChanged(true);
			for (ROIShape shape : shapeList)
			{
				if (getDrawing().contains(shape.getFigure()))
				{
					shape.getFigure().removeFigureListener(controller);
					getDrawing().removeDrawingListener(controller);
					getDrawing().remove(shape.getFigure());
					getDrawing().addDrawingListener(controller);
				}
				model.deleteShape(shape.getID(), shape.getCoord3D());
			}
		} catch (Exception e) {
			handleROIException(e, DELETE_MSG);
		}
	}
	
    /**
	 * Returns <code>true</code> if in the graph or intensity view,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean inDataView()
	{
		return (inIntensityView() || inGraphView() || inCalcWizardView());
	}
	
	/**
	 * Returns <code>true</code> if in the calcWizard view,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean inCalcWizardView()
	{
		int index = tabs.getSelectedIndex();
		if (index < 0) return false;
		int n = tabs.getTabCount();
		if (index >= n) return false;
		return (tabs.getTitleAt(index).equals(calcWizard.getComponentName()));
	}
	
	/**
	 * Returns <code>true</code> if in the graph view,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean inGraphView()
	{
		int index = tabs.getSelectedIndex();
		if (index < 0) return false;
		int n = tabs.getTabCount();
		if (index >= n) return false;
		return (tabs.getTitleAt(index).equals(graphPane.getComponentName()));
	}
	
	/**
	 * Returns <code>true</code> if in the intensity view,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean inIntensityView()
	{
		int index = tabs.getSelectedIndex();
		if (index < 0) return false;
		int n = tabs.getTabCount();
		if (index >= n) return false;
		return (tabs.getTitleAt(index).equals(intensityView.getComponentName()));
	}
	

	/**
	 * Returns <code>true</code> if in the intensity Results view,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean inIntensityResultsView()
	{
		return (tabs.getTitleAt(tabs.getSelectedIndex()).
				equals(intensityResultsView.getComponentName()));
	}
	
    /**
     * Returns the {@link #loadingWindow}.
     * 
     * @return See above.
     */
    LoadingWindow getLoadingWindow() { return loadingWindow; }
    
    /**
	 * Sets the passed color to the currently selected cell.
	 * 
	 * @param color The color to set.
	 */
    void setCellColor(Color color)
    {
		if (roiInspector != null) roiInspector.setCellColor(color);
	}
    
    /**
     * Selects the current figure based on ROIid, t and z sections.
     * 
     * @param ROIid     The id of the selected ROI.
     * @param t 	The corresponding timepoint.
     * @param z 	The corresponding z-section.
     */
    void selectFigure(long ROIid, int t, int z)
    {
    	try {
    		ROI roi = model.getROI(ROIid);
    		ROIFigure fig = roi.getFigure(new Coord3D(z, t));
    		selectFigure(fig);
		} catch (Exception e) {
			handleROIException(e, RETRIEVE_MSG);
		}	
    }
    
    /** Displays the ROI assistant. */
	void showROIAssistant()
	{
		Registry reg = MeasurementAgent.getRegistry();
		UserNotifier un = reg.getUserNotifier();
		if (inDataView())
		{
			un.notifyInfo("ROI Assistant", "ROI Assistant cannot be used" +
					" in graph pane or intensity view.");
			return;
		}
		
		Collection<ROI> roiList = model.getSelectedROI();
		if (roiList.size() == 0)
		{
			un.notifyInfo("ROI Assistant", "Select a Figure to modify " +
			"using the ROI Assistant.");
			return;
		}
		if (roiList.size() > 1)
		{
			un.notifyInfo("ROI Assistant", "The ROI Assistant can" +
					"only be used on one ROI" +
			"at a time.");
			return;
		}
		ROI currentROI = roiList.iterator().next();
			
    	ROIAssistant assistant = new ROIAssistant(model.getNumTimePoints(), 
    		model.getNumZSections(), model.getCurrentView(), currentROI, this);
    	UIUtilities.setLocationRelativeToAndShow(this, assistant);
	}
	
	/**
	 * Displays the {@link ROIAssistant} for the passed ROI.
	 * 
	 * @param roi The ROI to handle.
	 */
	void showROIAssistant(ROI roi)
	{
		Registry reg = MeasurementAgent.getRegistry();
		UserNotifier un = reg.getUserNotifier();
		if (inDataView())
		{
			un.notifyInfo("ROI Assistant", "ROI Assistant cannot be used" +
					" in graph pane or intensity view");
			return;
		}

	  	ROIAssistant assistant = new ROIAssistant(model.getNumTimePoints(), 
    		model.getNumZSections(), model.getCurrentView(), roi, this);
    	UIUtilities.setLocationRelativeToAndShow(this, assistant);
	}
	
    /**
     * Selects the passed figure.
     * 
     * @param figure The figure to select.
     */
    void selectFigure(ROIFigure figure)
    {
    	if (figure == null) return;
    	Coord3D coord3D = figure.getROIShape().getCoord3D();
    	if (coord3D == null) return;
    	if (!coord3D.equals(model.getCurrentView())) {
    		model.setPlane(coord3D.getZSection(), coord3D.getTimePoint());
    		SelectPlane request = 
    			new SelectPlane(model.getPixelsID(), coord3D.getZSection(), 
    							coord3D.getTimePoint());
    		EventBus bus = MeasurementAgent.getRegistry().getEventBus();
    		bus.post(request);
    		updateDrawingArea();
    		//return;
    	}
    	
    	DrawingCanvasView dv = model.getDrawingView();
    	dv.clearSelection();
    	dv.addToSelection(figure);
		List<ROIShape> roiShapeList = new ArrayList<ROIShape>();
		roiShapeList.add(figure.getROIShape());
		dv.grabFocus();
		if (model.isServerROI()) {
			List<Long> ids = new ArrayList<Long>();
			Iterator<ROIShape> j = roiShapeList.iterator();
			ROIShape roiShape;
			while (j.hasNext()) {
				roiShape = (ROIShape) j.next();
				ids.add(roiShape.getROI().getID());
			}
			Component c = tabs.getSelectedComponent();
			if (c instanceof ServerROITable) {
				((ServerROITable) c).selectROI(ids);
			}
		} else {
			roiInspector.setSelectedFigures(roiShapeList);
			roiManager.setSelectedFigures(roiShapeList, false);
		}
    }
    
    /**
     * Sets the selected figures.
     * 
     * @param figures Collection of selected figures.
     */
    void setSelectedFigures(Collection figures)
    {
    	if (model.getState() != MeasurementViewer.READY) return;
		if (figures == null) return;
		Iterator i = figures.iterator();
		ROIFigure figure;
		List<ROIShape> shapeList = new ArrayList<ROIShape>();
		ROI roi;
		ROIShape shape;
		try {
			while (i.hasNext()) {
				figure = (ROIFigure) i.next();
				shape = figure.getROIShape();
				if (shape != null) shapeList.add(shape);
			}
		} catch (Exception e) {
			handleROIException(e, RETRIEVE_MSG);
		}
		if (model.isServerROI()) {
			List<Long> ids = new ArrayList<Long>();
			Iterator<ROIShape> j = shapeList.iterator();
			while (j.hasNext()) {
				shape = (ROIShape) j.next();
				ids.add(shape.getROI().getID());
			}
			Component c = tabs.getSelectedComponent();
			if (c instanceof ServerROITable) {
				((ServerROITable) c).selectROI(ids);
			}
		} else {
			roiInspector.setSelectedFigures(shapeList);
			roiManager.setSelectedFigures(shapeList, true);
		}
	}
    
    /**
     * Sets the figures selected from the table.
     * 
     * @param figures
     */
    void setTableSelectedFigure(List<ROIFigure> figures)
    {
    	DrawingCanvasView dv = model.getDrawingView();
    	Iterator<ROIFigure> k = figures.iterator();
    	ROIFigure figure;
    	dv.clearSelection();
    	if (figures == null || figures.size() == 0) return;
    	dv.removeFigureSelectionListener(controller);
    	int n = figures.size()-1;
    	int index = 0;
    	while (k.hasNext()) {
    		figure = k.next();
    		dv.addToSelection(figure);
    		if (index == n) {
    			scrollToFigure(figure);	
    		}
    		index++;
		}
    	dv.addFigureSelectionListener(controller);
		dv.grabFocus();
    }
    
    /**
     * Removes the specified figure from the display.
     * 
     * @param figure The figure to remove.
     */
    void removeROI(ROIFigure figure)
    {
    	if (figure == null) return;
    	try {
    		model.removeROIShape(figure.getROI().getID());
    		if (!model.isServerROI()) {
    			roiManager.removeFigure(figure);
    			roiResults.refreshResults();
    		}
		} catch (Exception e) {
			handleROIException(e, DELETE_MSG);
		}
    }
    
    /**
     * Adds the specified figure to the display.
     * 
     * @param figure The figure to add.
     */
    void addROI(ROIFigure figure)
    {
    	if (figure == null) return;
    	ROI roi = null;
    	try {
    		roi = model.createROI(figure,!getDrawingView().isDuplicate());
    		getDrawingView().unsetDuplicate();
    	} catch (Exception e) {
			handleROIException(e, CREATE_MSG);
		}
    	if (roi == null) return;
    	List<ROI> roiList = new ArrayList<ROI>();
    	roiList.add(roi);
    	if (!model.isServerROI()) {
    		roiManager.addFigures(roiList);
        	roiResults.refreshResults();
    	}
    }
    
    /**
     * Reacts to the changes of attributes for the specified figure.
     * 
     * @param figure The figure to handle.
     */
    void onAttributeChanged(Figure figure)
    {
    	if (model.getState() != MeasurementViewer.READY) return;
    	if (figure == null) return;
    	getDrawingView().repaint();
    	if (!model.isServerROI()) {
    		roiInspector.setModelData(figure);
        	//roiManager.update();
        	roiResults.refreshResults();
    	}
    }

    /**
     * Returns the drawing.
     * 
     * @return See above.
     */
    Drawing getDrawing() { return model.getDrawing(); }
    
    /**
     * Returns the drawing view.
     * 
     * @return See above.
     */
    DrawingCanvasView getDrawingView() { return model.getDrawingView(); }
    
    /** Rebuilds the ROI table. */
    void rebuildManagerTable()
    { 
    	if (!model.isServerROI()) roiManager.rebuildTable(); 
    }
    
    /** Rebuilds the results table. */
    void refreshResultsTable()
    { 
    	if (!model.isServerROI())
    		roiResults.refreshResults();
    }
    
    /** Rebuild the inspector table. */
    void refreshInspectorTable()
    { 
    	if (!model.isServerROI())
    		roiInspector.repaint();
    } 
    
    /** 
     * Saves the results table.
     * 
     * @throws IOException Thrown if the data cannot be written.
     * @return See above.
     */
    boolean saveResultsTable() 
    	throws IOException
    { 
    	return roiResults.saveResults(); 
    }
    
    /**
	 * Shows the results wizard and updates the fields based on the users 
	 * selection.
	 */
	void showResultsWizard() { roiResults.showResultsWizard(); }
    
    /**
     * Handles the exception thrown by the <code>ROIComponent</code>.
     * 
     * @param e 	The exception to handle.
     * @param text 	The message displayed in the status bar.
     */
    void handleROIException(Exception e, String text)
    {
    	Registry reg = MeasurementAgent.getRegistry();
    	if (e instanceof ROICreationException || 
    		e instanceof NoSuchROIException)
    	{
    		reg.getLogger().error(this, 
    						"Problem while handling ROI "+e.getMessage());
    		statusBar.setStatus(text);
    	} 
    	else 
    	{
    		String s = "An unexpected error occured while handling ROI ";
    		reg.getLogger().error(this, s+e.getMessage());
    		reg.getUserNotifier().notifyError("ROI", s, e);
    	}
    }
    
    /** Lays out the UI. */
    void layoutUI()
    {
    	if (model.isServerROI()) {
    		tabs.removeAll();
    		Collection l = model.getMeasurementResults();
    		Iterator i = l.iterator();
    		ROIResult result;
    		ServerROITable comp;
    		while (i.hasNext()) {
    			result = (ROIResult) i.next();
				comp = new ServerROITable(this, model);
				comp.setResult(result);
				roiTables.add(comp);
				tabs.addTab(comp.getComponentName(), comp.getComponentIcon(), 
						comp);
			}
    		if (l.size() > 0) tabs.setSelectedIndex(0);
    		getContentPane().remove(toolBar);
    		setJMenuBar(null);
    	}
    }
    
    /** Updates the drawing area. */
	void updateDrawingArea()
	{
		Drawing drawing = model.getDrawing();
		drawing.removeDrawingListener(controller);
		drawing.clear();
		ShapeList list = null;
		ROIFigure figure;
		if (model.isServerROI()) {
			Component comp = tabs.getSelectedComponent();
			if (comp instanceof ServerROITable) {
				ServerROITable table = (ServerROITable) comp;
				try {
					List<ROI> rois = model.getROIList(table.getFileID());
					Iterator<ROI> k = rois.iterator();
					ROI roi;
					TreeMap<Coord3D, ROIShape> shapes;
					Iterator<ROIShape> j;
					ROIShape shape;
					while (k.hasNext()) {
						roi = k.next();
						shapes = roi.getShapes();
						j = shapes.values().iterator();
						while (j.hasNext()) {
							shape = j.next();
							figure = shape.getFigure();
							drawing.add(figure);
							figure.addFigureListener(controller);
						}
					}
				} catch (Exception e) {
					handleROIException(e, RETRIEVE_MSG);
				}
			}
			DrawingCanvasView canvas = model.getDrawingView();
			KeyListener[] l = canvas.getKeyListeners();
			if (l != null) {
				for (int i = 0; i < l.length; i++)
					canvas.removeKeyListener(l[i]);
			}
		} else {
			try {
				list = model.getShapeList();
			} catch (Exception e) {
				handleROIException(e, RETRIEVE_MSG);
			}
			if (list != null) {
				TreeMap map = list.getList();
				Iterator i = map.values().iterator();
				ROIShape shape;
				while (i.hasNext()) {
					shape = (ROIShape) i.next();
					if (shape != null) 
					{
						figure = shape.getFigure();
						drawing.add(figure);
						figure.addFigureListener(controller);
					}
				}
			}
		}
		setStatus(DEFAULT_MSG);
		model.getDrawingView().setDrawing(drawing);
		drawing.addDrawingListener(controller);
	}
	
	/**
	 * Propagates the selected shape in the roi model. 
	 * 
	 * @param shape 	The ROIShape to propagate.
	 * @param timePoint The timepoint to propagate to.
	 * @param zSection 	The z-section to propagate to.
	 */
	void propagateShape(ROIShape shape, int timePoint, int zSection) 
	{
		List<ROIShape> addedShapes;
		try
		{
			addedShapes = model.propagateShape(shape, timePoint, zSection);
			ROIFigure figToDelete = null;
			ROIFigure roiFig;
			for (ROIShape newShape : addedShapes)
			{
				if (newShape.getCoord3D().equals(model.getCurrentView()))
				{
					getDrawing().removeDrawingListener(controller);
					figToDelete = null;
					for (Figure f : getDrawing().getFigures()) {
						roiFig = (ROIFigure) f;
						if (roiFig.getROI().getID() == newShape.getID())
							figToDelete = roiFig;
					}
					if (figToDelete!=null)
						getDrawing().remove(figToDelete);
					this.getDrawing().add(newShape.getFigure());
					newShape.getFigure().addFigureListener(controller);
					getDrawing().addDrawingListener(controller);
				}
				newShape.getFigure().calculateMeasurements();
			}
			if (!model.isServerROI()) roiManager.addROIShapes(addedShapes);
		}
		catch (ROICreationException e)
		{
			handleROIException(e, CREATE_MSG);
		}
		catch (NoSuchROIException e)
		{
			handleROIException(e, RETRIEVE_MSG);
		}
		setStatus(DEFAULT_MSG);
	}
	
	/**
	 * Deletes the selected shape from current coordinate to timepoint 
	 * and z-section.
	 *  
	 * @param shape 	The initial shape to delete.
	 * @param timePoint The timepoint to delete to.
	 * @param zSection 	The z-section to delete to.
	 */
	void deleteShape(ROIShape shape, int timePoint, int zSection) 
	{
		try 
		{
			model.deleteShape(shape, timePoint, zSection);
		} catch (Exception e) 
		{
			handleROIException(e, RETRIEVE_MSG);
		}
		setStatus(DEFAULT_MSG);
		rebuildManagerTable();
	}

	/**
	 * Sets a message in the status bar.
	 * 
	 * @param text The text to display.
	 */
	void setStatus(String text) { statusBar.setStatus(text); }
	
	/**
	 * Sets ready message in the status bar.
	 */
	void setReadyStatus() { setStatus(DEFAULT_MSG); }
	
	/** Builds the graphs and displays them in the results pane. */
	void displayAnalysisResults()
	{
		if (inGraphView()) graphPane.displayAnalysisResults();
		else if (inIntensityView()) intensityView.displayAnalysisResults();
		else if (inIntensityResultsView()) 
			intensityResultsView.displayAnalysisResults();
	}
	
	/**
     * Creates a single figure and returns to the selection tool.
     * 
     * @param createSingleFig See above.
     */
    void createSingleFigure(boolean createSingleFig)
    {
    	if (createSingleFig)
    	{
    		createSingleFigure.setSelected(true);
    		createMultipleFigure.setSelected(false);
    	}
    	else
    	{
    		createSingleFigure.setSelected(false);
    		createMultipleFigure.setSelected(true);
    	}
    	toolBar.createSingleFigure(createSingleFig);
    	
    }
    
	/**
     * is the user menu set to create single figures
     * 
     * @return see above.
     */
    boolean isCreateSingleFigure()
    {
    	return createSingleFigure.isSelected();
    }
    
    /**
     * Returns the id of the pixels set this tool is for.
     * 
     * @return See above.
     */
    long getPixelsID() { return model.getPixelsID(); }
 
    /**
	 * Calculate the stats for the Rois in the shapelist. This method
	 * will call the graphView.
	 * 
	 * @param id see above.
	 * @param shapeList see above.
	 */
	void calculateStats(List<ROIShape> shapeList)
	{
		if (model.getState() != MeasurementViewer.READY) return;
		model.calculateStats(shapeList);
	}
	
    /** 
     * Overridden to the set the location of the {@link MeasurementViewer}.
     * @see TopWindow#setOnScreen() 
     */
    public void setOnScreen()
    {
        if (model != null) { //Shouldn't happen
        	setSize(DEFAULT_SIZE);
        	
            UIUtilities.setLocationRelativeToAndSizeToWindow(
            		model.getRequesterBounds(), this, MAXIMUM_SIZE);
        } else {
            pack();
            UIUtilities.incrementRelativeToAndShow(null, this);
        }
    }
    
}
