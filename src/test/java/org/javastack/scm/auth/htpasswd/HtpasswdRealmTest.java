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

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.SyncingRealmHelper;
import sonia.scm.user.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HtpasswdRealmTest extends HtpasswdTestBase {
  @Mock
  private HtpasswdConfigStore configStore;
  @Mock
  private SyncingRealmHelper syncingRealmHelper;
  private HtpasswdRealm realm;
  private HtpasswdConfig config;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUpRealm() {
    this.setup();
    config = createConfigWithFiles();
    lenient().when(configStore.get()).thenReturn(config);
    realm = new HtpasswdRealm(configStore, syncingRealmHelper);
  }

  @Test
  void shouldReturnNullIfHtpasswdIsDisabled() {
    config.setEnabled(false);
    AuthenticationInfo authenticationInfo = realm
        .doGetAuthenticationInfo(createToken("trillian", "trilli123"));
    assertThat(authenticationInfo).isNull();
  }

  @Test
  void shouldThrowUnknownAccountException() {
    AuthenticationToken token = createToken("hansolo", "trilli123");
    assertThrows(UnknownAccountException.class, () -> realm.doGetAuthenticationInfo(token));
  }

  @Test
  void shouldReturnAuthenticationInfo() {
    AuthenticationInfo authenticationInfoMock = mock(AuthenticationInfo.class);
    when(syncingRealmHelper.createAuthenticationInfo(eq(HtpasswdRealm.TYPE), any()))
        .thenReturn(authenticationInfoMock);

    AuthenticationInfo authenticationInfo = realm.getAuthenticationInfo(createToken("trillian", "trilli123"));
    verify(syncingRealmHelper).store(any(User.class));
    assertThat(authenticationInfo).isSameAs(authenticationInfoMock);
  }

  @Test
  void testWrongPassword() {
    AuthenticationToken token = createToken("trillian", "trilli1234");
    assertThrows(UserAuthenticationFailedException.class, () -> realm.doGetAuthenticationInfo(token));
  }

  private AuthenticationToken createToken(String username, String password) {
    return new UsernamePasswordToken(username, password);
  }
}
