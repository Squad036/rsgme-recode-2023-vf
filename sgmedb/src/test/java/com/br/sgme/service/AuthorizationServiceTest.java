package com.br.sgme.service;

import com.br.sgme.exceptions.LoginInvalidoException;
import com.br.sgme.model.usuario.Usuario;
import com.br.sgme.model.usuario.UsuarioRole;
import com.br.sgme.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthorizationServiceTest {

    AuthorizationService authorizationService;
    private final UsuarioRepository usuarioRepository = Mockito.mock(UsuarioRepository.class);

    @BeforeEach
    void setUp() {
        authorizationService = new AuthorizationService(usuarioRepository);
    }

    @Test
    @DisplayName("Retorna um usuario com sucesso")
    void buscarUsuario() {
        Usuario usuario = new  Usuario("1L", "UsuarioExistente", "login@domain.com", "123", UsuarioRole.ADMIN);

        Mockito.when(usuarioRepository.findByLogin("login@domain.com")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = authorizationService.loadUserByUsername("login@domain.com");

        Mockito.verify(usuarioRepository, Mockito.times(1)).findByLogin(Mockito.eq("login@domain.com"));

        assertEquals("123", userDetails.getPassword());
        assertEquals("login@domain.com", userDetails.getUsername());

    }

    @Test
    @DisplayName("Lancar um erro ao buscar um usuario")
    void erroAoBuscarUsuario() {
        String username = "usuarioInexistente";

        Mockito.when(usuarioRepository.findByLogin(username))
                .thenReturn(Optional.empty());

        LoginInvalidoException exception = assertThrows(LoginInvalidoException.class, ()->{
            authorizationService.loadUserByUsername(username);
        });

        assertEquals("Usuário inexistente ou senha inválida", exception.getMessage());

    }
}