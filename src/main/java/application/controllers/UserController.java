package application.controllers;

import application.services.AccountService;
import application.start.UserProfile;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;

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

    @CrossOrigin
    @RequestMapping("/greeting")
    public String greeting(@RequestParam(value = "name", required = false, defaultValue = "World") String name, Model model) {
        model.addAttribute("name", name);
        return "greeting";
    }

    /**
     * Данный метод тоже вызывается с помощью reflection'a, поэтому Spring позволяет инжектить в него аргументы.
     * Подробнее можно почитать в сорцах к аннотации {@link RequestMapping}. Там описано как заинжектить различные атрибуты http-запроса.
     * Возвращаемое значение можно так же варьировать. Н.п. Если отдать InputStream, можно стримить музыку или видео
     * <p>
     * Описани параметров у RequestMapping см. по адресу:
     * http://ru.java.wikia.com/wiki/@RequestMapping
     */
    @CrossOrigin
    @RequestMapping(path = "/signup", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity signup(@RequestBody UserProfile userProfile, HttpSession httpSession) {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final JSONObject resultJson = new JSONObject();
        final ArrayList<ResponseMsg> errorsJson = new ArrayList<>();

//        ArrayList<ResponseMsg> msg = new ArrayList<>();

        if (StringUtils.isEmpty(userProfile.getName())) {
//            msg.add(new ResponseMsg("Поле 'имя' пустое. Заполните его."));
            errorsJson.add(new ResponseMsg("Поле 'имя' пустое. Заполните его."));
        }
        if (StringUtils.isEmpty(userProfile.getPassword())) {
//            msg.add(new ResponseMsg("Поле 'пароль' пустое. Заполните его."));
            errorsJson.add(new ResponseMsg("Поле 'пароль' пустое. Заполните его."));
        }
        if (StringUtils.isEmpty(userProfile.getEmail())) {
//            msg.add(new ResponseMsg("Поле 'email' пустое. Заполните его."));
            errorsJson.add(new ResponseMsg("Поле 'email' пустое. Заполните его."));
        }
        if (StringUtils.isEmpty(userProfile.getNick())) {
//            msg.add(new ResponseMsg("Поле 'ник' пустое. Заполните его."));
            errorsJson.add(new ResponseMsg("Поле 'ник' пустое. Заполните его."));
        }

        if (!errorsJson.isEmpty()) {
            resultJson.put("msg", errorsJson);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorsJson);
        }

        userProfile = accountService.signup(userProfile);

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

    @CrossOrigin
    @RequestMapping(path = "/login", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity login(@RequestBody UserProfile user, HttpSession httpSession) {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final JSONArray errorsJson = new JSONArray();
        final JSONObject resultJson = new JSONObject();
        final String email = user.getEmail();
        final String password = user.getPassword();

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
            user = accountService.getUser(email);
            httpSession.setAttribute("email", email);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new JSONObject().put("msg", "Неверный логин или пароль.").toString());
        }
    }

    @CrossOrigin
    @RequestMapping(path = "/logout", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity<String> logout(HttpSession httpSession) {
        if (!StringUtils.isEmpty(httpSession.getAttribute("email"))) {
            httpSession.removeAttribute("email");
            return ResponseEntity.ok(new JSONObject().put("msg", "Goodbye!").toString());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new JSONObject().put("msg", "Пользователь не авторизован.").toString());
        }
    }

    @CrossOrigin
    @RequestMapping(path = "/currentsessionuser", method = RequestMethod.GET,
            produces = "application/json", consumes = "application/json")
    public ResponseEntity currentSessionUser(HttpSession httpSession) {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final String sessionKey = (String) httpSession.getAttribute("email");
        if (!StringUtils.isEmpty(sessionKey)) {
            return ResponseEntity.ok(accountService.getUser(sessionKey));
//            return ResponseEntity.ok(gson.toJson(accountService.getUser(sessionKey)));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseMsg("Пользователь текущей сессии не авторизован."));
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
//                    new JSONObject().put("msg", "Пользователь текущей сессии не авторизован.").toString());
        }
    }

    @CrossOrigin
    @RequestMapping(path = "/test", method = RequestMethod.GET, produces = "application/json", consumes = "application/json")
    public ResponseEntity testconnection() {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseMsg("Пользователь текущей сессии не авторизован."));
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


    private static final class ResponseMsg {

        @JsonProperty
        String msg;


        @JsonCreator
        ResponseMsg(@JsonProperty String msg) {
            this.msg = msg;
        }
    }


}
