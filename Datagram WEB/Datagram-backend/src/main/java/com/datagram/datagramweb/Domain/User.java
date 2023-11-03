package com.datagram.datagramweb.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;

@Table(name = "usuarios")
@Entity(name = "Usuario")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User implements UserDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String login;
    private String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    //No método getUsername(), devolvemos qual atributo da classe representa o Username. Por isso, ao invés de retornar null, vamos devolver o login. Aplicaremos a mesma lógica no método getPassword().
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }//No isCredentialsNonExpired(), é só caso quisermos controlar a conta do usuário: se há uma data de expiração ou se pode ter as credenciais bloqueadas. No caso, não faremos esse controle de conta, portanto, vamos devolver tudo como verdadeiro.

    @Override
    public boolean isEnabled() {
        return true;
    }
}
