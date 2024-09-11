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

import java.util.Optional;
import java.util.Set;

import org.javastack.scm.auth.htpasswd.ConfigurationException;
import org.javastack.scm.auth.htpasswd.HtpasswdAuthenticator;
import org.javastack.scm.auth.htpasswd.HtpasswdConfig;
import org.javastack.scm.auth.htpasswd.HtpasswdGroupResolver;
import org.javastack.scm.auth.htpasswd.InvalidUserException;
import org.javastack.scm.auth.htpasswd.UserAuthenticationFailedException;
import org.javastack.scm.auth.htpasswd.UserSearchFailedException;

import sonia.scm.user.User;

public class HtpasswdAuthTester {
  private final HtpasswdConfig config;

  HtpasswdAuthTester(HtpasswdConfig config) {
    this.config = config;
  }

  AuthenticationResult test(String username, String password) {
    HtpasswdAuthenticator authenticator = new HtpasswdAuthenticator(config);
    try {
      Optional<User> optionalUser = authenticator.authenticate(username, password);

      if (!optionalUser.isPresent()) {
        return new AuthenticationResult(AuthenticationFailure.userNotFound());
      }

      HtpasswdGroupResolver groupResolver = HtpasswdGroupResolver.from(config);
      Set<String> groups = groupResolver.resolve(username);

      return new AuthenticationResult(optionalUser.get(), groups);
    } catch (UserAuthenticationFailedException ex) {
      return new AuthenticationResult(AuthenticationFailure.authenticationFailed(ex));
    } catch (InvalidUserException ex) {
      return new AuthenticationResult(AuthenticationFailure.invalidUser(ex), ex.getInvalidUser());
    } catch (ConfigurationException ex) {
      return new AuthenticationResult(AuthenticationFailure.invalidConfig(ex));
    } catch (UserSearchFailedException ex) {
      return new AuthenticationResult(AuthenticationFailure.userNotFound(ex));
    }
  }
}
