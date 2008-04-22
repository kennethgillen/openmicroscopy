/*
 * org.openmicroscopy.shoola.agents.util.editor.PropertiesUI 
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
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.TreeComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.TitledLineBorder;
import pojos.AnnotationData;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PermissionData;
import pojos.ProjectData;

/** 
 * Displays the properties of the selected object.
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
class PropertiesUI   
	extends AnnotationUI
	implements DocumentListener
{
    
	/** The title associated to this component. */
	static final String			TITLE = "Properties";

	/** The details string. */
    private static final String DETAILS = "Details";
    
    /** The maximum width of the name area. */
    private static final int	WIDTH = 250;
    
    /** The maximum width of the e-mail and owner area. */
    private static final int	WIDTH_NAME = 200;
    
    /** Area where to enter the name of the <code>DataObject</code>. */
    private JTextField          nameArea;
     
    /** Area where to enter the description of the <code>DataObject</code>. */
    private JTextArea          	descriptionArea;

    /** Panel hosting the main display. */
    private JComponent			contentPanel;
    
    /** The name before possible modification. */
    private String				originalName;
    
    /** The description before possible modification. */
    private String				originalDescription;
    
    /**
     * Builds and lays out the panel displaying the permissions of the edited
     * file.
     * 
     * @param permissions   The permissions of the edited object.
     * @return See above.
     */
    private JPanel buildPermissions(PermissionData permissions)
    {
        JPanel content = new JPanel();
        double[][] tl = {{TableLayout.PREFERRED, WIDTH}, //columns
        				{TableLayout.PREFERRED, TableLayout.PREFERRED,
        				TableLayout.PREFERRED} }; //rows
        content.setLayout(new TableLayout(tl));
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        //The owner is the only person allowed to modify the permissions.
        //boolean isOwner = model.isObjectOwner();
        //Owner
        JLabel label = UIUtilities.setTextFont(EditorUtil.OWNER);
        JPanel p = new JPanel();
        JCheckBox box =  new JCheckBox(EditorUtil.READ);
        box.setSelected(permissions.isUserRead());
        /*
        box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setUserRead(source.isSelected());
               view.setEdit(true);
            }
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        box =  new JCheckBox(EditorUtil.WRITE);
        box.setSelected(permissions.isUserWrite());
        /*
        box.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setUserWrite(source.isSelected());
               view.setEdit(true);
            }
        
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        content.add(label, "0, 0, l, c");
        content.add(p, "1, 0, l, c");  
        //Group
        label = UIUtilities.setTextFont(EditorUtil.GROUP);
        p = new JPanel();
        box =  new JCheckBox(EditorUtil.READ);
        box.setSelected(permissions.isGroupRead());
        /*
        box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setGroupRead(source.isSelected());
               view.setEdit(true);
            }
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        box =  new JCheckBox(EditorUtil.WRITE);
        box.setSelected(permissions.isGroupWrite());
        /*
        box.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setGroupWrite(source.isSelected());
               view.setEdit(true);
            }
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        content.add(label, "0, 1, l, c");
        content.add(p, "1, 1, l, c"); 
        //OTHER
        label = UIUtilities.setTextFont(EditorUtil.WORLD);
        p = new JPanel();
        box =  new JCheckBox(EditorUtil.READ);
        box.setSelected(permissions.isWorldRead());
        /*
        box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setWorldRead(source.isSelected());
               view.setEdit(true);
            }
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        box =  new JCheckBox(EditorUtil.WRITE);
        box.setSelected(permissions.isWorldWrite());
        /*
        box.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setWorldWrite(source.isSelected());
               view.setEdit(true);
            }
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        content.add(label, "0, 2, l, c");
        content.add(p, "1, 2, l, c"); 
        return content;
    }
    
    /**
     * Lays out the key/value (String, String) pairs.
     * 
     * @param details The map to handle.
     * @return See above.
     */
    private JPanel layoutDetails(Map details)
    {
    	JPanel content = new JPanel();
    	double[] columns = {TableLayout.PREFERRED, 5,  WIDTH_NAME};
    	TableLayout tl = new TableLayout();
    	content.setLayout(tl);
    	tl.setColumn(columns);
    	Iterator i = details.keySet().iterator();
        JLabel label;
        JTextField area;
        String key, value;
        int index = 0;
        while (i.hasNext()) {
        	tl.insertRow(index, TableLayout.PREFERRED);
            key = (String) i.next();
            value = (String) details.get(key);
            label = UIUtilities.setTextFont(key);
            content.add(label, "0, "+index);
            area = new JTextField(value);
            area.setEditable(false);
            area.setEnabled(false);
            label.setLabelFor(area);
            content.add(area, "2, "+index);  
            index++;
            tl.insertRow(index, TableLayout.PREFERRED);
            content.add(new JLabel(), "0, "+index+", 2, "+index);
            index++;
        }
        return content;
    }
    
    /** Initializes the components composing this display. */
    private void initComponents()
    {
        nameArea = new JTextField();
        UIUtilities.setTextAreaDefault(nameArea);
        descriptionArea = new MultilineLabel();
        UIUtilities.setTextAreaDefault(descriptionArea);
        nameArea.setEnabled(false);
        descriptionArea.setEnabled(false);
    }   
    
    /**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}. If the <code>DataOject</code>
     * is annotable and if we are in the {@link Editor#PROPERTIES_EDITOR} mode,
     * we display the annotation pane. 
     * 
     * @return See above.
     */
    private JPanel buildContentPanel()
    {
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		p.add(UIUtilities.setTextFont("Name"), c);
		c.gridx++;
		p.add(Box.createHorizontalStrut(5), c);
		c.gridx++;
		c.weightx = 0.5;
		p.add(nameArea, c);
		c.gridy++;
		p.add(Box.createVerticalStrut(5), c);
		c.gridx = 0;
		c.gridy++;
		c.weightx = 0;
		p.add (UIUtilities.setTextFont("Description"), c);
		c.gridx++;
		p.add(Box.createHorizontalStrut(5), c);
		c.gridx++;
		c.weightx = 0.5;
		c.ipady = 80; 
		p.add(new JScrollPane(descriptionArea), c);
        return p;
    }
    
    /**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}.
     */
    private void buildGUI()
    {
        setLayout(new BorderLayout());
        contentPanel = new JPanel();
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        contentPanel.setLayout(new BoxLayout(contentPanel, 
    		   					BoxLayout.Y_AXIS));
    	contentPanel.add(buildContentPanel());
        ExperimenterData exp = model.getRefObjectOwner();
        if (exp != null) {
        	JPanel p = new JPanel();
        	p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        	JPanel details = layoutDetails(
        				EditorUtil.transformExperimenterData(exp));
        	PermissionData perm = model.getRefObjectPermissions();
        	p.add(details);
        	if (perm != null && !(model.getRefObject() instanceof ImageData)) {
        		p.add(buildPermissions(perm));
        	}
        	//UIUtilities.setBoldTitledBorder(DETAILS, p);
        	p.setBorder(new TitledLineBorder(DETAILS));
        	TreeComponent tree = new TreeComponent();
        	JPanel collapse = new JPanel();
        	collapse.setBorder(new TitledLineBorder(DETAILS));
        	tree.insertNode(p, collapse, false);
        	contentPanel.add(tree);
            contentPanel.add(new JPanel());
        }
        add(contentPanel, BorderLayout.NORTH);
    }

    /**
     * Creates a new instance.
     * 
     * @param model Reference to the {@link EditorModel}.
     * 				Mustn't be <code>null</code>.                            
     */
    PropertiesUI(EditorModel model)
    {
       super(model);
       title = TITLE;
       initComponents();
       UIUtilities.setBoldTitledBorder(getComponentTitle(), this);
       TitledLineBorder border = new TitledLineBorder(title, getBackground());
       getCollapseComponent().setBorder(border);
       buildGUI();
    }   

    /**
	 * Overridden to lay out the tags.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		removeAll();
		nameArea.getDocument().removeDocumentListener(this);
		descriptionArea.getDocument().removeDocumentListener(this);
		originalName = model.getRefObjectName();
		nameArea.setText(originalName);
		originalDescription = model.getRefObjectDescription();
        descriptionArea.setText(originalDescription);
        boolean b = model.isCurrentUserOwner(model.getRefObject());
        nameArea.setEnabled(b);
        descriptionArea.setEnabled(b);
        if (b) {
        	nameArea.getDocument().addDocumentListener(this);
    		descriptionArea.getDocument().addDocumentListener(this);
        }
        buildGUI();
	}
	
    /** Sets the focus on the name area. */
	void setFocusOnName() { nameArea.requestFocus(); }
   
	/** Updates the data object. */
	void updateDataObject() 
	{
		if (!hasDataToSave()) return;
		Object object =  model.getRefObject();
		String name = nameArea.getText().trim();
		String desc = descriptionArea.getText().trim();
		if (object instanceof ProjectData) {
			ProjectData p = (ProjectData) object;
			if (name.length() > 0)
				p.setName(name);
			p.setDescription(desc);
		} else if (object instanceof DatasetData) {
			DatasetData p = (DatasetData) object;
			if (name.length() > 0)
				p.setName(name);
			p.setDescription(desc);
		} else if (object instanceof ImageData) {
			ImageData p = (ImageData) object;
			if (name.length() > 0)
				p.setName(name);
			p.setDescription(desc);
		}
	}
	
	/**
	 * Returns <code>true</code> if the name is valid,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isNameValid()
	{ 
		String name = nameArea.getText();
		if (name == null) return false;
		return name.trim().length() != 0;
	}
	
	/**
	 * Overridden to set the title of the component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return TITLE; }

	/**
	 * No-op implementation in this case.
	 * @see AnnotationUI#getAnnotationToRemove()
	 */
	protected List<AnnotationData> getAnnotationToRemove() { return null; }

	/**
	 * No-op implementation in this case.
	 * @see AnnotationUI#getAnnotationToSave()
	 */
	protected List<AnnotationData> getAnnotationToSave() { return null; }
	
	/**
	 * Returns <code>true</code> if the data object has been edited,
	 * <code>false</code> otherwise.
	 * @see AnnotationUI#hasDataToSave()
	 */
	protected boolean hasDataToSave()
	{
		String name = originalName;//model.getRefObjectName().trim();
		String value = nameArea.getText();
		if (!name.equals(value.trim())) return true;
		
		name = originalDescription;//model.getRefObjectDescription();
		value = descriptionArea.getText();
		value = value.trim();
		if (name == null) 
			return value.length() != 0;
		name = name.trim();
		if (value.equals(name)) return false;
		return true;
	}
	
	/**
	 * Clears the data to save.
	 * @see AnnotationUI#clearData()
	 */
	protected void clearData()
	{
		nameArea.getDocument().removeDocumentListener(this);
		descriptionArea.getDocument().removeDocumentListener(this);
		nameArea.setText("");
		descriptionArea.setText("");
		nameArea.getDocument().addDocumentListener(this);
		descriptionArea.getDocument().addDocumentListener(this);
		/*
		nameArea.setText(originalName);
		String name = originalDescription;//model.getRefObjectDescription();
		if (name != null)
			descriptionArea.setText(name.trim());
			*/
	}
	
	/**
	 * Clears the UI.
	 * @see AnnotationUI#clearDisplay()
	 */
	protected void clearDisplay() 
	{
		
	}

	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
						Boolean.TRUE);
	}

	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
							Boolean.TRUE);
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
}
