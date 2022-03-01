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

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static org.javastack.scm.auth.htpasswd.resource.HtpasswdModule.PERMISSION_NAME;

import javax.inject.Inject;

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
  private ScmPathInfoStore scmPathInfoStore;

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
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), HtpasswdConfigResource.class);
    return linkBuilder.method("getConfig").parameters().href();
  }

  private String update() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), HtpasswdConfigResource.class);
    return linkBuilder.method("setConfig").parameters().href();
  }

  private String test() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), HtpasswdConfigResource.class);
    return linkBuilder.method("testConfig").parameters().href();
  }
}
