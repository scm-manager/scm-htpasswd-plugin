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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import javax.ws.rs.core.MediaType;

import org.assertj.core.api.Assertions;
import org.javastack.scm.auth.htpasswd.HtpasswdConfig;
import org.javastack.scm.auth.htpasswd.HtpasswdConfigStore;
import org.javastack.scm.auth.htpasswd.resource.AuthenticationResult;
import org.javastack.scm.auth.htpasswd.resource.HtpasswdAuthTester;
import org.javastack.scm.auth.htpasswd.resource.HtpasswdConfigResource;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;

import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.user.UserTestData;
import sonia.scm.web.RestDispatcher;

@SubjectAware(configuration = "classpath:sonia/scm/shiro.ini")
@RunWith(MockitoJUnitRunner.Silent.class)
public class HtpasswdConfigResourceTest {
  private static final String HTPASSWD_CONFIG_JSON = ("{" //
      + "'htpasswdFilepath':'/etc/scm/.htpasswd.test'," //
      + "'htgroupFilepath':'/etc/scm/.htgroup.test'," //
      + "'htmetaFilepath':'/etc/scm/.htmeta.test'" + "}") //
          .replaceAll("'", "\"");

  @Rule
  public final ShiroRule shiroRule = new ShiroRule();

  @Mock
  private HtpasswdConfigStore configStore;
  @Mock
  private ScmPathInfoStore scmPathInfoStore;

  @Mock
  private HtpasswdAuthTester authTester;

  @InjectMocks
  private HtpasswdConfigMapperImpl mapper;

  private RestDispatcher dispatcher;

  @Before
  public void init() {
    HtpasswdConfigResource resource = new HtpasswdConfigResource(configStore, mapper) {
      @Override
      HtpasswdAuthTester createAuthTester(HtpasswdConfig config) {
        return authTester;
      }
    };

    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);

    when(scmPathInfoStore.get()).thenReturn(() -> URI.create("/"));
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void adminShouldGetConfig() throws URISyntaxException, UnsupportedEncodingException {
    when(configStore.get()).thenReturn(new HtpasswdConfig());

    MockHttpRequest request = MockHttpRequest.get("/v2/config/htpasswd");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(200, response.getStatus());
    Assertions.assertThat(response.getContentAsString()) //
        .contains("\"update\":{\"href\":\"/v2/config/htpasswd\"}") //
        .contains("\"test\":{\"href\":\"/v2/config/htpasswd/test\"}");
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void normalUserShouldNotGetConfig() throws URISyntaxException, UnsupportedEncodingException {
    when(configStore.get()).thenReturn(new HtpasswdConfig());

    MockHttpRequest request = MockHttpRequest.get("/v2/config/htpasswd");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(403, response.getStatus());
    Assertions.assertThat(response.getContentAsString()) //
        .contains("configuration:read:htpasswd");
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void adminShouldSetConfig() throws URISyntaxException {
    when(configStore.get()).thenReturn(new HtpasswdConfig());

    MockHttpRequest request = MockHttpRequest //
        .put("/v2/config/htpasswd") //
        .contentType(MediaType.APPLICATION_JSON_TYPE) //
        .content(HTPASSWD_CONFIG_JSON.getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(204, response.getStatus());
    verify(configStore).set(any());
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void normalUserShouldNotSetConfig() throws URISyntaxException, UnsupportedEncodingException {
    when(configStore.get()).thenReturn(new HtpasswdConfig());

    MockHttpRequest request = MockHttpRequest //
        .put("/v2/config/htpasswd") //
        .contentType(MediaType.APPLICATION_JSON_TYPE) //
        .content(HTPASSWD_CONFIG_JSON.getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(403, response.getStatus());
    Assertions.assertThat(response.getContentAsString()) //
        .contains("configuration:write:htpasswd");
    verify(configStore, never()).set(any());
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void adminShouldTestConfig() throws URISyntaxException, UnsupportedEncodingException {
    when(configStore.get()).thenReturn(new HtpasswdConfig());

    AuthenticationResult result = new AuthenticationResult(UserTestData.createSlarti(),
        Collections.emptySet());
    when(authTester.test(any(), any())).thenReturn(result);

    MockHttpRequest request = MockHttpRequest //
        .post("/v2/config/htpasswd/test") //
        .contentType(MediaType.APPLICATION_JSON_TYPE) //
        .content("{}".getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(200, response.getStatus());
    Assertions.assertThat(response.getContentAsString()) //
        .contains("configured").contains("userFound");
    verify(configStore, never()).set(any());
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void normalUserShouldNotTestConfig() throws URISyntaxException, UnsupportedEncodingException {
    when(configStore.get()).thenReturn(new HtpasswdConfig());

    MockHttpRequest request = MockHttpRequest.put("/v2/config/htpasswd") //
        .contentType(MediaType.APPLICATION_JSON_TYPE) //
        .content(HTPASSWD_CONFIG_JSON.getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(403, response.getStatus());
    Assertions.assertThat(response.getContentAsString()) //
        .contains("configuration:write:htpasswd");
    verify(configStore, never()).set(any());
  }
}
