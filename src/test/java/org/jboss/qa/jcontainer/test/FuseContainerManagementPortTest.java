/*
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.qa.jcontainer.test;

import static org.junit.Assert.assertNotNull;

import org.apache.commons.configuration.PropertiesConfiguration;

import org.jboss.qa.jcontainer.fuse.FuseClient;
import org.jboss.qa.jcontainer.fuse.FuseConfiguration;
import org.jboss.qa.jcontainer.fuse.FuseContainer;
import org.jboss.qa.jcontainer.fuse.FuseUser;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;

@RunWith(JUnit4.class)
public class FuseContainerManagementPortTest extends FuseContainerTest {

	protected static final Integer MANAGEMENT_PORT = 8102;

	@BeforeClass
	public static void beforeClass() throws Exception {
		final FuseConfiguration conf = FuseConfiguration.builder()
				.managementPort(MANAGEMENT_PORT).directory(FUSE_HOME).xmx("2g").build();
		container = new FuseContainer<>(conf);
		final FuseUser user = new FuseUser();
		user.setUsername(conf.getUsername());
		user.setPassword(conf.getPassword());
		user.addRoles("admin", "SuperUser");
		container.addUser(user);
		container.start();
	}

	@Test
	public void standaloneClientTest() throws Exception {
		try (FuseClient client = new FuseClient<>(FuseConfiguration.builder()
				.managementPort(MANAGEMENT_PORT).build())) {
			client.execute(GOOD_CMD);
			assertNotNull(client.getCommandResult());
		}
	}

	@AfterClass
	public static void afterClass() throws Exception {
		if (container != null) {
			container.stop();
			final File propsFile = new File(container.getConfiguration().getDirectory(),
					"etc" + File.separator + "org.apache.karaf.shell.cfg");
			final PropertiesConfiguration propConf = new PropertiesConfiguration(propsFile);
			propConf.setProperty("sshPort", FuseConfiguration.DEFAULT_MANAGEMENT_PORT);
			propConf.save();
		}
	}
}

