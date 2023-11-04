

package com.datagram.datagramweb.config;


import com.datagram.datagramweb.domain.User;
import com.datagram.datagramweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestConfig implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        User user = new User();
        user.setLogin("Renan");
        user.setPassword("xxxx");
        userRepository.save(user);
    }
}
