package org.openmrs.maven.plugins.packager.config;

import junit.framework.TestCase;
import org.junit.Assert;

public class AbstractPackagerConfigTest extends TestCase {

	/**
	 * @throws Exception
	 */
	public void testThatBuildArtifactsAreInExpectedDirectory() throws Exception {

		ConfigProject configProject = new ConfigProject("config-test-parent");

		configProject.executeGoal("clean", "-N");
		Assert.assertFalse(configProject.getTargetDir().exists());
		Assert.assertFalse(configProject.getPluginBuildDir().exists());

		configProject.executeGoal("compile", "-N", "-X");
		Assert.assertTrue(configProject.getTargetDir().exists());
		Assert.assertTrue(configProject.getPluginBuildDir().exists());
	}
}
