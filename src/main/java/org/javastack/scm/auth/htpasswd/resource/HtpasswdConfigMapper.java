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

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static org.javastack.scm.auth.htpasswd.resource.HtpasswdModule.PERMISSION_NAME;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.javastack.scm.auth.htpasswd.HtpasswdConfig;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;

import de.otto.edison.hal.Links;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ConfigurationPermissions;

@Mapper
public abstract class HtpasswdConfigMapper {
  @Inject
  private Provider<ScmPathInfoStore> scmPathInfoStore;

  @Mapping(ignore = true, target = "attributes")
  public abstract HtpasswdConfigDto map(HtpasswdConfig config);

  public abstract HtpasswdConfig map(HtpasswdConfigDto dto, @Context HtpasswdConfig oldConfig);

  @ObjectFactory
  HtpasswdConfigDto createDto(HtpasswdConfig config) {
    Links.Builder linksBuilder = linkingTo().self(self());
    if (ConfigurationPermissions.write(PERMISSION_NAME).isPermitted()) {
      linksBuilder.single(link("update", update()));
      linksBuilder.single(link("test", test()));
    }
    return new HtpasswdConfigDto(linksBuilder.build());
  }

  private String self() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get().get(), HtpasswdConfigResource.class);
    return linkBuilder.method("getConfig").parameters().href();
  }

  private String update() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get().get(), HtpasswdConfigResource.class);
    return linkBuilder.method("setConfig").parameters().href();
  }

  private String test() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get().get(), HtpasswdConfigResource.class);
    return linkBuilder.method("testConfig").parameters().href();
  }
}
