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

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.util.Providers;

import sonia.scm.group.GroupResolver;
import sonia.scm.plugin.Extension;

@Extension
public class HtpasswdGroupResolver implements GroupResolver {
  private static final Logger logger = LoggerFactory.getLogger(HtpasswdGroupResolver.class);

  private final Provider<HtpasswdConfig> store;

  @Inject
  public HtpasswdGroupResolver(HtpasswdConfigStore store) {
    this((Provider<HtpasswdConfig>) store);
  }

  private HtpasswdGroupResolver(Provider<HtpasswdConfig> store) {
    this.store = store;
  }

  public static HtpasswdGroupResolver from(HtpasswdConfig config) {
    return new HtpasswdGroupResolver(Providers.of(config));
  }

  @Override
  public Set<String> resolve(String principal) {
    HtpasswdConfig config = store.get();
    if (config.isEnabled() && config.isValid()) {
      try {
        final GroupFileSource file = GroupFileSource.getInstance(config.getHtgroupFilepath());
        final Set<String> groups = file.getGroups(principal);
        return (groups.isEmpty() ? Collections.emptySet() : groups);
      } catch (IOException ex) {
        logger.error("failed to resolve groups for principal {}", principal, ex);
      }
    } else {
      logger.debug("htpasswd is disabled, returning empty set of groups");
    }
    return Collections.emptySet();
  }
}
