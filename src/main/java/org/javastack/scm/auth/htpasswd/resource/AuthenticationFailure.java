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

import org.javastack.scm.auth.htpasswd.ConfigurationException;
import org.javastack.scm.auth.htpasswd.InvalidUserException;
import org.javastack.scm.auth.htpasswd.UserAuthenticationFailedException;
import org.javastack.scm.auth.htpasswd.UserSearchFailedException;

import com.google.common.base.Throwables;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
class AuthenticationFailure {
  private final boolean configured;
  private final boolean userFound;
  private final boolean userAuthenticated;
  private final String exception;

  static AuthenticationFailure userNotFound() {
    return userNotFound((String) null);
  }

  static AuthenticationFailure userNotFound(UserSearchFailedException ex) {
    return userNotFound(Throwables.getStackTraceAsString(ex));
  }

  static AuthenticationFailure userNotFound(String exception) {
    return new AuthenticationFailure(true, false, false, exception);
  }

  static AuthenticationFailure authenticationFailed(UserAuthenticationFailedException ex) {
    return new AuthenticationFailure(true, true, false, Throwables.getStackTraceAsString(ex));
  }

  static AuthenticationFailure invalidUser(InvalidUserException ex) {
    return new AuthenticationFailure(true, true, true, Throwables.getStackTraceAsString(ex));
  }

  static AuthenticationFailure invalidConfig(ConfigurationException ex) {
    return new AuthenticationFailure(false, false, false, Throwables.getStackTraceAsString(ex));
  }
}
