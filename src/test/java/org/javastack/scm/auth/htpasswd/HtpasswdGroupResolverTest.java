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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.store.InMemoryConfigurationStore;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HtpasswdGroupResolverTest extends HtpasswdTestBase {
  private HtpasswdConfig config;
  private HtpasswdGroupResolver groupResolver;

  @BeforeEach
  void setUpAuthenticator() {
    this.setup();
    config = createConfig();
    HtpasswdConfigStore htpasswdConfigStore = new HtpasswdConfigStore(new InMemoryConfigurationStore<>());
    htpasswdConfigStore.set(config);
    groupResolver = new HtpasswdGroupResolver(htpasswdConfigStore);
  }

  @Test
  void shouldReturnGroups() {
    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).containsOnly("HeartOfGold", "RestaurantAtTheEndOfTheUniverse",
        "HappyVerticalPeopleTransporter");
  }

  @Test
  void shouldReturnEmptyGroupWithoutMemberOf() {
    Set<String> groups = groupResolver.resolve("zaphod");
    assertThat(groups).isEmpty();
  }

  @Test
  void shouldReturnEmptyForNonExistingUsers() {
    Set<String> groups = groupResolver.resolve("slarti");
    assertThat(groups).isEmpty();
  }

  @Test
  void shouldReturnEmptyCollectionIfHtpasswdIsDisabled() {
    config.setEnabled(false);
    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).isEmpty();
  }

  @Test
  void shouldReturnEmptyOnInvalidConfiguration() {
    config.setHtgroupFilepath(null);
    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).isEmpty();
  }

  @Test
  void shouldReturnGroupsEvenWithUpdate() {
    assertThat(groupResolver.resolve("dephn")).containsOnly("HeartOfGold");
    // Add a new group
    try (PrintWriter out = new PrintWriter(new FileOutputStream(HTGROUP, true))) {
      out.println("test: dephn");
      out.flush();
    } catch (Exception ex) {
      Assertions.fail("failed to write group data file: " + HTGROUP, ex);
    }
    assertThat(groupResolver.resolve("dephn")).containsExactlyInAnyOrder("HeartOfGold", "test");
  }
}
