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

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.util.Providers;

import sonia.scm.group.GroupResolver;
import sonia.scm.plugin.Extension;

@Extension
public class HtpasswdGroupResolver implements GroupResolver {
  private static final Logger logger = LoggerFactory.getLogger(HtpasswdGroupResolver.class);

  private final Provider<HtpasswdConfig> store;

  @Inject
  public HtpasswdGroupResolver(HtpasswdConfigStore store) {
    this((Provider<HtpasswdConfig>) store);
  }

  private HtpasswdGroupResolver(Provider<HtpasswdConfig> store) {
    this.store = store;
  }

  public static HtpasswdGroupResolver from(HtpasswdConfig config) {
    return new HtpasswdGroupResolver(Providers.of(config));
  }

  @Override
  public Set<String> resolve(String principal) {
    HtpasswdConfig config = store.get();
    if (config.isEnabled() && config.isValid()) {
      try {
        final GroupFileSource file = GroupFileSource.getInstance(config.getHtgroupFilepath());
        final Set<String> groups = file.getGroups(principal);
        return (groups.isEmpty() ? Collections.emptySet() : groups);
      } catch (IOException ex) {
        logger.error("failed to resolve groups for principal {}", principal, ex);
      }
    } else {
      logger.debug("htpasswd is disabled, returning empty set of groups");
    }
    return Collections.emptySet();
  }
}
