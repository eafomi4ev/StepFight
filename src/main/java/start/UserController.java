package start;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * Created by egor on 23.02.17.
 */
@RestController
@RequestMapping("/api/")
public class UserController {

    @NotNull
    private final AccountService accountService;

    /**
     * Конструктор будет вызван с помощью reflection'а. Другими словами, объект создается через ApplicationContext.
     * Поэтому в нем можно использовать DI. Подробнее про это расскажу на лекции.
     */
    public UserController(@NotNull AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Данный метод тоже вызывается с помощью reflection'a, поэтому Spring позволяет инжектить в него аргументы.
     * Подробнее можно почитать в сорцах к аннотации {@link RequestMapping}. Там описано как заинжектить различные атрибуты http-запроса.
     * Возвращаемое значение можно так же варьировать. Н.п. Если отдать InputStream, можно стримить музыку или видео
     */
    //TODO: Method "register" -   Обработать 500 ошибки. К примеру, если прислать неполные данные в json, то вылетит 500 ошибка.
    @RequestMapping(path = "/register", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity<String> register(@RequestBody UserProfile userProfile, HttpSession httpSession) {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final JSONObject resultJson = new JSONObject();
        final JSONArray errorsJson = new JSONArray();

        if (StringUtils.isEmpty(userProfile.getName())) {
            errorsJson.put("Поле 'имя' пустое. Заполните его.");
        }
        if (StringUtils.isEmpty(userProfile.getPassword())) {
            errorsJson.put("Поле 'пароль' пустое. Заполните его.");
        }
        if (StringUtils.isEmpty(userProfile.getEmail())) {
            errorsJson.put("Поле 'email' пустое. Заполните его.");
        }
        if (StringUtils.isEmpty(userProfile.getNick())) {
            errorsJson.put("Поле 'ник' пустое. Заполните его.");
        }

        if (errorsJson.length() != 0) {
            resultJson.put("msg", errorsJson);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultJson.toString());
        }

        userProfile = accountService.register(userProfile);

        if (userProfile != null) {
//            httpSession.setAttribute("email", userProfile.getEmail());
            return ResponseEntity.ok(gson.toJson(userProfile));
        } else {
//            return resultJson.put("error", "Такой пользователь уже существует").toString();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JSONObject().put("msg", "Этот email уже занят.").toString());
        }


//        if (httpSession.getAttribute(email) != null) {
//            return gson.toJson(userProfile);
//        } else {
//            httpSession.setAttribute(email, true);
//            return gson.toJson(userProfile);
//        }


    }

    @RequestMapping(path = "/login", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity<String> login(@RequestBody UserProfile userProfile, HttpSession httpSession) {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final JSONArray errorsJson = new JSONArray();
        final JSONObject resultJson = new JSONObject();
        final String email = userProfile.getEmail();
        final String password = userProfile.getPassword();

        if (StringUtils.isEmpty(password)) {
            errorsJson.put("Поле 'пароль' пустое. Заполните его.");
        }
        if (StringUtils.isEmpty(email)) {
            errorsJson.put("Поле 'email' пустое. Заполните его.");
        }

        if (errorsJson.length() != 0) {
            resultJson.put("msg", errorsJson);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultJson.toString());
        }

        if (accountService.login(email, password)) {
            userProfile = accountService.getUser(email);
            httpSession.setAttribute("email", email);
            return ResponseEntity.ok(gson.toJson(userProfile));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new JSONObject().put("msg", "Неверный логин или пароль.").toString());
        }
    }

    @RequestMapping(path = "/currentsessionuser", method = RequestMethod.POST,
            produces = "application/json", consumes = "application/json")
    public ResponseEntity<String> currentSessionUser(HttpSession httpSession) {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final String sessionKey = (String) httpSession.getAttribute("email");
        if (!StringUtils.isEmpty(sessionKey)) {
            return ResponseEntity.ok(gson.toJson(accountService.getUser(sessionKey)));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new JSONObject().put("msg", "Пользователь текущей сессии не авторизован.").toString());
        }
    }

    @RequestMapping(path = "/logout", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity<String> changeUserData(HttpSession httpSession) {
        if (!StringUtils.isEmpty(httpSession.getAttribute("email"))) {
            httpSession.removeAttribute("email");
            return ResponseEntity.ok(new JSONObject().put("msg", "Goodbye!").toString());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new JSONObject().put("msg", "Пользователь не авторизован.").toString());
        }
    }


    private static final class SessionKey {
        String key;

        @JsonCreator
        SessionKey(@JsonProperty("email") String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
}
