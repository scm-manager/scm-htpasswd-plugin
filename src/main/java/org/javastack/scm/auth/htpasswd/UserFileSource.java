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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UserFileSource extends HtFileSource {
  private static final Logger logger = LoggerFactory.getLogger(UserFileSource.class);
  private static final HashMap<String, UserFileSource> INSTANCES = new HashMap<>();

  private final File file;
  private HashMap<String, String> kd = new HashMap<>();
  private long updateTs = 0;

  synchronized static UserFileSource getInstance(final String file) {
    UserFileSource fs = INSTANCES.get(file);
    if (fs == null) {
      fs = new UserFileSource(file);
      INSTANCES.put(file, fs);
    }
    return fs;
  }

  private UserFileSource(final String file) {
    this.file = new File(file).getAbsoluteFile();
  }

  synchronized List<String> getLine(final String username) {
    if (!file.exists()) {
      logger.error("not found file: {}", file);
      kd.clear();
      updateTs = 0;
      return Collections.emptyList();
    }
    if (file.lastModified() != updateTs) {
      logger.debug("parsing file {}", file);
      kd.clear();
      updateTs = 0;
      // generic text format:
      // username:data...
      try (BufferedReader in = new BufferedReader(new FileReader(file))) {
        String line = null;
        while ((line = in.readLine()) != null) {
          if (line.startsWith("#")) {
            logger.debug("skip line {}", line);
            continue;
          }
          final String[] userData = splitKeyData(line);
          if (userData == null) {
            logger.debug("skip line {}", line);
            continue;
          }
          final String user = cleanName(userData[0]);
          final String data = userData[1];
          if (!user.isEmpty()) {
            kd.put(user, data);
          } else {
            logger.debug("skip line {}", line);
          }
        }
        updateTs = file.lastModified();
      } catch (IOException e) {
        logger.error("unable to parse file {}", file, e);
      }
    } else {
      logger.debug("using cached file {}", file);
    }
    if (kd.containsKey(username)) {
      logger.debug("username {} successfully found", username);
      return Arrays.asList(username, kd.get(username));
    } else {
      logger.debug("username {} not found", username);
    }
    return Collections.emptyList();
  }
}
