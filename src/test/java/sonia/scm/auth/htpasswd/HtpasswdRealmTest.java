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
package sonia.scm.auth.htpasswd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.NoSuchAlgorithmException;

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
  void setUpRealm() throws NoSuchAlgorithmException {
    config = createConfig();
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
