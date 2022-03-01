/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.javastack.scm.auth.htpasswd;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import org.javastack.scm.auth.htpasswd.HtpasswdConfig;
import org.javastack.scm.auth.htpasswd.HtpasswdConfigStore;
import org.javastack.scm.auth.htpasswd.HtpasswdGroupResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sonia.scm.store.InMemoryConfigurationStore;

class HtpasswdGroupResolverTest extends HtpasswdTestBase {
  private HtpasswdConfig config;
  private HtpasswdGroupResolver groupResolver;

  @BeforeEach
  void setUpAuthenticator() throws NoSuchAlgorithmException {
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
    assertThat(groupResolver.resolve("dephn")).containsOnly("HeartOfGold", "test");
  }
}
