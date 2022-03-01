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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import sonia.scm.Validateable;
import sonia.scm.util.Util;

@XmlRootElement(name = "htpasswd-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class HtpasswdConfig implements Validateable {
  @XmlElement(name = "enabled")
  private boolean enabled = false;

  @XmlElement(name = "htpasswd-filepath")
  private String htpasswdFilepath = "/etc/scm/.htpasswd";

  @XmlElement(name = "htgroup-filepath")
  private String htgroupFilepath = "/etc/scm/.htgroup";

  @XmlElement(name = "htmeta-filepath")
  private String htmetaFilepath = "/etc/scm/.htmeta";

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public String getHtpasswdFilepath() {
    return htpasswdFilepath;
  }

  public void setHtpasswdFilepath(final String htpasswdFilepath) {
    this.htpasswdFilepath = htpasswdFilepath;
  }

  public String getHtgroupFilepath() {
    return htgroupFilepath;
  }

  public void setHtgroupFilepath(final String htgroupFilepath) {
    this.htgroupFilepath = htgroupFilepath;
  }

  public String getHtmetaFilepath() {
    return htmetaFilepath;
  }

  public void setHtmetaFilepath(final String htmetaFilepath) {
    this.htmetaFilepath = htmetaFilepath;
  }

  @Override
  public boolean isValid() {
    return isValid(htpasswdFilepath, htgroupFilepath, htmetaFilepath);
  }

  private boolean isValid(String... fields) {
    boolean valid = true;

    for (String field : fields) {
      if (Util.isEmpty(field)) {
        valid = false;

        break;
      }
    }

    return valid;
  }
}
