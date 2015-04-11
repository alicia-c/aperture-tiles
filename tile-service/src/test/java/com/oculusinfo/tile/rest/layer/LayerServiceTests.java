/*
 * Copyright (c) 2014 Oculus Info Inc. http://www.oculusinfo.com/
 *
 * Released under the MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oculusinfo.tile.rest.layer;

import com.oculusinfo.binning.io.DefaultPyramidIOFactoryProvider;
import com.oculusinfo.binning.io.PyramidIO;
import com.oculusinfo.binning.io.impl.FileBasedPyramidIO;
import com.oculusinfo.binning.io.serialization.DefaultTileSerializerFactoryProvider;
import com.oculusinfo.binning.io.serialization.TileSerializer;
import com.oculusinfo.factory.providers.FactoryProvider;
import com.oculusinfo.tile.init.providers.*;
import com.oculusinfo.tile.rendering.LayerConfiguration;
import com.oculusinfo.tile.rest.QueryParamDecoder;

import org.json.JSONObject;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LayerServiceTests {

    private static final Logger LOGGER = LoggerFactory.getLogger( LayerServiceTests.class );
    protected LayerService _layerService;

	@Before
	public void setup () {
		try {
			String configFile = "res:///unit-test-config.json";
            Set<FactoryProvider<PyramidIO>> tileIoSet = new HashSet<>();
            tileIoSet.addAll( Arrays.asList( DefaultPyramidIOFactoryProvider.values() ) );
            Set<FactoryProvider<TileSerializer<?>>> serializerSet = new HashSet<>();
            serializerSet.addAll( Arrays.asList( DefaultTileSerializerFactoryProvider.values() ) );
            FactoryProvider<LayerConfiguration> layerConfigurationProvider = new StandardLayerConfigurationProvider(
                new StandardPyramidIOFactoryProvider( tileIoSet ),
                new StandardTilePyramidFactoryProvider(),
                new StandardTileSerializerFactoryProvider(serializerSet),
                new StandardImageRendererFactoryProvider(),
                new StandardTileTransformerFactoryProvider(),
				new StandardValueTransformerFactoryProvider()
            );
            _layerService = new LayerServiceImpl( configFile, layerConfigurationProvider );
		} catch (Exception e) {
			throw e;
		}
	}

	@Test
	public void getLayerJSONsTest() {
		List< JSONObject > jsons = _layerService.getLayerJSONs();
		assert( jsons.size() == 2 );
	}

	@Test
	public void getLayerJsonTest() {
		JSONObject layer0 = _layerService.getLayerJSON( "test-layer0" );
		JSONObject layer1 = _layerService.getLayerJSON( "test-layer1" );
		assert( layer0 != null );
		assert( layer1 != null );
	}

	@Test
	public void getLayerIdsTest() {
		List< String > layerIds = _layerService.getLayerIds();
		assert( layerIds.get(0).equals( "test-layer0" ) );
		assert( layerIds.get(1).equals( "test-layer1" ) );
	}

	@Test
	public void getLayerConfigurationTest() {
		LayerConfiguration layerConfig0 = _layerService.getLayerConfiguration( "test-layer0", null );
		LayerConfiguration layerConfig1 = _layerService.getLayerConfiguration( "test-layer1", null );
		try {
			PyramidIO io = layerConfig1.produce( PyramidIO.class );
			assert( io instanceof FileBasedPyramidIO );

		} catch ( Exception e ) {

		}

		System.out.println( layerConfig0.getPropertyValue( LayerConfiguration.LAYER_ID ) );
		System.out.println( layerConfig1.getPropertyValue( LayerConfiguration.LAYER_ID ) );
		assert( layerConfig0.getPropertyValue( LayerConfiguration.LAYER_ID ).equals( "test-layer0" ) );
		assert( layerConfig1.getPropertyValue( LayerConfiguration.LAYER_ID ).equals( "test-layer1" ) );
		assert( layerConfig0.getPropertyValue( LayerConfiguration.DATA_ID ).equals( "test-layer0-data" ) );
	}

	@Test
	public void saveAndGetLayerStateTest() {
		try {
			// create overrides
			JSONObject override = QueryParamDecoder.decode( "renderer.ramp=cool&renderer.coarseness=3" );
			String stateId = _layerService.saveLayerState( "test-layer0", override );
			JSONObject overrideState = _layerService.getLayerState( "test-layer0", stateId );
			assert( overrideState.getJSONObject( "renderer" ).getString( "ramp" ).equals( "cool" ) );
			assert( overrideState.getJSONObject( "renderer" ).getString( "coarseness" ).equals( "3" ) );
		} catch ( Exception e ) {
			LOGGER.error( "Error overriding layer config", e );
		}
	}

	@Test
	public void getLayerStatesTest() {
		try {
			// save state
			JSONObject override = QueryParamDecoder.decode( "renderer.ramp=cool&renderer.coarseness=3&valueTransform.type=linear" );
			String stateId = _layerService.saveLayerState( "test-layer0", override );
			// ensure default is that set in config, NOT propertyValue defaults (unless unconfigured)
			JSONObject states = _layerService.getLayerStates( "test-layer0" );
			// default
			assert( states.getJSONObject( "default" ).getJSONObject( "renderer" ).getString( "ramp" ).equals( "hot" ) );
			assert( states.getJSONObject( "default" ).getJSONObject( "valueTransform" ).getString( "type" ).equals( "log10" ) );
			// saved
			assert( states.getJSONObject( stateId  ).getJSONObject( "renderer" ).getString( "ramp" ).equals( "cool" ) );
			assert( states.getJSONObject( stateId ).getJSONObject( "renderer" ).getString( "coarseness" ).equals( "3" ) );
			assert( states.getJSONObject( stateId ).getJSONObject( "valueTransform" ).getString( "type" ).equals( "linear" ) );
		} catch ( Exception e ) {
			LOGGER.error( "Error overriding layer config", e );
		}
	}

}
