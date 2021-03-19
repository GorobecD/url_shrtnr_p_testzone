package edu.kpi.testcourse.logic;

import edu.kpi.testcourse.entities.User;
import edu.kpi.testcourse.storage.UrlRepository.AliasAlreadyExist;
import edu.kpi.testcourse.storage.UrlRepositoryFakeImpl;
import edu.kpi.testcourse.storage.UserRepositoryFakeImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class LogicTest {

  Logic createLogic() {
    return new Logic(new UserRepositoryFakeImpl(), new UrlRepositoryFakeImpl());
  }

  Logic createLogic(UserRepositoryFakeImpl users) {
    return new Logic(users, new UrlRepositoryFakeImpl());
  }

  Logic createLogic(UrlRepositoryFakeImpl urls) {
    return new Logic(new UserRepositoryFakeImpl(), urls);
  }

  @Test
  void shouldSuccessfullyCreateANewUser() throws Logic.UserIsAlreadyCreated {
    // GIVEN
    UserRepositoryFakeImpl users = new UserRepositoryFakeImpl();
    Logic logic = createLogic(users);

    // WHEN
    logic.createNewUser("aaa@bbb.com", "password");

    // THEN
    assertThat(users.findUser("aaa@bbb.com")).isNotNull();
  }

  @Test
  void shouldNotAllowUserCreationIfEmailIsUsed() {
    // GIVEN
    UserRepositoryFakeImpl users = new UserRepositoryFakeImpl();
    users.createUser(new User("aaa@bbb.com", "hash"));
    Logic logic = createLogic(users);

    assertThatThrownBy(() -> {
      // WHEN
      logic.createNewUser("aaa@bbb.com", "password");
    })
      // THEN
      .isInstanceOf(Logic.UserIsAlreadyCreated.class);
  }

  @Test
  void shouldAuthorizeUser() throws Logic.UserIsAlreadyCreated {
    // GIVEN
    Logic logic = createLogic();

    // WHEN
    logic.createNewUser("aaa@bbb.com", "password");

    // THEN
    assertThat(logic.isUserValid("aaa@bbb.com", "password")).isTrue();
  }

  @Test
  void shouldCreateShortVersionOfUrl() {
    // GIVEN
    UrlRepositoryFakeImpl urls = new UrlRepositoryFakeImpl();
    Logic logic = createLogic(urls);

    // WHEN
    var shortUrl = logic.createNewAlias("aaa@bbb.com", "http://g.com/loooong_url", "short");

    // THEN
    assertThat(shortUrl).isEqualTo("short");
    assertThat(logic.findFullUrl("short")).isEqualTo("http://g.com/loooong_url");
  }

  @Test
  void shouldNotAllowToCreateSameAliasTwice() {
    // GIVEN
    Logic logic = createLogic();

    // WHEN
    var shortUrl = logic.createNewAlias("aaa@bbb.com", "http://g.com/loooong_url", "short");

    // THEN
    assertThatThrownBy(() -> logic.createNewAlias("ddd@bbb.com", "http://d.com/laaaang_url", "short")).isInstanceOf(AliasAlreadyExist.class);
  }

  @Test
  void generatedAliasIsNotEmpty() {
    // GIVEN
    Logic logic = createLogic();

    // WHEN
    var generatedAlias = logic.createNewAlias("aaa@bbb.com", "http://g.com/loooong_url", "");

    // THEN
    assertThat(generatedAlias).isNotEmpty();
  }

  @Test
  void shouldSaveLinkAccordingToUser() {
    // GIVEN
    Logic logic = createLogic();
    var user1_alias1 = logic.createNewAlias("aaa@bbb.com", "http://g.com/loooong_url", "user1_1");
    var user1_alias2 = logic.createNewAlias("aaa@bbb.com", "http://g.com/loooong_url", "user1_2");
    var user2_alias1 = logic.createNewAlias("zzz@yyy.com", "http://h.com/shooort_url", "user2_1");
    // WHEN
    logic.dataCreation();
    // THEN
    assertThat(logic.data.get("aaa@bbb.com").size()).isEqualTo(2);
    assertThat(logic.data.get("zzz@yyy.com").size()).isEqualTo(1);
  }

  /*@Test
  void shouldNotDeleteAnotherUserAlias() {
    // GIVEN
    Logic logic = createLogic();
    var user1_alias1 = logic.createNewAlias("aaa@bbb.com", "https://www.amazon.com/b/?node=3952&ref_=Oct_s9_apbd_odnav_hd_bw_bzv_0&pf_rd_r=QJ1CKZN7V61T8JZM8RXY&pf_rd_p=753d54e0-e548-5702-bd6c-de0e948e86be&pf_rd_s=merchandised-search-10&pf_rd_t=BROWSE&pf_rd_i=3839", "amazon");
    var user2_alias1 = logic.createNewAlias("zzz@yyy.com", "https://youtu.be/s3Ejdx6cIho", "GOD");
    // WHEN
    logic.deleteFunc("aaa@bbb.com", "amazon");
    // THEN
    assertThat(logic.data.get("aaa@bbb.com")).isEqualTo("{}");
  }*/

}
