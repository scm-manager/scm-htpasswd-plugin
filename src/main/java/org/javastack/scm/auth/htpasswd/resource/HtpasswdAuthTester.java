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
