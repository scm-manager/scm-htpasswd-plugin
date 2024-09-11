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

import static com.google.common.base.Preconditions.checkArgument;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.plugin.Extension;
import sonia.scm.security.SyncingRealmHelper;
import sonia.scm.user.User;

@Singleton
@Extension
public class HtpasswdRealm extends AuthenticatingRealm {
  public static final String TYPE = "htpasswd";

  private static final Logger logger = LoggerFactory.getLogger(HtpasswdRealm.class);

  private final SyncingRealmHelper syncingRealmHelper;
  private final HtpasswdConfigStore configStore;

  @Inject
  public HtpasswdRealm(HtpasswdConfigStore configStore, SyncingRealmHelper syncingRealmHelper) {
    this.configStore = configStore;
    this.syncingRealmHelper = syncingRealmHelper;
    setAuthenticationTokenClass(UsernamePasswordToken.class);
    setCredentialsMatcher(new AllowAllCredentialsMatcher());

    setAuthenticationCachingEnabled(false);
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
    HtpasswdConfig config = configStore.get();
    if (!config.isEnabled()) {
      logger.debug("htpasswd not enabled - skipping authentication");
      return null;
    }

    checkArgument(token instanceof UsernamePasswordToken, "%s is required", UsernamePasswordToken.class);

    UsernamePasswordToken upt = (UsernamePasswordToken) token;
    String username = upt.getUsername();
    char[] password = upt.getPassword();

    HtpasswdAuthenticator authenticator = new HtpasswdAuthenticator(config);
    User user = authenticator.authenticate(username, new String(password))
        .orElseThrow(() -> new UnknownAccountException("could not find account with name " + username));

    syncingRealmHelper.store(user);
    return syncingRealmHelper.createAuthenticationInfo(TYPE, user);
  }
}
