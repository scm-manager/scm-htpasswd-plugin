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

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import com.google.common.annotations.VisibleForTesting;

import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

@Singleton
public class HtpasswdConfigStore implements Provider<HtpasswdConfig> {
  private final ConfigurationStore<HtpasswdConfig> configurationStore;

  @Inject
  public HtpasswdConfigStore(ConfigurationStoreFactory configurationStoreFactory) {
    this(configurationStoreFactory.withType(HtpasswdConfig.class).withName("htpasswd").build());
  }

  @VisibleForTesting
  HtpasswdConfigStore(ConfigurationStore<HtpasswdConfig> configurationStore) {
    this.configurationStore = configurationStore;
  }

  public HtpasswdConfig get() {
    return configurationStore.getOptional().orElse(new HtpasswdConfig());
  }

  public void set(HtpasswdConfig config) {
    configurationStore.set(config);
  }
}
