package models;

import io.ebean.Finder;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.security.SecureRandom;

@Entity
@Table(name = "tokens")
public class Token extends BaseModel {

    private static final int TOKEN_LENGTH = 20;

    private static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String lower = upper.toLowerCase();
    private static final String digits = "0123456789";
    private static final String chars = upper + digits + lower;

    private String token;

    @OneToOne(mappedBy = "token")
    private User user;

    private static final Finder<Long, Token> find =
            new Finder<>(Token.class);

    public Token() {
        super();
    }

    public static Token findByToken(String token) {
        return find
                .query()
                .where()
                    .eq("token", token)
                .findOne();
    }

    public static String generateToken() {
        StringBuilder sb = new StringBuilder(TOKEN_LENGTH);
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < TOKEN_LENGTH; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
