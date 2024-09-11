/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.javastack.scm.auth.htpasswd;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.javastack.scm.auth.htpasswd.HtpasswdConfig;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

public class HtpasswdConfigTest {
  @Test
  public void testIsValid() {
    HtpasswdConfig config = new HtpasswdConfig();
    config.setHtpasswdFilepath(".htpasswd");
    config.setHtgroupFilepath(".htgroup");
    config.setHtmetaFilepath(".htmail");
    assertTrue(config.isValid());
    config.setHtpasswdFilepath(null);
    assertFalse(config.isValid());
    config.setHtpasswdFilepath("");
    assertFalse(config.isValid());
  }
}
