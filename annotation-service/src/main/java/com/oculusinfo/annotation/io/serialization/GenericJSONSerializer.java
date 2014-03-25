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
package com.oculusinfo.annotation.io.serialization;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.oculusinfo.annotation.index.*;
import com.oculusinfo.binning.*;

public abstract class GenericJSONSerializer<T> implements AnnotationSerializer<T> {
	
    private static final long serialVersionUID = 2617903534522413550L;

    abstract protected T getValue (Object bin) throws JSONException;
	abstract protected JSONObject translateToJSON (T value);

	protected GenericJSONSerializer () {

	}

	public String getFileExtension(){
		return "json";
	}

	@Override
	public T deserialize (InputStream rawData){

		String jsonString = convertStreamToString(rawData);
		
		try {	
			
			JSONObject json = new JSONObject( jsonString );		
			return getValue( json );

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	private String convertStreamToString(java.io.InputStream is) {
		try {
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			java.util.Scanner s = new java.util.Scanner(isr);
			s.useDelimiter("\\A");
			String result = s.hasNext() ? s.next() : "";
			s.close();
			return result;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;

	}

	@Override
	public void serialize (T data, OutputStream stream) throws IOException {

		try {
			  
			JSONObject jsonEntry = translateToJSON( data );

			OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
			writer.write(jsonEntry.toString());
			writer.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
