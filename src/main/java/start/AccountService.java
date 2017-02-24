package start;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by egor on 23.02.17.
 */

@Service
public class AccountService {

    private Map<String, UserProfile> userNameToUserProfile = new HashMap<>();
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);


    AccountService() {
    }

    /**
     * Метод заносит профиль пользователя в БД, если такого пользователя в ней
     * еще нет. Проверка на существование пользователя в БД выполняется по полю email.
     *
     * @param userProfile профиль пользователя. Значение id в нем еще не установлено.
     * @return <tt>UserProfile userProfile</tt> если пользователя с таким email нет в БД.
     * <tt>null</tt> если пользователь с таким email уже есть в БД
     */

    public UserProfile register(@NotNull UserProfile userProfile) {
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String email = userProfile.getEmail(); //кэшируем значение, чтобы не вызывать геттер много раз
        if (!userNameToUserProfile.containsKey(userProfile.getEmail())) {
            userProfile.setId();
            userNameToUserProfile.put(email, userProfile);
            return userProfile;
        } else {
            return null;
        }
    }

    public boolean login(@NotNull String email, @NotNull String password) {
        UserProfile userProfile = userNameToUserProfile.get(email);

        if (userProfile != null) {
            if (userProfile.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public UserProfile getUser(String email) {
        return userNameToUserProfile.get(email);
    }

    //we need more methods
}