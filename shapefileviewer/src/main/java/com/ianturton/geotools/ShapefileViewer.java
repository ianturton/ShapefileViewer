package com.ianturton.geotools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.GeoTools;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.map.StyleLayer;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.Renderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLD;
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryType;
import org.w3c.dom.Document;

import com.ianturton.swing.JLegendPanel;


public class ShapefileViewer {
    JMapFrame frame;
    MapContent map;
    private AttributeDescriptor selAttr;
    private JTable varTable;
    private SimpleFeatureType schema;
    private SimpleFeatureSource featureSource;
    private JLegendPanel legend = new JLegendPanel();
    private StyleLayer layer;

    public ShapefileViewer() {

	map = new MapContent();

	frame = new JMapFrame(map);
	GTRenderer renderer = frame.getMapPane().getRenderer();
	System.out.println(renderer);
	renderer.setJava2DHints(new RenderingHints(
		RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON));
	frame.enableToolBar(true);
	frame.setMinimumSize(new Dimension(800, 400));
	varTable = new JTable();
	
	JPanel panel = new JPanel();

	BoxLayout boxMgr = new BoxLayout(panel, BoxLayout.Y_AXIS);
	BorderLayout borderMgr = new BorderLayout();
	panel.setLayout(borderMgr);
	// legend.setMinimumSize(new Dimension(200,200));
	panel.add(legend, BorderLayout.NORTH);

	JScrollPane scroll = new JScrollPane(varTable);

	scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

	panel.add(scroll, BorderLayout.CENTER);

	JMenuBar menuBar = new JMenuBar();
	JMenu fileMenu = new JMenu("File");
	fileMenu.setMnemonic('F');
	JMenuItem addFile = new JMenuItem("Set File");
	addFile.setMnemonic('F');
	addFile.addActionListener(new ActionListener() {
	    File file = null;

	    @Override
	    public void actionPerformed(ActionEvent e) {

		file = JFileDataStoreChooser.showOpenFile("shp", null);
		if (file == null) {
		    return;
		}

		setFile(file);
	    }
	});
	fileMenu.add(addFile);
	JMenuItem sld = new JMenuItem("Save Style", 'S');
	sld.addActionListener(new ActionListener() {
	    File file = null;

	    @Override
	    public void actionPerformed(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();

		fileChooser.setFileFilter(new FileFilter() {

		    @Override
		    public String getDescription() {
			// TODO Auto-generated method stub
			return "SLD files";
		    }

		    @Override
		    public boolean accept(File f) {
			if (f.isDirectory()) {
			    return true;
			}
			return f.getName().endsWith(".sld");
		    }
		});
		if (fileChooser.showSaveDialog(fileChooser) == JFileChooser.APPROVE_OPTION) {
		    file = fileChooser.getSelectedFile();

		}
		if (file == null) {
		    return;
		}

		exportStyle(file);
	    }

	});
	fileMenu.add(sld);
	JMenuItem svg = new JMenuItem("Export as SVG", 'E');
	svg.addActionListener(new ActionListener() {
	    File file = null;

	    @Override
	    public void actionPerformed(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();

		fileChooser.setFileFilter(new FileFilter() {

		    @Override
		    public String getDescription() {
			// TODO Auto-generated method stub
			return "SVG files";
		    }

		    @Override
		    public boolean accept(File f) {
			if (f.isDirectory()) {
			    return true;
			}
			return f.getName().endsWith(".svg");
		    }
		});
		if (fileChooser.showSaveDialog(fileChooser) == JFileChooser.APPROVE_OPTION) {
		    file = fileChooser.getSelectedFile();

		}
		if (file == null) {
		    return;
		}

		exportSVG(file);
	    }

	});
	fileMenu.add(svg);
	menuBar.add(fileMenu);
	frame.setJMenuBar(menuBar);
	frame.setLayout(new BorderLayout());
	frame.getContentPane().add(panel, BorderLayout.EAST);

	frame.setVisible(true);
    }

    public ShapefileViewer(String string) {
	this();
	File file = new File(string);
	setFile(file);
    }

    private void update(final int row) {
	Thread t = new Thread() {

	    public void run() {

		int attr = row;

		if (attr < 0) {
		    return;
		}
		String name = (String) varTable.getValueAt(attr, 0);
		selAttr = schema.getDescriptor(name);
		if (selAttr == null || map.layers().isEmpty()) {
		    return;
		}
		FeatureLayer layer = (FeatureLayer) map.layers().get(0);
		Style style = null;
		try {
		    style = legend.getStyle((SimpleFeatureCollection) layer
			    .getFeatureSource().getFeatures(), selAttr
			    .getLocalName());
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		    return;
		}

		layer.setStyle(style);
	    }
	};
	SwingUtilities.invokeLater(t);
    }

    /**
     * 
     */
    private void setFile(File file) {
	if (map.layers().size() > 0) {
	    map.removeLayer(layer);
	    map = new MapContent();
	    frame.setMapContent(map);
	}
	FileDataStore store;
	try {
	    store = FileDataStoreFinder.getDataStore(file);

	    featureSource = store.getFeatureSource();

	    schema = featureSource.getSchema();

	    Style style = SLD.createSimpleStyle(schema);
	    this.layer = new FeatureLayer(featureSource, style);

	    legend.setLayer(this.layer);
	    legend.resetLabels();
	    map.addLayer(layer);

	    int rows = schema.getAttributeCount();

	    Object[][] values = new Object[rows][2];
	    int rowCount = 0;

	    for (int i = 0; i < rows; i++) {
		AttributeDescriptor attribute = schema.getDescriptor(i);
		if (attribute.getType() instanceof GeometryType)
		    continue;

		String binding = attribute.getType().getBinding().toString();
		int index = binding.lastIndexOf('.');
		if (index > 0)
		    binding = binding.substring(index + 1);
		if (!binding.equalsIgnoreCase("String")) {
		    values[rowCount][0] = attribute.getLocalName();
		    values[rowCount][1] = binding;

		    rowCount++;
		} else {
		    legend.addLabel(attribute.getLocalName());
		}

	    }
	    rows = rowCount - 1;
	    varTable.setModel(new DefaultTableModel(rows, 2));
	    for (int i = 0; i < rows; i++) {
		varTable.getModel().setValueAt(values[i][0], i, 0);
		varTable.getModel().setValueAt(values[i][1], i, 1);
	    }
	    varTable.getColumnModel().getColumn(0).setHeaderValue("Attributes");
	    varTable.getColumnModel().getColumn(1).setHeaderValue("Type");
	    varTable.setRowSelectionAllowed(true);
	    varTable.getSelectionModel().addListSelectionListener(
		    new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
			    if (!event.getValueIsAdjusting()) {

				update(varTable.getSelectedRow());
			    }
			}
		    });
	} catch (IOException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}
    }

    private void exportStyle(File file) {

	Style style = layer.getStyle();
	SLDTransformer tx = new SLDTransformer();

	tx.setIndentation(2);

	try {
	    OutputStream output = new FileOutputStream(file);
	    tx.transform(style, output);
	    output.close();
	} catch (TransformerException e) {
	    e.printStackTrace();

	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    private void exportSVG(File file) {

	Dimension canvasSize = new Dimension(frame.getMapPane().getSize());

	Document document = null;

	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder db;
	try {
	    db = dbf.newDocumentBuilder();
	} catch (ParserConfigurationException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	    return;
	}

	// Create an instance of org.w3c.dom.Document
	document = db.getDOMImplementation().createDocument(null, "svg", null);

	// Set up the map
	SVGGeneratorContext ctx1 = SVGGeneratorContext.createDefault(document);
	SVGGeneratorContext ctx = ctx1;
	ctx.setComment("Generated by GeoTools with Batik SVG Generator");

	SVGGraphics2D g2d = new SVGGraphics2D(ctx, true);

	g2d.setSVGCanvasSize(canvasSize);

	Rectangle outputArea = new Rectangle(g2d.getSVGCanvasSize());
	ReferencedEnvelope dataArea = map.getViewport().getBounds();

	frame.getMapPane().getRenderer().paint(g2d, outputArea, dataArea);
	OutputStreamWriter osw = null;
	try {
	    OutputStream out = new FileOutputStream(file);
	    osw = null;

	    osw = new OutputStreamWriter(out, "UTF-8");
	    g2d.stream(osw);
	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (UnsupportedEncodingException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (SVGGraphics2DIOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} finally {
	    if (osw != null)
		try {
		    osw.close();
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	}
    }

    public static void main(String[] args) {
	if (args.length > 0) {
	    new ShapefileViewer(args[0]);
	} else {
	    new ShapefileViewer();

	}
    }
}
