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

import org.hibernate.validator.constraints.NotEmpty;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings("squid:S2160") // No need for equals and hash code here, never compared, never hashed
public class HtpasswdConfigDto extends HalRepresentation {
  private boolean enabled;
  @NotEmpty
  private String htpasswdFilepath;
  @NotEmpty
  private String htgroupFilepath;
  @NotEmpty
  private String htmetaFilepath;

  public HtpasswdConfigDto(Links links) {
    super(links);
  }
}
