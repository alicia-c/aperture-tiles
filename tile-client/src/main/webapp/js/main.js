/*
 * Copyright (c) 2014 Oculus Info Inc.
 * http://www.oculusinfo.com/
 * 
 * Released under the MIT License.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

require(['./FileLoader',
         './map/Map',
         './layer/view/server/ServerLayer',
         './layer/controller/LayerControls',
         './layer/controller/UIMediator',
         './layer/view/client/CarouselLayer',
         './layer/view/client/data/LayerInfoLoader',
         './layer/view/client/data/DataTracker'],
        function (FileLoader,
                  Map,
                  ServerLayer,
                  LayerControls,
                  ServerLayerUiMediator,
                  Carousel,
                  LayerInfoLoader,
                  DataTracker
                  ) {
            "use strict";

            // Note that, for the purposes of these id's, "." is webapps, where
            // main.js (this file) runs, and not webapps/js, where this file 
            // actually is
            var sLayerFileId = "./data/layers.json",
                mapFileId = "./data/map.json",
                cLayerFileId = "./data/renderLayers.json";

            // Load all our UI configuration data before trying to bring up the ui
            FileLoader.loadJSONData(mapFileId, sLayerFileId, cLayerFileId, function (jsonDataMap) {
                // We have all our data now; construct the UI.
                var worldMap,
                    serverLayers,
                    mapLayerState
                    //i,
                    //layerId,
                    //layerName,
                    //dataTracker,
                    //carousel;
					;
                // Create world map and axes from json file under mapFileId
                worldMap = new Map("map", jsonDataMap[mapFileId]);

                // Set up a debug layer
                // debugLayer = new DebugLayer();
                // debugLayer.addToMap(worldMap);

                // Set up client-rendered layers
				/*
                renderLayerSpecs = jsonDataMap[cLayerFileId];

				
                for (i=0; i<renderLayerSpecs.length; ++i) {
                    renderLayerSpec = FileLoader.downcaseObjectKeys(renderLayerSpecs[i]);
                    layerId = renderLayerSpec.layer;

                    layerName = renderLayerSpec.name;
                    if (!layerName) {
                        layerName = layerId;
                    }
                }

				
                LayerInfoLoader.getLayerInfo( renderLayerSpec, function( layerInfo ) {
                    dataTracker = new DataTracker(layerInfo);
                    carousel = new Carousel( {
                        map: worldMap.map,
                        views: [
                            {
                                id: "red",
                                dataTracker: dataTracker,
                                renderer: tileScoreRenderer
                            },
                            {
                                id: "blue",
                                dataTracker: dataTracker,
                                renderer: tileScoreRendererOther
                            }
                        ]});
                    carousel.dummy = 0; // to shut jslint up
                });
				*/



                // Set up server-rendered display layers
                serverLayers = new ServerLayer(FileLoader.downcaseObjectKeys(jsonDataMap[sLayerFileId] ));
                serverLayers.addToMap(worldMap);

                // Populate the map layer state object with server layer data, and enable
                // listeners that will push state changes into the layers.
                mapLayerState = {};
                new ServerLayerUiMediator().initialize(mapLayerState, serverLayers, worldMap);

                // Bind layer controls to the state model.
                new LayerControls().initialize(mapLayerState);
                

                // Trigger the initial resize event to resize everything
                $(window).resize();
                
                /*
                setTimeout(function () {
                    console.log(Class.getProfileInfo());
                }, 10000);
                */
            });
        });
