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
package com.oculusinfo.annotation.init.providers;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.oculusinfo.annotation.filter.AnnotationFilter;
import com.oculusinfo.annotation.filter.AnnotationFilterFactory;
import com.oculusinfo.factory.ConfigurableFactory;
import com.oculusinfo.factory.providers.FactoryProvider;
import com.oculusinfo.factory.providers.StandardUberFactoryProvider;

import java.util.List;
import java.util.Set;


@Singleton
public class StandardAnnotationFilterFactoryProvider extends StandardUberFactoryProvider<AnnotationFilter> {
	@Inject
	public StandardAnnotationFilterFactoryProvider (Set<FactoryProvider<AnnotationFilter>> providers) {
		super(providers);
	}

	@Override
	public ConfigurableFactory<AnnotationFilter> createFactory (String name,
	                                                            ConfigurableFactory<?> parent,
	                                                            List<String> path) {
		return new AnnotationFilterFactory(name, parent, path, createChildren(parent, path));
	}
}
