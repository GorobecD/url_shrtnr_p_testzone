package edu.kpi.testcourse.logic;


import edu.kpi.testcourse.entities.UrlAlias;
import edu.kpi.testcourse.entities.User;
import edu.kpi.testcourse.rest.AuthenticatedApiController;
import edu.kpi.testcourse.storage.UrlRepository;
import edu.kpi.testcourse.storage.UrlRepository.AliasAlreadyExist;
import edu.kpi.testcourse.storage.UrlRepository.PermissionDenied;
import edu.kpi.testcourse.storage.UserRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.netty.handler.codec.http2.StreamBufferingEncoder.Http2ChannelClosedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Business logic of the URL shortener application.
 */
public class Logic {
  /**
   * Create variable.
   */
  private final UserRepository users;
  private final UrlRepository urls;
  private final HashUtils hashUtils;
  public Map<String, Map<String, String>> allList = new HashMap<>();
  public Map<String, Map<String, String>> data = new HashMap<>();

  /**
   * Creates an instance.
   */
  public Logic(UserRepository users, UrlRepository urls) {
    this.users = users;
    this.urls = urls;
    this.hashUtils = new HashUtils();
  }

  /**
   * Create a new user.
   *
   * @param email users email
   * @param password users password
   * @throws UserIsAlreadyCreated is thrown if user is already created
   */
  public void createNewUser(String email, String password) throws UserIsAlreadyCreated {
    if (users.findUser(email) != null) {
      throw new UserIsAlreadyCreated();
    } else {
      users.createUser(new User(email, hashUtils.generateHash(password)));
    }
  }

  /**
   * Gives an answer if user is registered and password is correct.
   *
   * @param email a users email
   * @param password a users password
   * @return if user is registered and password is correct
   */
  public boolean isUserValid(String email, String password) {
    User user = users.findUser(email);
    if (user == null) {
      return false;
    }

    return hashUtils.validatePassword(password, user.passwordHash());
  }

  /**
   * Create a new URL alias (shortened version).
   *
   * @param email an email of a user that creates the alias
   * @param url a full URL
   * @param alias a proposed alias
   *
   * @return a shortened URL
   */
  public String createNewAlias(String email, String url, String alias) throws AliasAlreadyExist {
    String finalAlias;
    Map<String, String> alUrl = new HashMap<>();

    if (alias == null || alias.isEmpty()) {
      String ab = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
      Random rnd = new Random();
      StringBuilder sb = new StringBuilder(5);
      for (int i = 0; i < 5; i++) {
        sb.append(ab.charAt(rnd.nextInt(ab.length())));
      }
      finalAlias = sb.toString();
    } else {
      finalAlias = alias;
    }

    alUrl.put(finalAlias, url);
    if (!allList.containsKey(email)) {
      allList.put(email, alUrl);
    } else {
      allList.get(email).put(finalAlias, url);
    }
    urls.createUrlAlias(new UrlAlias(finalAlias, url, email));

    return finalAlias;
  }

  /**
   * Get full URL by alias.
   *
   * @param alias a short URL alias
   * @return a full URL
   */
  public String findFullUrl(String alias) {
    UrlAlias urlAlias = urls.findUrlAlias(alias);

    if (urlAlias != null) {
      return urlAlias.destinationUrl();
    }

    return null;
  }

  /**
   * Get URL's data.
   *
   * @return a URL's data
   */
  public Map<String, Map<String, String>> dataCreation() {
    Map<String, Map<String, String>> fullUrl = allList;
    Map<String, Map<String, String>> answer = new HashMap<>();

    for (Map.Entry<String, Map<String, String>> entry : fullUrl.entrySet()) {
      String key = entry.getKey();
      Map<String, String> val = entry.getValue();
      if (!answer.containsKey(key)) {
        answer.put(key, val);
      } else {
        answer.get(key).put(val.entrySet().toArray()[0].toString(),
            val.entrySet().toArray()[1].toString());
      }
      for (Map.Entry<String, Map<String, String>> entry2 : answer.entrySet()) {
        String dataKey = entry2.getKey();
        Map<String, String> val2 = entry.getValue();
        if (!data.containsKey(dataKey)) {
          data.put(dataKey, val2);
        }
      }
    }
    return data;
  }

  /**
   * Delete URL's by alias.
   *
   * @return a response
   */
  public String deleteUrl(String email, String alias) throws PermissionDenied {
    return data.get(email).remove(alias);
  }

  /**
   * Error for situation when we are trying to register already registered user.
   */
  public static class UserIsAlreadyCreated extends Throwable {
    public UserIsAlreadyCreated() {
      super("User with such email is already created");
    }
  }

}
