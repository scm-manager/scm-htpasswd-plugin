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
package sonia.scm.auth.htpasswd.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sonia.scm.auth.htpasswd.HtpasswdConfig;
import sonia.scm.auth.htpasswd.HtpasswdTestBase;

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
