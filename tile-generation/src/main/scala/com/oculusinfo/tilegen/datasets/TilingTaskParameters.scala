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

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oculusinfo.tilegen.datasets



import java.util.{List => JavaList}

import scala.collection.JavaConverters._

import com.oculusinfo.factory.{ConfigurationProperty, ConfigurableFactory}
import com.oculusinfo.factory.properties.{ListProperty, IntegerProperty, BooleanProperty, StringProperty}
import com.oculusinfo.tilegen.util.OptionsFactoryMixin


/**
 * A consolidated location for the random parameters associated with tiling tasks, so as to make them amenable to
 * factory construction
 *
 * @param name The basic name of the tile pyramid to be tiled
 * @param description A description of the tile pyramid to be tiled
 * @param prefix A prefix to be prepended to the basic name, to differentiate different runs of a tiling task
 * @param levels A list of levels to be tiled by this configuration
 * @param tileWidth The number of bins into which each tile created is divided horizontally
 * @param tileHeight The number of bins into which each tile created is divided vertically
 * @param consolidationPartitions The number of partitions into which to consolidate data when performign reduce operations
 */
case class TilingTaskParameters (name: String,
                                 description: String,
                                 prefix: Option[String],
                                 levels: Seq[Seq[Int]],
                                 tileWidth: Int,
                                 tileHeight: Int,
                                 consolidationPartitions: Option[Int])
{
}


object TilingTaskParametersFactory {
	val NAME_PROPERTY = new StringProperty("name", "The basic root name of the tile pyramid to be tiled", "")
	val DESC_PROPERTY = new StringProperty("description", "A description of the tile pyramid to be tiled.  This will be put in the pyramid metaData.", "")
	val PREFIX_PROPERTY = new StringProperty("prefix", "A prefix to be prepended to the basic name, so as to differentiate different attempts to tile the same data.", "")
	val LEVELS_PROPERTY = new ListProperty(new StringProperty("levels", "A comma-separated lists of levels or ranges of levels to tile together", "0"),
	                                       "levels", "A list of groups of levels to tile together.  Groups are comma-separated lists of levels or ranges of levels")
	val TILE_WIDTH_PROPERTY = new IntegerProperty("tileWidth", "The width of created tiles, in bins", 256)
	val TILE_HEIGHT_PROPERTY = new IntegerProperty("tileHeight", "The height of created tiles, in bins", 256)
	val PARTITIONS_PROPERTY = new IntegerProperty("consolidationPartitions", "The number of partitions into which to consolidate data when performign reduce operations", 0)
}
class TilingTaskParametersFactory (parent: ConfigurableFactory[_], path: JavaList[String])
		extends ConfigurableFactory[TilingTaskParameters](classOf[TilingTaskParameters], parent, path, true)
		with OptionsFactoryMixin[TilingTaskParameters]
{
	import TilingTaskParametersFactory._
	addProperty(NAME_PROPERTY)
	addProperty(DESC_PROPERTY)
	addProperty(PREFIX_PROPERTY)
	addProperty(LEVELS_PROPERTY)
	addProperty(TILE_WIDTH_PROPERTY)
	addProperty(TILE_HEIGHT_PROPERTY)
	addProperty(PARTITIONS_PROPERTY)

	private def parseLevels (levelsDescriptions: Seq[String]): Seq[Seq[Int]] = {
		levelsDescriptions.map(levelSet =>
			levelSet.split(',').map(levelRange =>
				{
					val extrema = levelRange.split('-')

					if ((0 == extrema.size) || (levelRange=="")) Seq[Int]()
					else if (1 == extrema.size) Seq[Int](extrema(0).trim.toInt)
					else Range(extrema(0).trim.toInt, extrema(1).trim.toInt+1).toSeq
				}
			).fold(Seq[Int]())(_ ++ _)
		)
	}

	override protected def create(): TilingTaskParameters = {
		new TilingTaskParameters(getPropertyValue(NAME_PROPERTY),
		                         getPropertyValue(DESC_PROPERTY),
		                         optionalGet(PREFIX_PROPERTY),
		                         parseLevels(getPropertyValue(LEVELS_PROPERTY).asScala),
		                         getPropertyValue(TILE_WIDTH_PROPERTY),
		                         getPropertyValue(TILE_HEIGHT_PROPERTY),
		                         optionalGet(PARTITIONS_PROPERTY).map(_.intValue()))
	}
}