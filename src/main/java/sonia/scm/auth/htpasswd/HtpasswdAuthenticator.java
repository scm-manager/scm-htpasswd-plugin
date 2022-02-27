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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.codec.digest.Md5Crypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import sonia.scm.user.User;
import sonia.scm.util.ValidationUtil;

public class HtpasswdAuthenticator {
  private static final Logger logger = LoggerFactory.getLogger(HtpasswdAuthenticator.class);

  private final HtpasswdConfig config;

  public HtpasswdAuthenticator(HtpasswdConfig config) {
    this.config = config;
  }

  public Optional<User> authenticate(String username, String password) {
    if (!config.isValid()) {
      throw new ConfigurationException("invalid config");
    }
    if (!authenticateUser(username, password)) {
      return Optional.empty();
    }
    final User user = createUser(username);
    logger.trace("successfully created user from from htpasswd: {}", user);
    return Optional.of(user);
  }

  private boolean authenticateUser(final String username, final String password) {
    // user:$apr1$...encrypted...
    // https://httpd.apache.org/docs/2.4/misc/password_encryptions.html
    final UserFileSource file = UserFileSource.getInstance(config.getHtpasswdFilepath());
    final List<String> line = file.getLine(username);
    if (line.size() == 2) {
      final String user = line.get(0);
      final String hash = line.get(1);
      if (user.equals(username)) {
        if (hash.startsWith("$apr1$")) { // only APR1 (secure)
          final String computedHash = Md5Crypt.apr1Crypt(password, hash);
          if (hash.equals(computedHash)) {
            logger.debug("user {} successfully authenticated", username);
            return true;
          }
        }
        throw new UserAuthenticationFailedException("failed to authenticate user " + username);
      }
    }
    return false;
  }

  private List<String> getMeta(final String username) {
    // user:mail:displayName
    final UserFileSource file = UserFileSource.getInstance(config.getHtmetaFilepath());
    final List<String> line = file.getLine(username);
    if (line.size() == 2) {
      final String user = line.get(0);
      final String data = line.get(1);
      final String[] toks = data.split(":", -1);
      final String mail = ((toks.length >= 1) ? toks[0] : null);
      final String displayName = ((toks.length >= 2) ? toks[1] : null);
      if (user.equals(username)) {
        return Arrays.asList(mail, displayName);
      }
    }
    return Arrays.asList("", "");
  }

  private User createUser(String username) {
    User user = new User();
    user.setExternal(true);
    user.setName(username);

    List<String> meta = getMeta(username);

    String displayName = meta.get(1);
    if (Strings.isNullOrEmpty(displayName)) {
      displayName = username;
    }
    user.setDisplayName(displayName);

    String mail = meta.get(0);
    if (ValidationUtil.isMailAddressValid(mail)) {
      user.setMail(mail);
    } else {
      logger.warn("No valid e-mail address found for user {}", username);
    }

    if (!user.isValid()) {
      throw new InvalidUserException("invalid user object: " + user, user);
    }

    return user;
  }
}
