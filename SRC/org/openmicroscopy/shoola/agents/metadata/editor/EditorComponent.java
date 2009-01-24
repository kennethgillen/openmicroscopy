/*
 * org.openmicroscopy.shoola.agents.metadata.editor.EditorComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.browser.Browser;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import org.openmicroscopy.shoola.util.ui.tdialog.TinyDialog;
import pojos.AnnotationData;
import pojos.ChannelAcquisitionData;
import pojos.ChannelData;
import pojos.FileAnnotationData;
import pojos.ImageAcquisitionData;
import pojos.ImageData;
import pojos.TagAnnotationData;
import pojos.URLAnnotationData;

/** 
 * Implements the {@link Editor} interface to provide the functionality
 * required of the hierarchy viewer component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
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
class EditorComponent 
	extends AbstractComponent
	implements Editor
{

	/** The Model sub-component. */
	private EditorModel		model;
	
	/** The Control sub-component. */
	private EditorControl	controller;
	
	/** The View sub-component. */
	private EditorUI		view;

	/**
	 * Shows the selection wizard.
	 * 
	 * @param type			The type of objects to handle.
	 * @param available 	The available objects.
	 * @param selected  	The selected objects.
	 * @param addCreation	Pass <code>true</code> to add a component
	 * 						allowing creation of object of the passed type,
	 * 						<code>false</code> otherwise.
	 */
	private void showSelectionWizard(Class type, Collection available, 
									Collection selected, boolean addCreation)
	{
		IconManager icons = IconManager.getInstance();
		Registry reg = MetadataViewerAgent.getRegistry();
		SelectionWizard wizard = new SelectionWizard(
				reg.getTaskBar().getFrame(), available, selected, type,
				addCreation);
		wizard.setTitle("Tags Selection", "Select the Tags to add or " +
				"remove, \nor Create new Tags",  
				icons.getIcon(IconManager.TAGS_48));
		wizard.addPropertyChangeListener(controller);
		UIUtilities.centerAndShow(wizard);
	}
	
	/**
	 * Creates a new instance.
	 * The {@link #initialize() initialize} method should be called straigh 
	 * after to complete the MVC set up.
	 * 
	 * @param model The Model sub-component. Mustn't be <code>null</code>.
	 */
	EditorComponent(EditorModel model)
	{
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
		view = new EditorUI();
		controller = new EditorControl();
	}
	
	/** Links up the MVC triad. */
	void initialize()
	{
		controller.initialize(this, view);
		view.initialize(model, controller);
		model.getObservable().addPropertyChangeListener(controller);
	}
	
	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#getUI()
	 */
	public JComponent getUI() { return view; }

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setStructuredDataResults()
	 */
	public void setStructuredDataResults()
	{
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		view.layoutUI();
		if (model.hasBeenViewedBy() && !model.isThumbnailsLoaded())
			model.loadThumbnails();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setRootObject(Object)
	 */
	public void setRootObject(Object refObject)
	{
		if (refObject == null)
			throw new IllegalArgumentException("Root object not valid.");
		model.setRootObject(refObject);
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		/*
		if ((refObject instanceof String) && 
			(refObject instanceof TimeRefObject)) {
			view.showEditor(false);
		} else {
			view.showEditor(true);
			
			//model.loadUserThumbnail();
		}
		*/
		view.setRootObject();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setThumbnails(Map, long)
	 */
	public void setThumbnails(Map<Long, BufferedImage> thumbnails, 
							long imageID)
	{
		Object ref = model.getRefObject();
		if (ref instanceof ImageData) {
			if (((ImageData) ref).getId() == imageID) {
				model.setThumbnails(thumbnails);
				view.setThumbnails();
			}
		}
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setExistingTags(Collection)
	 */
	public void setExistingTags(Collection tags)
	{
		model.setExistingTags(tags);
		if (view.isAutoComplete()) view.setExistingTags();
		else {
			Collection setTags = view.getCurrentTagsSelection();
			Iterator<TagAnnotationData> k = setTags.iterator();
			List<Long> ids = new ArrayList<Long>();
			while (k.hasNext()) {
				ids.add(k.next().getId());
			}
			List available = new ArrayList();
			if (tags != null) {
				Iterator i = tags.iterator();
				TagAnnotationData data, tag;
				String ns;
				Set<TagAnnotationData> l;
				Iterator<TagAnnotationData> j;
				while (i.hasNext()) {
					data = (TagAnnotationData) i.next();
					ns = data.getNameSpace();
					if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
						l = data.getTags();
						if (l != null) {
							j = l.iterator();
							while (j.hasNext()) {
								tag = j.next();
								if (!ids.contains(tag.getId()))
									available.add(tag);
							}
						}
					} else {
						if (!ids.contains(data.getId()))
							available.add(data);
					}
				}
			}
			showSelectionWizard(TagAnnotationData.class, available, setTags,
								true);
			
		}
		setStatus(false);
	}
	
	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setChannelsData(List, boolean)
	 */
	public void setChannelsData(List channelData, boolean updateView)
	{
		model.setChannelData(channelData);
		if (updateView) view.showChannelData();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#hasDataToSave()
	 */
	public boolean hasDataToSave()
	{
		return view.hasDataToSave();
	}
	
	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setDownloadedFiles(Collection)
	 */
	public void setDownloadedFiles(Collection files)
	{
		if (files == null || files.size() == 0) return;
		UserNotifier un = MetadataViewerAgent.getRegistry().getUserNotifier();
		un.notifyDownload(files);
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setDiskSpace(List)
	 */
	public void setDiskSpace(List list)
	{
		if (list == null || list.size() != 2) return;
		view.setDiskSpace(list);
		view.layoutUI();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#passwordChanged(boolean)
	 */
	public void passwordChanged(boolean changed)
	{
		UserNotifier un = MetadataViewerAgent.getRegistry().getUserNotifier();
		if (changed) {
			un.notifyInfo("Password change", "The password has been " +
					"successfully modified.");
		} else {
			un.notifyInfo("Password change", "The password could not be " +
					"modified. Please try again.");
		}
		view.passwordChanged();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#loadChannelData()
	 */
	public void loadChannelData()
	{
		if (model.getChannelData() == null) model.loadChannelData();
		else view.showChannelData();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setExistingAttachements(Collection)
	 */
	public void setExistingAttachements(Collection attachments)
	{
		if (attachments == null) return;
		model.setExistingAttachments(attachments);
		
		
		
		
		
		//view.setExistingAttachements();
		setStatus(false);
	}
	
	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setExistingURLs(Collection)
	 */
	public void setExistingURLs(Collection urls)
	{
		if (urls == null) return;
		model.setExistingURLs(urls);
		view.setExistingURLs();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setSelectionMode(boolean)
	 */
	public void setSelectionMode(boolean single)
	{
		view.repaint();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#loadParents()
	 */
	public void loadParents()
	{
		model.loadParents();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setStatus(boolean)
	 */
	public void setStatus(boolean busy)
	{
		view.setStatus(busy);
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setStatus(boolean)
	 */
	public void loadExistingTags()
	{
		model.loadExistingTags();
		setStatus(true);
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#deleteAnnotation(AnnotationData)
	 */
	public void deleteAnnotation(AnnotationData data)
	{
		if (data == null) return;
		String s = null;
		if (data instanceof FileAnnotationData) 
			s = "Do you want to delete the attachment?";
		else if (data instanceof URLAnnotationData) 
			s = "Do you want to delete the URL?";
		
		if (s == null) return;
		JFrame owner = 
			MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
		MessageBox msg = new MessageBox(owner, "Delete", s);
		int option = msg.centerMsgBox();
		if (option == MessageBox.YES_OPTION) {
			List<AnnotationData> toRemove = new ArrayList<AnnotationData>(1);
			List<AnnotationData> toAdd = new ArrayList<AnnotationData>();
			toRemove.add(data);
			//model.fireAnnotationSaving(toAdd, toRemove);
		}
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setImageAcquisitionData(ImageAcquisitionData)
	 */
	public void setImageAcquisitionData(ImageAcquisitionData map)
	{
		if (map == null) return;
		model.setImageAcquisitionData(map);
		view.setImageAcquisitionData();
		view.setStatus(false);
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#loadImageAcquisitionData()
	 */
	public void loadImageAcquisitionData()
	{
		if (!(model.getRefObject() instanceof ImageData)) return;
		Object data = model.getImageAcquisitionData();
		if (data != null) return;
		if (!(model.getRefObject() instanceof ImageData)) return;
		model.fireImagAcquisitionDataLoading();
		view.setStatus(true);
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#loadChannelAcquisitionData(ChannelData)
	 */
	public void loadChannelAcquisitionData(ChannelData channel)
	{
		if (!(model.getRefObject() instanceof ImageData)) return;
		if (channel == null) return;
		Object data = model.getChannelAcquisitionData(channel.getIndex());
		if (data != null) return;
		model.fireChannelAcquisitionDataLoading(channel);
		view.setStatus(true);
	}
	
	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setChannelEnumerations(Map)
	 */
	public void setChannelEnumerations(Map map)
	{
		model.setChannelEnumerations(map);
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setImageEnumerations(Map)
	 */
	public void setImageEnumerations(Map map)
	{
		model.setImageEnumerations(map);
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#showManufacturer(JComponent, Point)
	 */
	public void showManufacturer(JComponent comp, Point p)
	{
		TinyDialog d = new TinyDialog(
				MetadataViewerAgent.getRegistry().getTaskBar().getFrame(), comp,
				TinyDialog.CLOSE_ONLY);
		d.pack();
		Dimension dim = d.getSize();
		d.setLocation(p.x-dim.width/2, p.y-dim.height-5);
		d.setVisible(true);
		
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setChannelAcquisitionData(int, ChannelAcquisitionData)
	 */
	public void setChannelAcquisitionData(int index, 
			ChannelAcquisitionData data)
	{
		model.setChannelAcquisitionData(index, data);
		view.setChannelAcquisitionData(index);
		view.setStatus(false);
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#loadExistingAttachments()
	 */
	public void loadExistingAttachments()
	{
		model.loadExistingAttachments();
		setStatus(true);
	}
	
}
