package com.ianturton.styling;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.ComboBoxModel;

import org.geotools.brewer.color.BrewerPalette;
import org.geotools.brewer.color.ColorBrewer;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.filter.function.ClassificationFunction;
import org.geotools.filter.function.Classifier;
import org.geotools.filter.function.JenksNaturalBreaksFunction;
import org.geotools.filter.function.RangedClassifier;
import org.geotools.geometry.jts.Geometries;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

import com.vividsolutions.jts.geom.Geometry;

public class ClassifiedStyleCreator {
    ColorBrewer brewer = ColorBrewer.instance();
    public static final int DEFAULT_NUMBER_OF_CLASSES = 5;
    public static final String DEFAULT_PALETTE = "Pastel1";
    private static final Function DEFAULT_CLASSIFIER = new JenksNaturalBreaksFunction();
    private int numberOfClasses = DEFAULT_NUMBER_OF_CLASSES;
    private String paletteName = DEFAULT_PALETTE;
    private static StyleBuilder sb = new StyleBuilder();
    private static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
    private Function classifier = DEFAULT_CLASSIFIER;
    private ArrayList<Function> classifiers = new ArrayList<Function>();
    private List<Color> colors = new ArrayList<Color>();
    private boolean labeled = false;
    private boolean coloredStrokes = true;
    private String attribute;
    private BrewerPalette palette;
    private String label = "";
    private Color labelColor = Color.black;
    private java.awt.Font font = new java.awt.Font(java.awt.Font.SANS_SERIF,
	    java.awt.Font.PLAIN, 10);
    private ArrayList<String> titles = new ArrayList<String>();

    public ClassifiedStyleCreator() {
	Set<Function> funcs = CommonFactoryFinder.getFunctions(GeoTools
		.getDefaultHints());
	ArrayList<String> fNames = new ArrayList<String>();
	for (Function func : funcs) {
	    if (func instanceof ClassificationFunction) {
		classifiers.add(func);
		fNames.add(func.getName());
	    }
	}
	setPalette(DEFAULT_PALETTE);
	setNumberOfClasses(DEFAULT_NUMBER_OF_CLASSES);
	setClassifier(DEFAULT_CLASSIFIER);
    }

    public Style getStyle(SimpleFeatureCollection collection, String attribute) {
	Style ret = sb.createStyle();
	if(attribute==null || attribute.isEmpty()) {
	    return ret;
	}
	
	if (colors.isEmpty()) {
	    Color[] c = palette.getColors(numberOfClasses);
	    colors.addAll(Arrays.asList(c));
	}
	SimpleFeatureType schema = collection.getSchema();
	Classifier groups = classify(collection, attribute, schema);
	if (groups instanceof RangedClassifier) {
	    RangedClassifier rcGroups = (RangedClassifier) groups;
	    if (rcGroups.getSize() != numberOfClasses) {
		throw new RuntimeException("incorrect number of classes "
			+ rcGroups.getSize() + " expected " + numberOfClasses);
	    }

	    Geometries geomType = Geometries
		    .getForBinding((Class<? extends Geometry>) schema
			    .getGeometryDescriptor().getType().getBinding());
	    titles  = new ArrayList<String>(numberOfClasses);
	    Rule[] rules = new Rule[numberOfClasses];
	    for (int i = 0; i < numberOfClasses; i++) {
		Symbolizer symbolizer = null;
		Filter filter = Filter.INCLUDE;
		Object min2 = rcGroups.getMin(i);
		String min;
		if(min2!=null) {
		    min = min2.toString();
		}else {
		    min="";
		}
		Object max2 = rcGroups.getMax(i);
		String max;
		if(max2!=null) {
		    max = max2.toString();
		}else {
		    max="";
		}
		if (min.contains("[")) {// unique classifier set
		    // TODO handle unique sets
		} else {
		    PropertyName property = ff.property(attribute);

		    if (!min.isEmpty() && !max.isEmpty()) { // between
			titles.add(min+" - "+max);
			Literal lMin = ff.literal(Double.parseDouble(min));
			Literal lMax = ff.literal(Double.parseDouble(max));
			filter = ff.between(property, lMin, lMax);
		    } else if (min.isEmpty()) { // less than max
			titles.add("< "+max);
			Literal lMax = ff.literal(Double.parseDouble(max));
			filter = ff.less(property, lMax);
		    } else if (max.isEmpty()) { // greater than min
			titles.add(">= "+min);
			Literal lMin = ff.literal(Double.parseDouble(min));
			filter = ff.greaterOrEqual(property, lMin);
		    }
		}
		Color border;
		if (isColoredStrokes()) {
		    border = colors.get(i);
		} else {
		    border = Color.black;
		}
		switch (geomType) {
		case POINT:
		case MULTIPOINT:
		    symbolizer = sb.createPolygonSymbolizer(colors.get(i),
			    border, 1);
		    break;
		case LINESTRING:
		case MULTILINESTRING:
		    symbolizer = sb.createLineSymbolizer(colors.get(i));
		    break;
		case POLYGON:
		case MULTIPOLYGON:
		case GEOMETRY:
		    symbolizer = sb.createPolygonSymbolizer(colors.get(i),
			    border, 1);
		    break;
		case GEOMETRYCOLLECTION:
		    throw new IllegalArgumentException(
			    "Classifier doesn't support GeometryCollections");

		}
		rules[i] = sb.createRule(symbolizer);

		rules[i].setFilter(filter);
	    }
	    FeatureTypeStyle fts = sb.createFeatureTypeStyle("Feature", rules);
	    if (isLabeled()) {
		TextSymbolizer text = sb.createTextSymbolizer(labelColor,
			sb.createFont(font), label);
		fts.rules().add(sb.createRule(text));
	    }
	    ret.featureTypeStyles().add(fts);
	}
	return ret;
    }

    private Classifier classify(SimpleFeatureCollection collection,
	    String attribute, SimpleFeatureType schema) {
	if (attribute == null || attribute.isEmpty()) {
	    return null;

	}

	this.attribute = attribute;

	Function classify = ff.function(classifier.getName(),
		ff.property(attribute), ff.literal(numberOfClasses));
	Classifier groups = (Classifier) classify.evaluate(collection);
	return groups;

    }

    public void setClassifier(Function defaultClassifier) {

	this.classifier = defaultClassifier;
    }

    public void setClassifier(String name) {
	for (Function f : classifiers) {
	    if (f.getName().equalsIgnoreCase(name)) {
		setClassifier(f);
		return;
	    }
	}
    }

    public Function getClassifier() {
	return classifier;
    }

    public String getClassifierName() {
	return classifier.getName();
    }

    /**
     * @return the numberOfClasses
     */
    public int getNumberOfClasses() {
	return numberOfClasses;
    }

    /**
     * @param numberOfClasses
     *            the numberOfClasses to set
     */
    public void setNumberOfClasses(int numberOfClasses) {
	if (numberOfClasses < 2)
	    numberOfClasses = 2;
	this.numberOfClasses = numberOfClasses;
	// check if there are enough colors with the current pallete
	setPalette(paletteName);
    }

    /**
     * @return the paletteName
     */
    public String getPaletteName() {
	return paletteName;
    }

    public BrewerPalette getPalette() {
	return palette;
    }

    /**
     * @param palette
     *            the palette to set
     */
    public void setPalette(String palleteName) {
	this.paletteName = palleteName;
	palette = brewer.getPalette(palleteName);
	if (numberOfClasses > palette.getMaxColors()) {
	    System.out.println("reset "+numberOfClasses+" to "+palette.getMaxColors());
	    numberOfClasses = palette.getMaxColors();
	} else if (numberOfClasses < palette.getMinColors()) {
	    System.out.println("reset "+numberOfClasses+" to "+palette.getMinColors());
	    numberOfClasses = palette.getMinColors();
	}
	colors = Arrays.asList(palette.getColors(numberOfClasses));
    }

    /**
     * @return the colors
     */
    public List<Color> getColors() {
	return colors;
    }

    /**
     * @return the labeled
     */
    public boolean isLabeled() {
	return labeled;
    }

    /**
     * @param label
     *            the label to set (or a null or empty string to turn of labels)
     */
    public void setLabelAttribute(String label) {
	if(label==null||label.isEmpty()) {
	    this.label = "";
	    this.labeled = false;
	}
	
	this.label = label;
	this.labeled = true;
    }

    /**
     * @return the coloredStrokes
     */
    public boolean isColoredStrokes() {
	return coloredStrokes;
    }

    /**
     * @param coloredStrokes
     *            the coloredStrokes to set
     */
    public void setColoredStrokes(boolean coloredStrokes) {
	this.coloredStrokes = coloredStrokes;
    }

    /**
     * @return the labelColor
     */
    public Color getLabelColor() {
	return labelColor;
    }

    /**
     * @param labelColor
     *            the labelColor to set
     */
    public void setLabelColor(Color labelColor) {
	this.labelColor = labelColor;
    }

    /**
     * @return the font
     */
    public java.awt.Font getFont() {
	return font;
    }

    /**
     * @param font2
     *            the font to set
     */
    public void setFont(java.awt.Font font2) {
	this.font = font2;
    }

    public List<String> getPaletteNames() {
	// TODO Auto-generated method stub
	return Arrays.asList(brewer.getPaletteNames());
    }

    public List<String> getFunctionNames() {
	ArrayList<String> ret = new ArrayList<String>();

	for (Function f : classifiers) {
	    ret.add(f.getName());

	}
	return ret;
    }

    public List<String> getTitles() {
	// TODO Auto-generated method stub
	return titles;
    }

}
