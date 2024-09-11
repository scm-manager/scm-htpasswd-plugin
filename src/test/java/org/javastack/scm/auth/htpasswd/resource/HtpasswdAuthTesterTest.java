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

package org.javastack.scm.auth.htpasswd.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.NoSuchAlgorithmException;

import org.javastack.scm.auth.htpasswd.HtpasswdConfig;
import org.javastack.scm.auth.htpasswd.HtpasswdTestBase;
import org.javastack.scm.auth.htpasswd.resource.AuthenticationFailure;
import org.javastack.scm.auth.htpasswd.resource.AuthenticationResult;
import org.javastack.scm.auth.htpasswd.resource.HtpasswdAuthTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HtpasswdAuthTesterTest extends HtpasswdTestBase {
  private HtpasswdConfig config;
  private HtpasswdAuthTester tester;

  @BeforeEach
  void setUpAuthTester() throws NoSuchAlgorithmException {
    config = createConfig();
    tester = new HtpasswdAuthTester(config);
  }

  @Test
  void shouldReturnSuccessful() {
    AuthenticationResult result = tester.test("trillian", "trilli123");
    assertThat(result.getFailure()).isEmpty();
    assertThat(result.getUser()).hasValueSatisfying(r -> assertThat(r.getName()).isEqualTo("trillian"));
    assertThat(result.getGroups()).containsOnly("HeartOfGold", //
        "RestaurantAtTheEndOfTheUniverse", //
        "HappyVerticalPeopleTransporter");
  }

  @Test
  void shouldReturnUserNotFoundFailure() {
    AuthenticationResult result = tester.test("hansolo", "trilli123");
    assertThat(result.getFailure()).contains(AuthenticationFailure.userNotFound());
  }

  @Test
  void shouldReturnAuthenticationFailedFailure() {
    AuthenticationResult result = tester.test("trillian", "trilli1234");
    assertThat(result.getFailure()).isPresent();
    AuthenticationFailure failure = result.getFailure().get();
    assertThat(failure.isUserFound()).isTrue();
    assertThat(failure.isUserAuthenticated()).isFalse();
    assertThat(failure.getException()).isNotEmpty();
  }

  @Test
  void shouldReturnInvalidConfiguration() {
    config.setHtpasswdFilepath(null);
    AuthenticationResult result = tester.test("trillian", "trilli123");
    assertThat(result.getFailure()).isPresent();
    AuthenticationFailure failure = result.getFailure().get();
    assertThat(failure.isConfigured()).isFalse();
    assertThat(failure.isUserFound()).isFalse();
    assertThat(failure.isUserAuthenticated()).isFalse();
    assertThat(failure.getException()).isNotEmpty();
  }
}
