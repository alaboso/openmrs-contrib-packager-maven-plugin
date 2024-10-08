/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, you can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark, and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.maven.plugins.packager.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.semver4j.Semver;
import org.semver4j.SemverException;

/**
 * Mojo to validate content properties file - ensures the properties are only within allowed ranges.
 * Values like "latest" and "next" are not permitted.
 */
@Mojo(name = "validate-content-package")
public class ValidateContentPackageMojo extends AbstractMojo {
	
	// List of terms that are not valid versions
	private static final String[] INVALID_TERMS = { "latest", "next" };
	
	/**
	 * The full path to the content.properties file, including the filename. This file is similar to
	 * distro.properties used in the validation process. For example:
	 * "{project.basedir}/content.properties".
	 */
	@Parameter(property = "sourceFile")
	protected String sourceFile;
	
	/**
	 * Executes the property validation.
	 *
	 * @throws MojoExecutionException if an error occurs during validation
	 */
	@Override
	public void execute() throws MojoExecutionException {
		validateProperties();
	}
	
	/**
	 * Validates the properties in the given file.
	 *
	 * @throws MojoExecutionException if an error occurs while reading the file
	 */
	private void validateProperties() throws MojoExecutionException {
		if (sourceFile == null) {
			throw new MojoExecutionException(
			        "sourceFile is missing. A valid path and file for content.properties are required for this plugin.");
		}
		try (InputStream inputStream = new FileInputStream(sourceFile)) {
			Properties properties = new Properties();
			properties.load(inputStream);
			
			if (!properties.containsKey("name") || !properties.containsKey("version")) {
				throw new MojoExecutionException("The properties file must contain both 'name' and 'version' keys.");
			}
			
			for (String key : properties.stringPropertyNames()) {
				String value = properties.getProperty(key);
				
				if (key.startsWith("omod") || key.startsWith("owa") || key.startsWith("spa.frontend")
				        || "version".equalsIgnoreCase(key)) {
					if (!isValid(value)) {
						throw new MojoExecutionException("Invalid SemVer format for key: " + key + ", value: " + value);
					}
				}
			}
		}
		catch (Exception e) {
			throw new MojoExecutionException("Could not validate configuration file '" + sourceFile + "': " + e.getMessage(),
			        e);
		}
	}
	
	/**
	 * Validates whether a given value is a valid SemVer expression. If the range expression
	 * satisfies(true or false), it is considered valid. If an exception occurs, the range
	 * expression is invalid.
	 *
	 * @param versionOrRange the value to validate
	 * @return true if the value is a valid SemVer expression or range, false otherwise
	 */
	protected boolean isValid(String versionOrRange) {
		
		for (String term : INVALID_TERMS) {
			if (term.equalsIgnoreCase(versionOrRange)) {
				return false;
			}
		}
		
		try {
			Semver semver = new Semver("0.0.0");
			semver.satisfies(versionOrRange);
			return true;
		}
		catch (SemverException e) {
			return false;
		}
	}
}
