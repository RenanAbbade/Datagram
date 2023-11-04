

package com.datagram.datagramweb.config;


import com.datagram.datagramweb.domain.User;
import com.datagram.datagramweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("test")
public class TestConfig implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        User user = new User();
        user.setLogin("Renan@voll.med");
        String passwordEncoded = passwordEncoder.encode("123456");
        user.setPassword(passwordEncoded);
        userRepository.save(user);
    }
}
