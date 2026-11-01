package com.datagram.datagramweb.config;

import com.datagram.datagramweb.Repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Explicit, one-time migration for existing plaintext passwords. Never logs credentials. */
@Component
@ConditionalOnProperty(name = "app.migrate-legacy-passwords", havingValue = "true")
public class LegacyPasswordMigration implements CommandLineRunner {
  private final UsuarioRepository users;
  private final PasswordEncoder encoder;

  public LegacyPasswordMigration(UsuarioRepository users, PasswordEncoder encoder) {
    this.users = users;
    this.encoder = encoder;
  }

  @Override
  @Transactional
  public void run(String... args) {
    for (var user : users.findAll()) {
      String value = user.getSenha();
      if (value != null && !value.matches("\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}")) {
        user.setSenha(encoder.encode(value));
        users.save(user);
      }
    }
  }
}
