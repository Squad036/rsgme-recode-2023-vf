package com.br.sgme.service;

import com.br.sgme.controller.cliente.dto.ClienteDto;
import com.br.sgme.exceptions.ErrorDetails;
import com.br.sgme.exceptions.RecursoNaoEncontradoException;
import com.br.sgme.model.Cliente;
import com.br.sgme.model.usuario.Usuario;
import com.br.sgme.model.usuario.UsuarioRole;
import com.br.sgme.ports.ClienteUseCase;
import com.br.sgme.repository.ClienteRepository;
import com.br.sgme.repository.UsuarioRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClienteUseCaseImplTest {
    ClienteUseCase clienteUseCase;
    private final UsuarioRepository usuarioRepository = Mockito.mock(UsuarioRepository.class);
    private final ClienteRepository clienteRepository = Mockito.mock(ClienteRepository.class);

    @BeforeEach
    void setUp() {
        clienteUseCase = new ClienteUseCaseImpl(clienteRepository, usuarioRepository);
    }

    @Test
    @DisplayName("Deve Salvar um cliente com sucesso")
    void deveSalvarUmClienteComSucesso() {

        ClienteDto clienteDto = getClienteDto();
        Usuario usuarioRecuperado = getUsuario();
        Optional<Usuario> usuario = Optional.of(usuarioRecuperado);

        Cliente clienteEntity = Cliente.builder()
                .usuario(usuarioRecuperado)
                .cpf(clienteDto.getCpf())
                .nome(clienteDto.getNome())
                .dataNascimento(clienteDto.getDataNascimento())
                .telefone(clienteDto.getTelefone())
                .build();

        Mockito.when(usuarioRepository.findById(Mockito.eq("1L")))
                .thenReturn(usuario);

        ResponseEntity<?> save = clienteUseCase.save(clienteDto);

        Mockito.verify(clienteRepository, Mockito.times(1))
                .save(Mockito.eq(clienteEntity));

        assertEquals(HttpStatus.CREATED, save.getStatusCode());

    }


    @Test
    @DisplayName("Deve lancar erro ao tentar salvar com cpf ja existente")
    void erroAoSalvarComCpfDuplicado() {

        Usuario usuario = getUsuario();
        ClienteDto clienteDto = new ClienteDto("1L",
                "1L",
                "04625588899",
                "Helena Lima",
                "21999999988" ,
                LocalDate.now());

        Cliente clienteExistente = new Cliente(
                "1L", usuario,
                "04625588899",
                "Alice Lima",
                "21999999999" ,
                LocalDate.now());

        Mockito.when(usuarioRepository.findById(Mockito.eq("1L"))).thenReturn(Optional.of(usuario));

        Mockito.when(clienteRepository.findByCpfAndUsuarioId(Mockito.eq("04625588899"), Mockito.eq("1L")))
                .thenReturn(Optional.of(clienteExistente));

     ResponseEntity<?> response = clienteUseCase.save(clienteDto);

     Mockito.verify(clienteRepository,
             Mockito.times(1)).findByCpfAndUsuarioId("04625588899", "1L");

     assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
     assertEquals("Cpf já cadastrado", ((ErrorDetails) Objects.requireNonNull(response.getBody())).getMessage());
    }




    @Test
    @DisplayName("Deve alterar os dados do cliente com sucesso")
    void alterarClienteComSucesso() {

        Usuario usuario = getUsuario();
        Cliente clienteReceuperado = new Cliente("1L", usuario, "99999999988", "Helena Lima", "88888888888", LocalDate.now());

        ClienteDto clienteDto = getClienteDto();

        Cliente clienteEntity = Cliente.builder()
                .id(clienteReceuperado.getId())
                .usuario(clienteReceuperado.getUsuario())
                .cpf(clienteDto.getCpf())
                .nome(clienteDto.getNome())
                .dataNascimento(clienteDto.getDataNascimento())
                .telefone(clienteDto.getTelefone())
                .build();


        Mockito.when(clienteRepository.findById(Mockito.eq("1L")))
                .thenReturn(Optional.of(clienteReceuperado));

        ResponseEntity<?> update = clienteUseCase.update("1L", clienteDto);


        Mockito.verify(clienteRepository, Mockito.times(1))
                .save(Mockito.eq(clienteEntity));

        assertEquals(HttpStatus.ACCEPTED, update.getStatusCode());
    }



    @Test
    @DisplayName("Deve  lancar error ao realizar o update dos dados com CPF duplicado")
    void errorAoAtualizarComCpfDuplicado() {

        Usuario usuario = getUsuario();
        Cliente clienteReceuperado = new Cliente("1L", usuario, "06452285655", "Alice Lima", "88888888888", LocalDate.now());

        Cliente clienteExistente = new Cliente("2L", usuario, "02365544455", "Helena Lima", "88888888888", LocalDate.now());

        ClienteDto clienteDto = new ClienteDto("1L", "1L", "02365544455", "Helena Lima", "88888888888", LocalDate.now());

        Mockito.when(clienteRepository.findById("1L")).thenReturn(Optional.of(clienteReceuperado));

        Mockito.when(clienteRepository.findByCpfAndUsuarioId("02365544455", "1L"))
                .thenReturn(Optional.of(clienteExistente));

        ResponseEntity<?> response = clienteUseCase.update("1L",  clienteDto);

        Mockito.verify(clienteRepository, Mockito.times(1)).findByCpfAndUsuarioId("02365544455","1L");

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("Cpf já cadastrado", ((ErrorDetails) Objects.requireNonNull(response.getBody())).getMessage());

    }

    @Test
    @DisplayName("Deve lancar um erro ao realizar o update dos dados de um cliente nao existente")
    void deveLancarErroAoAtualizar() {

        ClienteDto clienteDto = getClienteDto();

       Mockito.when(clienteRepository.findById(Mockito.eq("1L"))).thenReturn(Optional.empty());

       RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class,
               ()->clienteUseCase.update("1L", clienteDto)
               );
    }

    @Test
    @DisplayName("Deve Listar os clientes de um Usuario")
    void deveListarClientesComSucesso() {

        Usuario usuario = getUsuario();
        Cliente cliente1 = new Cliente("1L", usuario, "06452285655", "Alice Lima", "99999999999", LocalDate.now());
        Cliente cliente2 = new Cliente("2L", usuario, "02365544455", "Helena Lima", "99999999999", LocalDate.now());

        Mockito.when(clienteRepository.findByUsuarioId("1L")).thenReturn(List.of(cliente1, cliente2));

        List<ClienteDto> result = clienteUseCase.get("1L");

        Mockito.verify(clienteRepository, Mockito.times(1)).findByUsuarioId("1L");

        assertEquals(2, result.size());
        assertEquals("Alice Lima", result.get(0).getNome());
        assertEquals("Helena Lima", result.get(1).getNome());
        assertEquals("06452285655", result.get(0).getCpf());
        assertEquals("02365544455", result.get(1).getCpf());

    }


    @Test
    @DisplayName("Deve Listar um cliente buscando pelo ID")
    void deveListarClientePorId() {

        Usuario usuario = getUsuario();
        Cliente cliente = new Cliente("1L", usuario, "06452285655", "Alice Lima", "99999999999", LocalDate.now());

        Mockito.when(clienteRepository.findById("1L")).thenReturn(Optional.of(cliente));

        ClienteDto result = clienteUseCase.getId("1L");

        Mockito.verify(clienteRepository, Mockito.times(1)).findById("1L");
        assertEquals("Alice Lima", result.getNome());
        assertEquals("06452285655", result.getCpf());

    }


    @Test
    @DisplayName("Deve Lancar Erro ao buscar um cliente pelo ID Inexistente")
    void deveLancarErroBuscandoClienteInexistente() {

        Mockito.when(clienteRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            clienteUseCase.getId(Mockito.any());
        });

        assertEquals("Cliente não encontrado", exception.getMessage());

    }


    @Test
    @DisplayName("Deve Listar um cliente buscando pelo CPF")
    void deveListarClientePorCPF() {

        Usuario usuario = getUsuario();
        Cliente cliente = new Cliente("1L", usuario, "06452285655", "Alice Lima", "99999999999", LocalDate.now());

        Mockito.when(clienteRepository.findByCpfAndUsuarioId("06452285655", "1L"))
                .thenReturn(Optional.of(cliente));

        ClienteDto result = clienteUseCase.getByCpf("06452285655", "1L");

        Mockito.verify(clienteRepository, Mockito.times(1)).findByCpfAndUsuarioId("06452285655", "1L");
        assertEquals("Alice Lima", result.getNome());
        assertEquals("06452285655", result.getCpf());

    }

    @Test
    @DisplayName("Deve Lancar Erro ao buscar cliente buscando pelo CPF")
    void deveLancarErroAoBuscarClientePorCPF() {


        Mockito.when(clienteRepository.findByCpfAndUsuarioId("06452285655", "1L")).thenThrow(new RecursoNaoEncontradoException("Cliente não encontrado"));

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            clienteUseCase.getByCpf(Mockito.anyString(), Mockito.anyString());
        });

        assertEquals("Cliente não encontrado", exception.getMessage());


    }


    @Test
    @DisplayName("Deve Deletar um cliente  pelo ID")
    void deveDeletarClientePorId() {

        Usuario usuario = getUsuario();
        Cliente cliente = new Cliente("1L", usuario, "06452285655", "Alice Lima", "99999999999", LocalDate.now());

        Mockito.when(clienteRepository.findById("1L")).thenReturn(Optional.of(cliente));
        clienteUseCase.delete("1L");

        Mockito.verify(clienteRepository, Mockito.times(1)).deleteById(Mockito.eq("1L"));
    }

    @Test
    @DisplayName("Deve Lancar Erro ao Deletar um cliente  pelo ID que nao existe")
    void deveLancarErroAoDeletarClientePorId() {

        Mockito.when(clienteRepository.findById("1L")).thenThrow(new RecursoNaoEncontradoException("Cliente nao encontrado"));

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            clienteUseCase.delete(Mockito.anyString());
        });

        assertEquals("Cliente não encontrado", exception.getMessage());

    }


    private static Usuario getUsuario() {
        return new Usuario("1L", "Usuario", "login@domain.com", "123", UsuarioRole.ADMIN);
    }


    private static ClienteDto getClienteDto() {
        return new ClienteDto("1L", "1L", "99999999999", "Alice Lima", "88888888888", LocalDate.now());
    }


}