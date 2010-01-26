/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: TraceListenerEx3.java 468644 2006-10-28 06:56:42Z minchau $
 */
 
package org.apache.xalan.trace;

/**
 * Extends TraceListenerEx2 but adds extensions trace events.
 * @xsl.usage advanced
 */
public interface TraceListenerEx3 extends TraceListenerEx2 {

	/**
	 * Method that is called when an extension event occurs.
	 * The method is blocking.  It must return before processing continues.
	 *
	 * @param ee the extension event.
	 */
	public void extension(ExtensionEvent ee);

	/**
	 * Method that is called when the end of an extension event occurs.
	 * The method is blocking.  It must return before processing continues.
	 *
	 * @param ee the extension event.
	 */
	public void extensionEnd(ExtensionEvent ee);

}

