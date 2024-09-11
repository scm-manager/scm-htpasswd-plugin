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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.PrintWriter;
import java.util.Optional;

import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sonia.scm.user.User;

class HtpasswdAuthenticatorTest extends HtpasswdTestBase {
  private HtpasswdAuthenticator authenticator;

  @BeforeEach
  void setUpAuthenticator() {
    this.setup();
    HtpasswdConfig config = createConfigWithFiles();
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
