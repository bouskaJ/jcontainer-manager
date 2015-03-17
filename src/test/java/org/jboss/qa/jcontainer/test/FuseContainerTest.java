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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jboss.qa.jcontainer.Container;
import org.jboss.qa.jcontainer.fuse.FuseClient;
import org.jboss.qa.jcontainer.fuse.FuseConfiguration;
import org.jboss.qa.jcontainer.fuse.FuseContainer;
import org.jboss.qa.jcontainer.fuse.FuseUser;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class FuseContainerTest extends ContainerTest {

	private static final String GOOD_CMD = "osgi:version";
	private static final String BAD_FORMAT_CMD = "osgi:xxx";
	private static final String BAD_RESULT_CMD = "osgi:install xxx";

	private static final String CONFIG = "my.config";
	private static final String PROP_NAME = "my-prop";
	private static final String PROP_VAL = "my-value";

	protected static Container container;

	@BeforeClass
	public static void beforeClass() throws Exception {
		final FuseConfiguration conf = FuseConfiguration.builder().directory(FUSE_HOME).xmx("2g").build();
		container = new FuseContainer<>(conf);
		final FuseUser user = new FuseUser();
		user.setUsername(conf.getUsername());
		user.setPassword(conf.getPassword());
		user.addRoles("admin", "SuperUser");
		container.addUser(user);
		container.start();
	}

	@AfterClass
	public static void afterClass() throws Exception {
		if (container != null) {
			container.stop();
		}
	}

	@Before
	public void before() throws Exception {
		System.out.println(getConfigFile(CONFIG).getAbsolutePath());
		getConfigFile(CONFIG).delete();
	}

	@Test
	public void successCmdTest() throws Exception {
		container.getClient().execute(GOOD_CMD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void badResultCmdTest() throws Exception {
		container.getClient().execute(BAD_RESULT_CMD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void badFormatCmdTest() throws Exception {
		container.getClient().execute(BAD_FORMAT_CMD);
	}

	@Test
	public void successBatchTest() throws Exception {
		final List<String> cmds = new ArrayList<>();
		cmds.add(String.format("config:edit %s", CONFIG));
		cmds.add(String.format("config:propset %s %s", PROP_NAME, PROP_VAL));
		cmds.add("config:update");
		container.getClient().execute(cmds);
		assertTrue(getConfigFile(CONFIG).exists());
	}

	@Ignore("Batch rollback is not supported in JBoss Fuse")
	@Test
	public void failBatchTest() throws Exception {
		final List<String> cmds = new ArrayList<>();
		try {
			cmds.add(String.format("config:edit %s", CONFIG));
			cmds.add(String.format("config:propset %s %s", PROP_NAME, PROP_VAL));
			cmds.add("config:update");
			cmds.add(BAD_FORMAT_CMD);
			container.getClient().execute(cmds);
		} catch (Exception e) {
			assertFalse(getConfigFile(CONFIG).exists());
		}
	}

	@Test
	public void standaloneClientTest() throws Exception {
		try (FuseClient client = new FuseClient<>(FuseConfiguration.builder().build())) {
			client.execute(GOOD_CMD);
			assertNotNull(client.getCommandResult());
		}
	}

	private File getConfigFile(String name) {
		return new File(container.getConfiguration().getDirectory(), String.format("etc%s%s.cfg",
				File.separator, name));
	}
}

