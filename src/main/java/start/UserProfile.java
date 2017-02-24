package start;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by egor on 23.02.17.
 */

public class UserProfile {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    private Long id;

    private String name;
    private String password;
    private String email;
    private String nick;

//    UserProfile(String name, String password, String email, String nick){
//        this.id = ID_GENERATOR.getAndIncrement();
//        this.name = name;
//        this.password = password;
//        this.email = email;
//        this.nick = nick;
//    }

    UserProfile(@JsonProperty("name") String name, @JsonProperty("password") String password,
                @JsonProperty("email") String email, @JsonProperty("nick") String nick) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.nick = nick;
    }


    public Long getId() {
        return id;
    }

    protected final void setId() {
        this.id = ID_GENERATOR.getAndIncrement();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getName() {

        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getNick() {
        return nick;
    }

}
