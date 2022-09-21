/*
 * Copyright 2020-2022 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 *
 * Code is based on abandoned project by Trung (https://github.com/trung/InMemoryJavaCompiler).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package eu.mihosoft.jcompiler;

/**
 * Indicates a compilation error.
 */
public class CompilationException extends RuntimeException {
	private static final long serialVersionUID = 5272588827551900536L;

	/**
	 * Creates a new instance of this class.
	 * @param msg message of this exception
	 */
	public CompilationException(String msg) {
		super(msg);
	}

	/**
	 *  Creates a new instance of this class.
	 * @param msg message of this exception
	 * @param ex exception that caused this compilation exception
	 */
	public CompilationException(String msg, Exception ex) {
		super(msg, ex);
	}

}
