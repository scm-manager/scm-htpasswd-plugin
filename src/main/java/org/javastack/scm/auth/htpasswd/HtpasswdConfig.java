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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

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
