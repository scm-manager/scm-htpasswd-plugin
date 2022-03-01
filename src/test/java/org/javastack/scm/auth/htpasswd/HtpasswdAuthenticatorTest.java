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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import org.apache.commons.codec.digest.Md5Crypt;
import org.javastack.scm.auth.htpasswd.HtpasswdAuthenticator;
import org.javastack.scm.auth.htpasswd.HtpasswdConfig;
import org.javastack.scm.auth.htpasswd.UserAuthenticationFailedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sonia.scm.user.User;

class HtpasswdAuthenticatorTest extends HtpasswdTestBase {
  private HtpasswdConfig config;
  private HtpasswdAuthenticator authenticator;

  @BeforeEach
  void setUpAuthenticator() throws NoSuchAlgorithmException {
    config = createConfig();
    authenticator = new HtpasswdAuthenticator(config);
  }

  @Test
  void shouldAuthenticateUserTrillian() {
    Optional<User> optionalUser = authenticator.authenticate("trillian", "trilli123");
    assertThat(optionalUser).isPresent();

    User trillian = optionalUser.get();
    assertTrillian(trillian);
  }

  private void assertTrillian(User user) {
    assertThat(user.getName()).isEqualTo("trillian");
    assertThat(user.getDisplayName()).isEqualTo("Tricia McMillan");
    assertThat(user.getMail()).isEqualTo("tricia.mcmillan@hitchhiker.com");
  }

  @Test
  void shouldReturnEmptyOptional() {
    Optional<User> optionalUser = authenticator.authenticate("unknown", "secret");
    assertThat(optionalUser).isEmpty();
  }

  @Test
  void shouldThrowUserAuthenticationFailedException() {
    assertThrows(UserAuthenticationFailedException.class,
        () -> authenticator.authenticate("trillian", "i_don't_know"));
  }

  @Test
  void shouldReturnUserEvenWithoutMail() {
    Optional<User> optionalUser = authenticator.authenticate("arthur", "dent123");
    assertThat(optionalUser).isPresent();
    assertThat(optionalUser.get().getMail()).isNull();
  }

  @Test
  void shouldOnlySetMailIfValid() {
    Optional<User> optionalUser = authenticator.authenticate("prefect", "prefi123");
    assertThat(optionalUser).isPresent();
    assertThat(optionalUser.get().getMail()).isNull();
  }

  @Test
  void shouldReturnUserEvenWithoutDisplayName() {
    Optional<User> optionalUser = authenticator.authenticate("zaphod", "zaphod123");
    assertThat(optionalUser).isPresent();
    assertThat(optionalUser.get().getDisplayName()).isEqualTo("zaphod");
  }

  @Test
  void shouldAuthenticateUserPrefectEvenWithUpdate() {
    assertThat(authenticator.authenticate("prefect", "prefi123")).isPresent();
    // Replace users with new user/pwd
    try (PrintWriter out = new PrintWriter(HTPASSWD)) {
      out.print("prefect");
      out.print(":");
      out.println(Md5Crypt.apr1Crypt("updated123", "test!"));
      out.flush();
    } catch (Exception ex) {
      Assertions.fail("failed to write htpasswd data file: " + HTPASSWD, ex);
    }
    try (PrintWriter out = new PrintWriter(HTMETA)) {
      out.println("prefect:ford.prefect@hitchhiker.com:Ford Prefect!");
      out.flush();
    } catch (Exception ex) {
      Assertions.fail("failed to write htmeta data file: " + HTMETA, ex);
    }
    Optional<User> optionalUser = authenticator.authenticate("prefect", "updated123");
    assertThat(optionalUser).isPresent();
    assertUpdatedPrefect(optionalUser.get());
  }

  private void assertUpdatedPrefect(User user) {
    assertThat(user.getName()).isEqualTo("prefect");
    assertThat(user.getDisplayName()).isEqualTo("Ford Prefect!");
    assertThat(user.getMail()).isEqualTo("ford.prefect@hitchhiker.com");
  }
}
