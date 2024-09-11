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

import static org.javastack.scm.auth.htpasswd.resource.HtpasswdModule.PERMISSION_NAME;

import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.javastack.scm.auth.htpasswd.HtpasswdConfig;
import org.javastack.scm.auth.htpasswd.HtpasswdConfigStore;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.user.User;
import sonia.scm.web.VndMediaType;

@OpenAPIDefinition(tags = { //
    @Tag(name = "Htpasswd Plugin", description = "Htpasswd Plugin related endpoints") //
})
@Singleton
@Path("v2/config/htpasswd")
public class HtpasswdConfigResource {
  private final HtpasswdConfigStore configStore;
  private final HtpasswdConfigMapper mapper;

  @Inject
  public HtpasswdConfigResource(HtpasswdConfigStore configStore, HtpasswdConfigMapper mapper) {
    this.configStore = configStore;
    this.mapper = mapper;
  }

  @POST
  @Path("test")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Test Htpasswd configuration", description = "Tests Htpasswd configuration.", tags = "Htpasswd Plugin")
  @ApiResponse( //
      responseCode = "200", //
      description = "success", //
      content = @Content( //
          mediaType = MediaType.APPLICATION_JSON, //
          schema = @Schema(implementation = HtpasswdTestConfigDto.class) //
      ) //
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the privilege")
  @ApiResponse( //
      responseCode = "500", //
      description = "internal server error", //
      content = @Content( //
          mediaType = VndMediaType.ERROR_TYPE, //
          schema = @Schema(implementation = ErrorDto.class) //
      ) //
  )
  public TestResultDto testConfig(HtpasswdTestConfigDto testConfig) {
    ConfigurationPermissions.write(PERMISSION_NAME).check();
    HtpasswdConfig config = mapper.map(testConfig.getConfig(), configStore.get());
    HtpasswdAuthTester tester = createAuthTester(config);
    AuthenticationResult result = tester.test(testConfig.getUsername(), testConfig.getPassword());
    Optional<User> user = result.getUser();
    Optional<AuthenticationFailure> failureOptional = result.getFailure();

    if (user.isPresent()) {
      return new TestResultDto(user.get(), result.getGroups());
    } else {
      AuthenticationFailure failure = failureOptional
          .orElseThrow(() -> new IllegalStateException("no user and no failure"));
      return new TestResultDto(failure.isConfigured(), failure.isUserFound(), failure.isUserAuthenticated(),
          failure.getException());
    }
  }

  @VisibleForTesting
  HtpasswdAuthTester createAuthTester(HtpasswdConfig config) {
    return new HtpasswdAuthTester(config);
  }

  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get Htpasswd configuration", description = "Returns the Htpasswd configuration.", tags = "Htpasswd Plugin")
  @ApiResponse( //
      responseCode = "200", //
      description = "success", //
      content = @Content( //
          mediaType = MediaType.APPLICATION_JSON, //
          schema = @Schema(implementation = HtpasswdConfigDto.class) //
      ) //
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the privilege")
  @ApiResponse( //
      responseCode = "500", //
      description = "internal server error", //
      content = @Content( //
          mediaType = VndMediaType.ERROR_TYPE, //
          schema = @Schema(implementation = ErrorDto.class) //
      ) //
  )
  public HtpasswdConfigDto getConfig() {
    ConfigurationPermissions.read(PERMISSION_NAME).check();
    return mapper.map(configStore.get());
  }

  @PUT
  @Path("")
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get Htpasswd configuration", description = "Returns the Htpasswd configuration.", tags = "Htpasswd Plugin")
  @ApiResponse(responseCode = "204", description = "success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the privilege")
  @ApiResponse( //
      responseCode = "500", //
      description = "internal server error", //
      content = @Content( //
          mediaType = VndMediaType.ERROR_TYPE, //
          schema = @Schema(implementation = ErrorDto.class) //
      ) //
  )
  public Response setConfig(@Context UriInfo uriInfo, @NotNull @Valid HtpasswdConfigDto config) {
    ConfigurationPermissions.write(PERMISSION_NAME).check();
    HtpasswdConfig newConfig = mapper.map(config, configStore.get());
    configStore.set(newConfig);
    return Response.noContent().build();
  }
}
