package com.ianturton.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.transform.TransformerException;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.map.StyleLayer;
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Function;



@SuppressWarnings("serial")
public class JLegendPanel extends JPanel {
    public static final int NCOLS = 2;
    public static final int DEFAULT_NUMBER_OF_CLASSES = 5;
    public static final String DEFAULT_PALLETE = "Pastel1";
    private static final int COLOR_COL = 1;

    private static final int TITLE_COL = 0;
    private String attribute = "";
    private JTable table;
    private JComboBox<String> palletePicker = new JComboBox<String>();
    private JComboBox<String> functionPicker = new JComboBox<String>();
    private JSpinner numberSpinner = new JSpinner();
    private DefaultTableModel tableModel;
    private com.ianturton.styling.ClassifiedStyleCreator styler;
    
    


 
    private StyleLayer layer = null;
    
    private JComboBox<String> labels;

    public JLegendPanel() {
	this(DEFAULT_NUMBER_OF_CLASSES);
    }

    public JLegendPanel(int defaultNumberOfClasses) {
	this(defaultNumberOfClasses, DEFAULT_PALLETE);
    }

    public JLegendPanel(int classCount, String pallete) {
	super();
	styler = new com.ianturton.styling.ClassifiedStyleCreator();

	palletePicker = new JComboBox<String>(styler.getPaletteNames().toArray(
		new String[] {}));

	palletePicker.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		System.out.println(e.getActionCommand());
		String pal = (String) ((JComboBox<String>) e.getSource())
			.getSelectedItem();
		System.out.println(pal);
		setPallete(pal);
	    }
	});
	styler.setPalette(pallete);
	palletePicker.setSelectedItem(styler.getPaletteName());

	List<String> fNames = styler.getFunctionNames();

	functionPicker = new JComboBox<String>(fNames.toArray(new String[] {}));

	functionPicker.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		String func = (String) ((JComboBox<String>) e.getSource())
			.getSelectedItem();
		setFunction(func);

	    }
	});
	functionPicker.setSelectedItem("Jenks");
	numberSpinner.setModel(new SpinnerNumberModel(
		DEFAULT_NUMBER_OF_CLASSES, 2, 10, 1));
	numberSpinner.addChangeListener(new ChangeListener() {

	    @Override
	    public void stateChanged(ChangeEvent e) {
		JSpinner s = ((JSpinner) e.getSource());
		setNumberOfClasses((Integer) s.getValue());
	    }
	});
	setTableModel();
	table = new JTable(tableModel);
	table.setRowSelectionAllowed(false);

	table.setColumnSelectionAllowed(false);
	table.setDefaultRenderer(Color.class, new ColorCellRenderer());
	
	JPanel topPanel = new JPanel(new FlowLayout());
	topPanel.add(palletePicker);
	topPanel.add(functionPicker);
	topPanel.add(new JLabel("#Classes:"));
	topPanel.add(numberSpinner);
	topPanel.add(new JLabel(" "));
	JPanel checks = new JPanel();
	checks.setLayout(new BoxLayout(checks, BoxLayout.Y_AXIS));
	final JCheckBox borders = new JCheckBox("borders");
	borders.setSelected(true);
	styler.setColoredStrokes(false);
	borders.addItemListener(new ItemListener() {

	    @Override
	    public void itemStateChanged(ItemEvent e) {

		styler.setColoredStrokes(!borders.isSelected());
		updateStyle();
	    }
	});
	labels = new JComboBox<String>(new String[] { "No Labels" });
	labels.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {

		String label = (String) ((JComboBox<String>) e.getSource())
			.getSelectedItem();
		if(label==null || label.isEmpty() )
		    return;
		if (label.equalsIgnoreCase("No Labels")) {
		    if(styler.isLabeled())
			styler.setLabelAttribute("");
		    else
			return;
		} else {
		    styler.setLabelAttribute(label);
		}
		updateStyle();
	    }
	});
	checks.add(borders);
	checks.add(labels);
	topPanel.add(checks);
	this.setLayout(new BorderLayout());
	this.add(topPanel, BorderLayout.NORTH);
	this.add(table, BorderLayout.CENTER);
	this.setNumberOfClasses(classCount);
	this.setPallete(pallete);

	this.setMinimumSize(new Dimension(200, 100));
	this.revalidate();
    }

    /**
     * 
     */
    private void setTableModel() {
	tableModel = new DefaultTableModel(getNumberOfClasses(), NCOLS) {
	    @Override
	    public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == COLOR_COL)
		    return Color.class;
		else
		    return String.class;
	    }
	};
	if (table != null)
	    table.setModel(tableModel);
    }

    public String getPallete() {
	return styler.getPaletteName();
    }

    public void setPallete(String pallete) {
	styler.setPalette(pallete);
	
	//setTableModel();
	updateStyle();
    }

    public int getNumberOfClasses() {
	return styler.getNumberOfClasses();
    }

    public void setNumberOfClasses(int numberOfClasses) {
	styler.setNumberOfClasses(numberOfClasses);
	
	setTableModel();
	updateStyle();
	revalidate();
    }

    public Color[] getColors() {
	return styler.getColors().toArray(new Color[] {});
    }

    @SuppressWarnings("unchecked")
    public Style getStyle(SimpleFeatureCollection collection, String attribute) {
	this.attribute = attribute;
	Style style = styler.getStyle(collection, attribute);
	List<String> titles = styler.getTitles();
	List<Color> colors = styler.getColors();
	for (int i = 0; i < titles.size(); i++) {

	    tableModel.setValueAt(titles.get(i), i, TITLE_COL);

	    tableModel.setValueAt(colors.get(i), i, COLOR_COL);
	}
	this.repaint();
	return style;

    }

   

    
    private void updateStyle() {
	System.out.println("updating "+styler.getPaletteName()+" of "+getNumberOfClasses());
	if (layer != null) {

	    FeatureSource<?, ?> featureSource = layer.getFeatureSource();
	    try {
		Style style = getStyle(
			(SimpleFeatureCollection) featureSource.getFeatures(),
			attribute);
		System.out.println("setting style");
		layer.setStyle(style);

	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

    }

    public boolean setFunction(String name) {

	boolean ret = false;

	List<String> functionNames = styler.getFunctionNames();
	for (int i = 0; i < functionNames.size(); i++) {
	    
	    if (functionNames.get(i).equalsIgnoreCase(name)) {
		ret = true;
		styler.setClassifier(name);
		
		setTableModel();
		updateStyle();
		break;
	    }
	}

	return ret;

    }

    public String[] getPalletes() {
	return styler.getPaletteNames().toArray(new String[] {});
    }

    public String[] getClassifiers() {
	return styler.getFunctionNames().toArray(new String[] {});
	
    }

    public static void main(String[] args) throws IOException {
	JLegendPanel legend = new JLegendPanel();
	File file = new File("../../data/states.shp");
	ShapefileDataStore ds = new ShapefileDataStore(
		DataUtilities.fileToURL(file));
	ContentFeatureCollection collection = ds.getFeatureSource()
		.getFeatures();

	String[] pals = legend.getPalletes();
	for (String p : pals)
	    System.out.println(p);

	for (String n : legend.getClassifiers()) {
	    System.out.println(n);

	    legend.setFunction(n);
	    Style style = legend.getStyle(collection, "P_FEMALE");
	    SLDTransformer tx = new SLDTransformer();

	    tx.setIndentation(2);

	    try {
		tx.transform(style, System.out);
	    } catch (TransformerException e) {
		throw (IOException) new IOException("Error writing style")
			.initCause(e);
	    }
	}

    }

    public void setLayer(StyleLayer layer) {
	this.layer = layer;

    }

    public void addLabel(String localName) {
	labels.addItem(localName);

    }

    public void resetLabels() {
	if (labels != null) {
	    if (labels.getItemCount() > 0)
		labels.removeAllItems();

	    labels.addItem("no labels");
	}
    }
}
