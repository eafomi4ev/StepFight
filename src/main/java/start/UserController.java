package start;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
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
     * Данный метод вызывается с помощью reflection'a, поэтому Spring позволяет инжектить в него аргументы.
     * Подробнее можно почитать в сорцах к аннотации {@link RequestMapping}. Там описано как заинжектить различные атрибуты http-запроса.
     * Возвращаемое значение можно так же варьировать. Н.п. Если отдать InputStream, можно стримить музыку или видео
     */
    @RequestMapping(path = "/register", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public String register(@RequestBody UserProfile userProfile, HttpSession httpSession) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String name = userProfile.getName();
        if (userProfile.getName().isEmpty()) {
            return gson.toJson("Поле 'имя' пустое. Заполните его.");
        }
        if (userProfile.getPassword().isEmpty()) {
            return gson.toJson("Поле \"пароль\" пустое. Заполните его.");
        }
        if (userProfile.getEmail().isEmpty()) {
            return gson.toJson("Поле \"email\" пустое. Заполните его.");
        }
        if (userProfile.getNick().isEmpty()) {
            return gson.toJson("Поле \"ник\" пустое. Заполните его.");
        }

        userProfile = accountService.register(userProfile);

        if (userProfile != null) {
            httpSession.setAttribute("email", userProfile.getEmail());
            return gson.toJson(userProfile);
        } else {
            return gson.toJson("Пользователь с такими данными уже есть.");
        }


//        if (httpSession.getAttribute(email) != null) {
//            return gson.toJson(userProfile);
//        } else {
//            httpSession.setAttribute(email, true);
//            return gson.toJson(userProfile);
//        }


    }

    /**
     * Конструктор тоже будет вызван с помощью reflection'а. Другими словами, объект создается через ApplicationContext.
     * Поэтому в нем можно использовать DI. Подробнее про это расскажу на лекции.
     */
    public UserController(@NotNull AccountService accountService) {
        this.accountService = accountService;
    }


//    private static final class GetMsgRequest {
//        int userId;
//
//        @JsonCreator
//        GetMsgRequest(@JsonProperty("userId") int userId) {
//            this.userId = userId;
//        }
//
//        public int getUserId() {
//            return userId;
//        }
//    }
}
