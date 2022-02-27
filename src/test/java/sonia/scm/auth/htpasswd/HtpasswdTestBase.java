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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.jupiter.api.Assertions;

public class HtpasswdTestBase {
  private static final String USER_DATA_FILE = "/users/users.txt";
  protected static final String HTPASSWD;
  protected static final String HTGROUP;
  protected static final String HTMETA;

  private static final HashMap<String, String> usrpwd = new HashMap<>(); // user:$apr1$....
  private static final HashMap<String, HashSet<String>> grpusr = new HashMap<>(); // grp: user1 user2 ...
  private static final HashMap<String, String> usrmeta = new HashMap<>(); // user:email:displayname

  static {
    try {
      final File tmpFile = File.createTempFile("htpasswd-test.", ".tmp");
      final File tmpDir = tmpFile.getParentFile();
      HTPASSWD = new File(tmpDir, "htpasswd.tmp").getAbsolutePath();
      HTGROUP = new File(tmpDir, "htgroup.tmp").getAbsolutePath();
      HTMETA = new File(tmpDir, "htmeta.tmp").getAbsolutePath();
      tmpFile.delete();
    } catch (IOException e) {
      Assertions.fail("failed to write in temporary directory", e);
      throw new IOError(e);
    }
    load();
  }

  protected HtpasswdConfig createConfig() {
    HtpasswdConfig config = new HtpasswdConfig();
    config.setEnabled(true);
    config.setHtpasswdFilepath(HTPASSWD);
    config.setHtgroupFilepath(HTGROUP);
    config.setHtmetaFilepath(HTMETA);
    this.write();
    return config;
  }

  private static void load() {
    // #user;pwd;email;displayname;groups
    try (BufferedReader in = new BufferedReader(
        new InputStreamReader(HtpasswdTestBase.class.getResourceAsStream(USER_DATA_FILE)))) {
      String line = null;
      while ((line = in.readLine()) != null) {
        if (line.startsWith("#") || line.trim().isEmpty()) {
          continue;
        }
        final String[] t = line.split(";", -1);
        if (t.length < 4) {
          Assertions.fail("invalid data on file: " + USER_DATA_FILE + " >> " //
              + line + " [" + t.length + "]");
        }
        final String user = t[0].trim(), //
            pwd = t[1].trim(), //
            email = t[2].trim(), //
            displayName = t[3].trim(), //
            grp = t[4].trim();
        final String computedHash = Md5Crypt.apr1Crypt(pwd, "test");
        usrpwd.put(user, computedHash);
        usrmeta.put(user, email + ":" + displayName);
        final String[] grps = grp.split(",", -1);
        for (String g : grps) {
          if (g.isEmpty()) {
            continue;
          }
          HashSet<String> gg = grpusr.get(g);
          if (gg == null) {
            gg = new HashSet<String>();
            grpusr.put(g, gg);
          }
          gg.add(user);
        }
      }
    } catch (Exception ex) {
      Assertions.fail("failed to load user data file: " + USER_DATA_FILE, ex);
    }
  }

  protected void write() {
    try (PrintWriter out = new PrintWriter(HTPASSWD)) {
      for (Entry<String, String> x : usrpwd.entrySet()) {
        out.print(x.getKey());
        out.print(":");
        out.println(x.getValue());
      }
      out.flush();
    } catch (Exception ex) {
      Assertions.fail("failed to write user data file: " + HTPASSWD, ex);
    }
    try (PrintWriter out = new PrintWriter(HTGROUP)) {
      for (Entry<String, HashSet<String>> x : grpusr.entrySet()) {
        out.print(x.getKey());
        out.print(": ");
        for (String y : x.getValue()) {
          out.print(y);
          out.print(" ");
        }
        out.println();
      }
      out.flush();
    } catch (Exception ex) {
      Assertions.fail("failed to write group data file: " + HTGROUP, ex);
    }
    try (PrintWriter out = new PrintWriter(HTMETA)) {
      for (Entry<String, String> x : usrmeta.entrySet()) {
        out.print(x.getKey());
        out.print(":");
        out.println(x.getValue());
      }
      out.flush();
    } catch (Exception ex) {
      Assertions.fail("failed to write meta data file: " + HTMETA, ex);
    }
  }
}
