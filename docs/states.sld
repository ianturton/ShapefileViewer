<?xml version="1.0" encoding="UTF-8"?><sld:UserStyle xmlns="http://www.opengis.net/sld" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
  <sld:Name>Default Styler</sld:Name>
  <sld:FeatureTypeStyle>
    <sld:Name>name</sld:Name>
    <sld:FeatureTypeName>Feature</sld:FeatureTypeName>
    <sld:Rule>
      <ogc:Filter>
        <ogc:PropertyIsBetween>
          <ogc:PropertyName>PUBTRANS</ogc:PropertyName>
          <ogc:LowerBoundary>
            <ogc:Literal>971.0</ogc:Literal>
          </ogc:LowerBoundary>
          <ogc:UpperBoundary>
            <ogc:Literal>18092.0</ogc:Literal>
          </ogc:UpperBoundary>
        </ogc:PropertyIsBetween>
      </ogc:Filter>
      <sld:PolygonSymbolizer>
        <sld:Fill>
          <sld:CssParameter name="fill">#DE77AE</sld:CssParameter>
        </sld:Fill>
        <sld:Stroke/>
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <ogc:Filter>
        <ogc:PropertyIsBetween>
          <ogc:PropertyName>PUBTRANS</ogc:PropertyName>
          <ogc:LowerBoundary>
            <ogc:Literal>18092.0</ogc:Literal>
          </ogc:LowerBoundary>
          <ogc:UpperBoundary>
            <ogc:Literal>66653.0</ogc:Literal>
          </ogc:UpperBoundary>
        </ogc:PropertyIsBetween>
      </ogc:Filter>
      <sld:PolygonSymbolizer>
        <sld:Fill>
          <sld:CssParameter name="fill">#F1B6DA</sld:CssParameter>
        </sld:Fill>
        <sld:Stroke/>
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <ogc:Filter>
        <ogc:PropertyIsBetween>
          <ogc:PropertyName>PUBTRANS</ogc:PropertyName>
          <ogc:LowerBoundary>
            <ogc:Literal>66653.0</ogc:Literal>
          </ogc:LowerBoundary>
          <ogc:UpperBoundary>
            <ogc:Literal>168814.0</ogc:Literal>
          </ogc:UpperBoundary>
        </ogc:PropertyIsBetween>
      </ogc:Filter>
      <sld:PolygonSymbolizer>
        <sld:Fill>
          <sld:CssParameter name="fill">#F7F7F7</sld:CssParameter>
        </sld:Fill>
        <sld:Stroke/>
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <ogc:Filter>
        <ogc:PropertyIsBetween>
          <ogc:PropertyName>PUBTRANS</ogc:PropertyName>
          <ogc:LowerBoundary>
            <ogc:Literal>168814.0</ogc:Literal>
          </ogc:LowerBoundary>
          <ogc:UpperBoundary>
            <ogc:Literal>538071.0</ogc:Literal>
          </ogc:UpperBoundary>
        </ogc:PropertyIsBetween>
      </ogc:Filter>
      <sld:PolygonSymbolizer>
        <sld:Fill>
          <sld:CssParameter name="fill">#B8E186</sld:CssParameter>
        </sld:Fill>
        <sld:Stroke/>
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <ogc:Filter>
        <ogc:PropertyIsBetween>
          <ogc:PropertyName>PUBTRANS</ogc:PropertyName>
          <ogc:LowerBoundary>
            <ogc:Literal>538071.0</ogc:Literal>
          </ogc:LowerBoundary>
          <ogc:UpperBoundary>
            <ogc:Literal>2113133.0</ogc:Literal>
          </ogc:UpperBoundary>
        </ogc:PropertyIsBetween>
      </ogc:Filter>
      <sld:PolygonSymbolizer>
        <sld:Fill>
          <sld:CssParameter name="fill">#4DAC26</sld:CssParameter>
        </sld:Fill>
        <sld:Stroke/>
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:TextSymbolizer>
        <sld:Label>
          <ogc:PropertyName>STATE_ABBR</ogc:PropertyName>
        </sld:Label>
        <sld:Font>
          <sld:CssParameter name="font-family">SansSerif</sld:CssParameter>
          <sld:CssParameter name="font-size">10.0</sld:CssParameter>
          <sld:CssParameter name="font-style">normal</sld:CssParameter>
          <sld:CssParameter name="font-weight">normal</sld:CssParameter>
        </sld:Font>
        <sld:LabelPlacement>
          <sld:PointPlacement>
            <sld:AnchorPoint>
              <sld:AnchorPointX>0.0</sld:AnchorPointX>
              <sld:AnchorPointY>0.5</sld:AnchorPointY>
            </sld:AnchorPoint>
          </sld:PointPlacement>
        </sld:LabelPlacement>
        <sld:Fill>
          <sld:CssParameter name="fill">#000000</sld:CssParameter>
        </sld:Fill>
      </sld:TextSymbolizer>
    </sld:Rule>
  </sld:FeatureTypeStyle>
</sld:UserStyle>
