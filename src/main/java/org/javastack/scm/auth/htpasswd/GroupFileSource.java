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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GroupFileSource extends HtFileSource {
  private static final Logger logger = LoggerFactory.getLogger(GroupFileSource.class);
  private static final HashMap<String, GroupFileSource> INSTANCES = new HashMap<>();

  private final File file;
  private HashMap<String, HashSet<String>> kd = new HashMap<>();
  private long updateTs = 0;

  synchronized static GroupFileSource getInstance(final String file) {
    GroupFileSource fs = INSTANCES.get(file);
    if (fs == null) {
      fs = new GroupFileSource(file);
      INSTANCES.put(file, fs);
    }
    return fs;
  }

  private GroupFileSource(final String file) {
    this.file = new File(file).getAbsoluteFile();
  }

  synchronized Set<String> getGroups(final String username) throws IOException {
    if (!file.exists()) {
      logger.error("not found file: {}", file);
      kd.clear();
      updateTs = 0;
      return Collections.emptySet();
    }
    if (file.lastModified() != updateTs) {
      // generic text format:
      // group: user1 user2 userN...
      // https://httpd.apache.org/docs/2.4/mod/mod_authz_groupfile.html
      try (BufferedReader in = new BufferedReader(new FileReader(file))) {
        logger.debug("parsing file {}", file);
        kd.clear();
        updateTs = 0;
        String line = null;
        while ((line = in.readLine()) != null) {
          if (line.startsWith("#")) {
            logger.debug("skip line {}", line);
            continue;
          }
          final String[] groupUsers = splitKeyData(line);
          if (groupUsers == null) {
            logger.debug("skip line {}", line);
            continue;
          }
          String group = cleanName(groupUsers[0]);
          if (group.isEmpty()) {
            logger.debug("skip line {}", line);
            continue;
          }
          final String[] users = groupUsers[1].split(" ");
          for (int i = 0; i < users.length; i++) {
            final String user = cleanName(users[i]);
            if (!user.isEmpty()) {
              HashSet<String> groups = kd.get(user);
              if (groups == null) {
                groups = new HashSet<>();
                kd.put(user, groups);
              }
              groups.add(group);
            } else {
              logger.debug("skip user {} in group {}", users[i], group);
            }
          }
        }
      }
      updateTs = file.lastModified();
    } else {
      logger.debug("using cached file {}", file);
    }
    if (kd.containsKey(username)) {
      logger.debug("username {} successfully found", username);
      return kd.get(username);
    } else {
      logger.debug("username {} not found", username);
    }
    return Collections.emptySet();
  }
}
