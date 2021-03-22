package edu.kpi.testcourse.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kpi.testcourse.entities.UrlAlias;
import edu.kpi.testcourse.logic.Logic;
import edu.kpi.testcourse.rest.models.ErrorResponse;
import edu.kpi.testcourse.rest.models.UrlAllResponse;
import edu.kpi.testcourse.rest.models.UrlShortenResponse;
import edu.kpi.testcourse.rest.models.UserSignupRequest;
import edu.kpi.testcourse.serialization.JsonTool;
import edu.kpi.testcourse.serialization.JsonToolJacksonImpl;
import edu.kpi.testcourse.storage.UrlRepository.PermissionDenied;
import edu.kpi.testcourse.storage.UrlRepositoryFakeImpl;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.server.util.HttpHostResolver;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;


/**
 * API controller for all REST API endpoints accessible without authentication.
 */
@Secured(SecurityRule.IS_ANONYMOUS)
@Controller
public class PublicApiController {

  private final Logic logic;
  private final ObjectMapper objectMapper;
  JsonToolJacksonImpl json = new JsonToolJacksonImpl();


  @Inject
  public PublicApiController(Logic logic, ObjectMapper objectMapper,
      HttpHostResolver httpHostResolver) {
    this.logic = logic;
    this.objectMapper = objectMapper;
  }

  /**
   * Sign-up (user creation) request.
   *
   * @param request request with email and password
   * @return nothing or error description
   */
  @Post(value = "/users/signup", produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> signup(UserSignupRequest request) throws JsonProcessingException {
    try {
      logic.createNewUser(request.email(), request.password());
      return HttpResponse.status(HttpStatus.CREATED);
    } catch (Logic.UserIsAlreadyCreated e) {
      return HttpResponse.serverError(
        objectMapper.writeValueAsString(new ErrorResponse(0, e.getMessage())));
    }
  }

  /**
   * Redirection to a full URL by alias.
   *
   * @param alias a short URL alias
   */
  @Get(value = "/r/{alias}")
  public HttpResponse<?> redirect(String alias) throws URISyntaxException {
    String fullUrl = logic.findFullUrl(alias);
    if (fullUrl != null) {
      return HttpResponse.redirect(new URI(fullUrl));
    } else {
      return HttpResponse.notFound();
    }
  }


  /**
   * Send list as response.
   *
   */
  @Get(value = "/urls")
  public HttpResponse<String> showUrls(HttpRequest<?> httpRequest, Principal principal) {
    String email = principal.getName();
    return HttpResponse.created(json.toJson(logic.dataCreation().get(email)));
  }


  /**
   * Delete full URL by alias.
   *
   * @param alias a short URL alias
   */
  @Delete(value = "/delete/{alias}")
  public HttpResponse<?> deleteAlias(String alias, Principal principal) throws PermissionDenied {
    String email = principal.getName();
    if (logic.deleteUrl(email, alias) != null) {
      return HttpResponse.created("Successfully_Deleted");
    } else {
      return HttpResponse.created("No_Alias_With_That_Key");
    }
  }

}
