package com.br.sgme.service;

import com.br.sgme.controller.receita.dto.ReceitaDto;
import com.br.sgme.enums.FormasPagamento;
import com.br.sgme.exceptions.RecursoNaoEncontradoException;
import com.br.sgme.model.Cliente;
import com.br.sgme.model.Receita;
import com.br.sgme.model.usuario.Usuario;
import com.br.sgme.model.usuario.UsuarioRole;
import com.br.sgme.ports.ReceitaUseCase;
import com.br.sgme.repository.ClienteRepository;
import com.br.sgme.repository.ReceitaRepository;
import com.br.sgme.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReceitaUseCaseImplTest {

    ReceitaUseCase receitaUseCase;

    private final UsuarioRepository usuarioRepository = Mockito.mock(UsuarioRepository.class);
    private final ClienteRepository clienteRepository = Mockito.mock(ClienteRepository.class);
    private final ReceitaRepository receitaRepository = Mockito.mock(ReceitaRepository.class);


    @BeforeEach
    void setUp() {
        receitaUseCase = new ReceitaUseCaseImpl(usuarioRepository, clienteRepository, receitaRepository);
    }

    @Test
    @DisplayName("Deve salvar um receita com sucesso")
    void salvarReceita() {
        Usuario usuarioRecuperado = getUsuario();
        Optional<Usuario> usuario = Optional.of(usuarioRecuperado);

        ReceitaDto receitaDto = getReceitaDto();
        Cliente clienteRecuperado = getCliente();
        Optional<Cliente> cliente = Optional.of(clienteRecuperado);

        Receita receitaEntity = Receita.builder()
                .usuario(usuarioRecuperado)
                .cliente(clienteRecuperado)
                .valor(receitaDto.getValor())
                .dataVencimento(receitaDto.getDataVencimento())
                .status(receitaDto.getStatus())
                .pagamento(FormasPagamento.valueOf(receitaDto.getFormaPagamento()))
                .observacao(receitaDto.getObservacao())
                .build();

        Mockito.when(usuarioRepository.findById(Mockito.eq("1L"))).thenReturn(usuario);
        Mockito.when(clienteRepository.findById(Mockito.eq("1L"))).thenReturn(cliente);

        ResponseEntity<?> save = receitaUseCase.save(receitaDto);

        Mockito.verify(receitaRepository, Mockito.times(1)).save(receitaEntity);
        assertEquals(HttpStatus.CREATED, save.getStatusCode());

    }
    @Test
    @DisplayName("Deve salvar um receita com sucesso com Observacao Vazia")
    void salvarReceitaComObservacaoVazia() {
        Usuario usuarioRecuperado = getUsuario();
        Optional<Usuario> usuario = Optional.of(usuarioRecuperado);

        ReceitaDto receitaDto = getReceitaDto();
        receitaDto.setObservacao(null);
        Cliente clienteRecuperado = getCliente();
        Optional<Cliente> cliente = Optional.of(clienteRecuperado);

        Receita receitaEntity = Receita.builder()
                .usuario(usuarioRecuperado)
                .cliente(clienteRecuperado)
                .valor(receitaDto.getValor())
                .dataVencimento(receitaDto.getDataVencimento())
                .status(receitaDto.getStatus())
                .pagamento(FormasPagamento.valueOf(receitaDto.getFormaPagamento()))
                .observacao("")
                .build();

        Mockito.when(usuarioRepository.findById(Mockito.eq("1L"))).thenReturn(usuario);
        Mockito.when(clienteRepository.findById(Mockito.eq("1L"))).thenReturn(cliente);

        ResponseEntity<?> save = receitaUseCase.save(receitaDto);

        Mockito.verify(receitaRepository, Mockito.times(1)).save(receitaEntity);
        assertEquals(HttpStatus.CREATED, save.getStatusCode());

    }

    @Test
    @DisplayName("Deve alterar uma despesa com sucesso")
    void alterarReceita() {
        Usuario usuario = getUsuario();
        ReceitaDto receitaDto = getReceitaDto();
        Cliente cliente = getCliente();

        Receita receitaRecuperada = new Receita(
                "1L",
                usuario,
                cliente,
                0.0,
                LocalDate.now(),
                "Pago",
                FormasPagamento.PIX,
                "Pedido 001"
        );

        Receita receitaEntity = Receita.builder()
                .id(receitaRecuperada.getId())
                .usuario(receitaRecuperada.getUsuario())
                .cliente(receitaRecuperada.getCliente())
                .valor(receitaDto.getValor())
                .dataVencimento(receitaDto.getDataVencimento())
                .status(receitaDto.getStatus())
                .pagamento(FormasPagamento.valueOf(receitaDto.getFormaPagamento()))
                .observacao(receitaDto.getObservacao())
                .build();

        Mockito.when(receitaRepository.findById(Mockito.eq("1L")))
                .thenReturn(Optional.of(receitaRecuperada));

        ResponseEntity<?> update = receitaUseCase.update("1L", receitaDto);

        Mockito.verify(receitaRepository, Mockito.times(1)).save(receitaEntity);

        assertEquals(HttpStatus.ACCEPTED, update.getStatusCode());
    }

    @Test
    @DisplayName("Deve lançar um erro ao alterar uma despesa")
    void erroAoAlterarReceita() {

        Mockito.when(receitaRepository.findById(Mockito.eq("1L"))).thenReturn(Optional.empty());

        ReceitaDto receitaDto = getReceitaDto();

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class,
                () -> receitaUseCase.update("1L", receitaDto));

        assertEquals("Receita não encontrada", exception.getMessage());

    }


    @Test
    @DisplayName("Deve listar as receitas no contexto de um usuário")
    void listarReceitasPorUsuario() {
        Usuario usuario = getUsuario();
        Cliente cliente = getCliente();

        Receita receita1 = new Receita("1L", usuario, cliente, 0.0, LocalDate.now(), "Pendente", FormasPagamento.BOLETO, "");
        Receita receita2 = new Receita("2L", usuario, cliente, 3.0, LocalDate.now(), "Pago", FormasPagamento.BOLETO, "");

        Mockito.when(receitaRepository.findByUsuarioId(Mockito.eq("1L"))).thenReturn(List.of(receita1, receita2));
        List<ReceitaDto> result = receitaUseCase.get("1L");

        Mockito.verify(receitaRepository, Mockito.times(1)).findByUsuarioId(Mockito.eq("1L"));

        assertEquals(2, result.size());
        assertEquals(0.0, result.get(0).getValor());
        assertEquals("1L", result.get(0).getIdCliente());
        assertEquals("1L", result.get(0).getIdUsuario());

        assertEquals(3.0, result.get(1).getValor());
        assertEquals("1L", result.get(1).getIdCliente());
        assertEquals("1L", result.get(1).getIdUsuario());
    }


    @Test
    @DisplayName("Deve listar uma despesa buscando por ID")
    void listarReceitaPorID() {

        Usuario usuario = getUsuario();
        Cliente cliente = getCliente();
        Receita receita =  new Receita("1L", usuario,cliente, 6.0, LocalDate.now(), "Pendente", FormasPagamento.BOLETO, ""  );

        Mockito.when(receitaRepository.findById(Mockito.eq("1L"))).thenReturn(Optional.of(receita));

        ReceitaDto result = receitaUseCase.getById("1L");

        Mockito.verify(receitaRepository, Mockito.times(1)).findById(Mockito.eq("1L"));

        assertEquals(6.0, result.getValor());
        assertEquals("1L", result.getIdUsuario());
        assertEquals("1L", result.getIdCliente());

    }


    @Test
    @DisplayName("Deve lancar um erro ao listar uma despesa buscando por ID")
    void erroAoListarReceitaPorID() {

       Mockito.when(receitaRepository.findById(Mockito.eq("1L"))).thenReturn(Optional.empty());

       RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class,
               ()-> receitaUseCase.getById("1L"));

       assertEquals("Receita não encontrada", exception.getMessage());

    }

    @Test
    @DisplayName("Deve excluir uma despesa com sucesso")
    void excluirReceita() {
        Usuario usuario = getUsuario();
        Cliente cliente = getCliente();
        Receita receita =  new Receita("1L", usuario,cliente, 6.0, LocalDate.now(), "Pendente", FormasPagamento.BOLETO, ""  );

        Mockito.when(receitaRepository.findById(Mockito.eq("1L"))).thenReturn(Optional.of(receita));
        receitaUseCase.delete("1L");

        Mockito.verify(receitaRepository, Mockito.times(1)).deleteById(Mockito.eq("1L"));

    }


    @Test
    @DisplayName("Deve lancar um erro ao excluir uma despesa")
    void erroAoExcluirReceita() {

        Mockito.when(receitaRepository.findById(Mockito.eq("1L"))).thenThrow(new RecursoNaoEncontradoException("Receita não encontrada"));
        RecursoNaoEncontradoException  exception = assertThrows(RecursoNaoEncontradoException.class,
                ()-> receitaUseCase.delete(Mockito.anyString()));

        assertEquals("Receita não encontrada", exception.getMessage());
    }



    private static Usuario getUsuario() {
        return new Usuario("1L", "Usuario", "login@domain.com", "123", UsuarioRole.ADMIN);
    }

    private static Cliente getCliente() {
        Usuario usuario = getUsuario();

        return new Cliente("1L", usuario, "99999999999", "Alice Lima", "88888888888", LocalDate.now());
    }


    private static ReceitaDto getReceitaDto() {
        return new ReceitaDto(
                "1L",
                "1L",
                "1L",
                0.0,
                LocalDate.now(),
                "Pendente",
                "CARTAO",
                "Pedido 001"
        );
    }
}