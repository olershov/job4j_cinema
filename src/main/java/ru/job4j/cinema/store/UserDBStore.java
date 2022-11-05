package ru.job4j.cinema.store;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import ru.job4j.cinema.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

@Repository
public class UserDBStore {
    private final BasicDataSource pool;
    private static final Logger LOG = LoggerFactory.getLogger(UserDBStore.class.getName());
    private static final String ADD = "INSERT INTO users(username, email, phone) VALUES (?, ?, ?)";
    private static final String FIND_EMAIL_PHONE = "SELECT * FROM users WHERE email = ? AND phone = ?";

    public UserDBStore(BasicDataSource pool) {
        this.pool = pool;
    }

    public Optional<User> add(User user) {
        Optional<User> result = Optional.empty();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement(ADD, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    user.setId(id.getInt(1));
                    result = Optional.of(user);
                }
            }
        } catch (Exception e) {
            LOG.error("Exception in UserDBStore.add()", e);
        }
        return result;
    }

    public Optional<User> findUserByEmailAndPhone(String email, String phone) {
        Optional<User> result = Optional.empty();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement(FIND_EMAIL_PHONE)) {
            ps.setString(1, email);
            ps.setString(2, phone);
            try (ResultSet it = ps.executeQuery()) {
                if (it.next()) {
                    User user = new User(
                            it.getInt("id"), it.getString("username"), it.getString("email"), it.getString("phone")
                            );
                    result = Optional.of(user);
                }
            }
        } catch (Exception e) {
            LOG.error("Exception in UserDBStore.findUserByEmailAndPhone()", e);
        }
        return result;
    }
}