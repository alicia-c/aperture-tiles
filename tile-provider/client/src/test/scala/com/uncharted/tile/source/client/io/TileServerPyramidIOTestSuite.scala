/*
 * Copyright (c) 2015 Uncharted Software Inc.
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
package com.uncharted.tile.source.client.io



import java.lang.{Integer => JavaInt}
import java.util.{Arrays => JavaArrays, List => JavaList}

import org.apache.avro.file.CodecFactory
import org.json.JSONObject

import scala.collection.JavaConverters._
import scala.util.Try

import org.scalatest.FunSuite

import com.oculusinfo.binning.TileIndex
import com.oculusinfo.binning.io.serialization.impl.PrimitiveAvroSerializer
import com.oculusinfo.binning.util.JsonUtilities




/**
 * Test the TileServerPyramidIO to make sure it works.
 */
class TileServerPyramidIOTestSuite extends FunSuite {
  // This test uses a remote server, rather than starting one itself.  Consequently, it is of course ignored, and
  // is intended to be run manually when said server is known to be up.
  ignore("Test on-demand tiling") {
    def time[T] (fcn: () => T): (T, Double) = {
      val start = System.nanoTime()
      val result = fcn()
      val end = System.nanoTime()
      (result, (end-start)/1000000.0)
    }
    val io = new TileServerPyramidIO("hadoop-s1", 1000*60*60)
    try {
      val configuration = new JSONObject(
        s"""{
           |  "type": "on-demand",
           |  "algorithm": "binning",
           |  "oculus": {
           |    "binning": {
           |      "name": "bitcoin",
           |      "description": "Live demo of bitcoin tiling",
           |      "levels": "0,1,2,3,4,5,6,7,8,9,10,11,12",
           |      "tileWidth": 256,
           |      "tileHeight": 256,
           |      "partitions": 128,
           |      "index": {
           |        "type": "cartesian",
           |        "field": ["logamount", "time"]
           |      },
           |      "value": {
           |        "type": "count",
           |        "valueType": "int"
           |      },
           |      "projection": {
           |        "type": "EPSG:4326",
           |        "autobounds": true
           |      },
           |      "source": {
           |        "location": "hdfs://hadoop-s1/xdata/data/bitcoin/sc2013/Bitcoin_Transactions_Datasets_20130410.tsv",
           |        "partitions": 128
           |      },
           |      "parsing": {
           |        "separator": "\t",
           |        "transaction": {
           |          "index":  0,
           |          "fieldType": "int"
           |        },
           |        "source": {
           |          "index": 1,
           |          "fieldType": "int"
           |        },
           |        "destination": {
           |          "index": 2,
           |          "fieldType": "int"
           |        },
           |        "time": {
           |          "index": 3,
           |          "fieldType": "date",
           |          "dateFormat": "yyyy-MM-dd HH:mm:ss"
           |        },
           |        "amount": {
           |          "index": 4
           |        },
           |        "logamount": {
           |          "index": 4,
           |          "fieldScaling": "log",
           |          "fieldAggregation": "log",
           |          "fieldBase": 10
           |        }
           |      }
           |    }
           |  },
           |  "data": {
           |    "oculus": {
           |      "binning": {
           |      }
           |    }
           |  }
           |}""".stripMargin)

      val tableName = "bitcoin"
      io.initializeForRead(tableName, 256, 256, JsonUtilities.jsonObjToProperties(configuration))
      val serializer = new PrimitiveAvroSerializer[JavaInt](classOf[JavaInt], CodecFactory.bzip2Codec())
      val level0Indices = JavaArrays.asList(new TileIndex(0, 0, 0))
      val (level0, level0Time) = time(() => Try(io.readTiles[JavaInt](tableName, serializer, level0Indices).asScala))
      val level1Indices: JavaList[TileIndex] = (for (x <- 0 to 1; y <- 0 to 1) yield (new TileIndex(1, x, y))).toList.asJava
      val (level1, level1Time) = time(() => Try(io.readTiles[JavaInt](tableName, serializer, level1Indices).asScala))
      val level2Indices: JavaList[TileIndex] = (for (x <- 0 to 3; y <- 0 to 3) yield (new TileIndex(2, x, y))).toList.asJava
      val (level2, level2Time) = time(() => Try(io.readTiles[JavaInt](tableName, serializer, level2Indices).asScala))
      val level3Indices: JavaList[TileIndex] = (for (x <- 0 to 7; y <- 0 to 7) yield (new TileIndex(3, x, y))).toList.asJava
      val (level3, level3Time) = time(() => Try(io.readTiles[JavaInt](tableName, serializer, level3Indices).asScala))

      println("Retrieved "+level0.get.size+" tiles for level 0 in "+(level0Time/1000.0)+" seconds")
      println("Retrieved "+level1.get.size+" tiles for level 1 in "+(level1Time/1000.0)+" seconds")
      println("Retrieved "+level2.get.size+" tiles for level 2 in "+(level2Time/1000.0)+" seconds")
      println("Retrieved "+level3.get.size+" tiles for level 3 in "+(level3Time/1000.0)+" seconds")

      assert(level0.get.size === 1)
      assert(level1.get.size === 4)
      assert(level2.get.size === 16)
      assert(level3.get.size === 64)
    } finally {
    }
  }
}