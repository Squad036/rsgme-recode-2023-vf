package com.br.sgme.service;

import com.br.sgme.controller.usuario.dto.AuthenticationDto;
import com.br.sgme.controller.usuario.dto.LoginResponseDto;
import com.br.sgme.controller.usuario.dto.RegisterDto;
import com.br.sgme.exceptions.ErrorDetails;
import com.br.sgme.exceptions.LoginInvalidoException;
import com.br.sgme.infra.security.TokenService;
import com.br.sgme.model.usuario.Usuario;
import com.br.sgme.model.usuario.UsuarioRole;
import com.br.sgme.ports.UsuarioUseCase;
import com.br.sgme.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CONFLICT;

class UsuarioUseCaseImplTest {


    private final UsuarioRepository usuarioRepository = Mockito.mock(UsuarioRepository.class);
    private final TokenService tokenService = Mockito.mock(TokenService.class);
    private final AuthenticationManager authenticationManager = Mockito.mock(AuthenticationManager.class);


    private UsuarioUseCase usuarioUseCase;

    @BeforeEach
    void setup() {
        usuarioUseCase = new UsuarioUseCaseImpl(authenticationManager, usuarioRepository, tokenService);

    }

    @Test
    @DisplayName("Salvar um usuario com sucesso")
    void deveSalvarUsarioComSucesso() {

        when(usuarioRepository.findByLogin(Mockito.anyString())).thenReturn(Optional.empty());

        RegisterDto registerDto = new RegisterDto("Usuario", "usuario@domin.com", "123", UsuarioRole.ADMIN);

        ResponseEntity<?> response = usuarioUseCase.save(registerDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        Mockito.verify(usuarioRepository, Mockito.times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Retornar um erro se o  Login estiver em Uso")
    void deveLancarErroLoginEmUso() {
        RegisterDto registerDto = new RegisterDto("Usuario", "usuario@domain.com", "123", UsuarioRole.ADMIN);

        when(usuarioRepository.findByLogin("usuario@domain.com")).thenReturn(Optional.of(new Usuario("Usuario", "usuario@domain.com", "123", UsuarioRole.ADMIN)));

        ResponseEntity<ErrorDetails> response = (ResponseEntity<ErrorDetails>) usuarioUseCase.save(registerDto);

        assertEquals(CONFLICT, response.getStatusCode());

        assertEquals("Login em uso", response.getBody().getMessage());

    }


    @Test
    @DisplayName("Deve autenticar o usuario e retornar o token e o Usuario logado com sucesso")
    void deveFazerLoginDoUsuarioComSucesso() {
        AuthenticationDto authenticationDto = new AuthenticationDto("usuario@domain.com", "123");

        Usuario usuarioAutenticado = new Usuario("Usuario Logado", "usuario@domain.com", "123", UsuarioRole.ADMIN);

        when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(usuarioAutenticado, null));

        when(tokenService.generateToken(usuarioAutenticado)).thenReturn("Token Gerado");

        ResponseEntity<LoginResponseDto> response = usuarioUseCase.login(authenticationDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertEquals("Token Gerado", Objects.requireNonNull(response.getBody()).getToken());
        assertEquals("usuario@domain.com", Objects.requireNonNull(response.getBody()).getUsuario().getLogin());
        assertEquals("Usuario Logado", Objects.requireNonNull(response.getBody()).getUsuario().getNome());
        assertEquals(UsuarioRole.ADMIN, Objects.requireNonNull(response.getBody()).getUsuario().getRole());
    }

    @Test
    @DisplayName("Deve lancar erro ao efetuar login ao informar login inexistente ou senha invalida")
    void deveLancarErroAoEfetuarLogin() {

       when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("Credenciais invalidas"));

       LoginInvalidoException exception = assertThrows(LoginInvalidoException.class, ()->{
            AuthenticationDto authenticationDto = new AuthenticationDto("usuario@domain.com", "123");
            usuarioUseCase.login(authenticationDto);
        });
        assertEquals("Credenciais invalidas", exception.getMessage());
    }


}