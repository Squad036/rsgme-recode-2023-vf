package com.br.sgme.service;

import com.br.sgme.controller.despesa.dto.DespesaDto;
import com.br.sgme.enums.FormasPagamento;
import com.br.sgme.exceptions.RecursoNaoEncontradoException;
import com.br.sgme.model.Despesa;
import com.br.sgme.model.Fornecedor;
import com.br.sgme.model.usuario.Usuario;
import com.br.sgme.model.usuario.UsuarioRole;
import com.br.sgme.ports.DespesaUseCase;
import com.br.sgme.repository.DespesaRepository;
import com.br.sgme.repository.FornecedorRepository;
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

class DespesaUseCaseImplTest {

    DespesaUseCase despesaUseCase;

    private final DespesaRepository despesaRepository = Mockito.mock(DespesaRepository.class);
    private final UsuarioRepository usuarioRepository = Mockito.mock(UsuarioRepository.class);
    private final FornecedorRepository fornecedorRepository = Mockito.mock(FornecedorRepository.class);

    @BeforeEach
    void setUp() {
        despesaUseCase = new DespesaUseCaseImpl(despesaRepository, usuarioRepository, fornecedorRepository);
    }

    @Test
    @DisplayName("Deve salvar uma despesa com sucesso")
    void salvarDespesa() {
        Usuario usuarioRecuperado = getUsuario();
        Optional<Usuario> usuario = Optional.of(usuarioRecuperado);
        DespesaDto despesaDto = getDespesaDto();
        Fornecedor fornecedorRecuperado = getFornecedor();
        Optional<Fornecedor> fornecedor = Optional.of(fornecedorRecuperado);

        Despesa despesaEntity = Despesa.builder()
                .usuario(usuarioRecuperado)
                .fornecedor(fornecedorRecuperado)
                .valor(despesaDto.getValor())
                .dataVencimento(despesaDto.getDataVencimento())
                .status(despesaDto.getStatus())
                .pagamento(FormasPagamento.valueOf(despesaDto.getFormaPagamento()))
                .observacao(despesaDto.getObservacao())
                .build();

        Mockito.when(usuarioRepository.findById(Mockito.eq("1L")))
                .thenReturn(usuario);

        Mockito.when(fornecedorRepository.findById(Mockito.eq("1L")))
                .thenReturn(fornecedor);

        ResponseEntity<?> save = despesaUseCase.save(despesaDto);


        Mockito.verify(despesaRepository, Mockito.times(1)).save(despesaEntity);

        assertEquals(HttpStatus.CREATED, save.getStatusCode());

    }


    @Test
    @DisplayName("Deve salvar uma despesa com sucesso com observaçao vazia")
    void salvarDespesaComObservacaoVazia() {
        Usuario usuarioRecuperado = getUsuario();
        Optional<Usuario> usuario = Optional.of(usuarioRecuperado);

        DespesaDto despesaDto = getDespesaDto();
        despesaDto.setObservacao(null);
        Fornecedor fornecedorRecuperado = getFornecedor();
        Optional<Fornecedor> fornecedor = Optional.of(fornecedorRecuperado);

        Despesa despesaEntity = Despesa.builder()
                .usuario(usuarioRecuperado)
                .fornecedor(fornecedorRecuperado)
                .valor(despesaDto.getValor())
                .dataVencimento(despesaDto.getDataVencimento())
                .status(despesaDto.getStatus())
                .pagamento(FormasPagamento.valueOf(despesaDto.getFormaPagamento()))
                .observacao("")
                .build();

        Mockito.when(usuarioRepository.findById(Mockito.eq("1L")))
                .thenReturn(usuario);

        Mockito.when(fornecedorRepository.findById(Mockito.eq("1L")))
                .thenReturn(fornecedor);

        ResponseEntity<?> save = despesaUseCase.save(despesaDto);


        Mockito.verify(despesaRepository, Mockito.times(1)).save(despesaEntity);

        assertEquals(HttpStatus.CREATED, save.getStatusCode());

    }



    @Test
    @DisplayName("Deve alterar uma despesa com sucesso")
    void alterarDespesa() {
        Usuario usuario = getUsuario();
        Fornecedor fornecedor = getFornecedor();
        DespesaDto despesaDto = getDespesaDto();

        Despesa despesaReceuperada = new Despesa(
                "1L",
                usuario,
                fornecedor,
                0.0,
                LocalDate.now(),
                "Pago",
                FormasPagamento.PIX,
                "Pedido 001");

        Despesa despesaEntity = Despesa.builder()
                .id(despesaReceuperada.getId())
                .usuario(despesaReceuperada.getUsuario())
                .fornecedor(despesaReceuperada.getFornecedor())
                .valor(despesaDto.getValor())
                .dataVencimento(despesaDto.getDataVencimento())
                .status(despesaDto.getStatus())
                .pagamento(FormasPagamento.valueOf(despesaDto.getFormaPagamento()))
                .observacao(despesaDto.getObservacao())
                .build();

        Mockito.when(despesaRepository.findById(Mockito.eq("1L")))
                .thenReturn(Optional.of(despesaReceuperada));

        ResponseEntity<?> update = despesaUseCase.update("1L", despesaDto);


        Mockito.verify(despesaRepository, Mockito.times(1)).save(Mockito.eq(despesaEntity));

        assertEquals(HttpStatus.ACCEPTED, update.getStatusCode());
    }

    @Test
    @DisplayName("Deve lançar  erro alterar uma despesa")
    void erroAoAlterarDespesa() {

        DespesaDto despesaDto = getDespesaDto();

        Mockito.when(despesaRepository.findById(Mockito.any()))
                .thenReturn(Optional.empty());

       RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, ()->{
          despesaUseCase.update("1L", despesaDto);
       });

       assertEquals("Despesa não encontrada", exception.getMessage());
    }

    @Test
    @DisplayName("Deve listar as despesas no contexto de um usuario")
    void listarDespesasPorUsuario() {
        Usuario usuario = getUsuario();
        Fornecedor fornecedor = getFornecedor();

        Despesa despesa1 = new Despesa("1L", usuario, fornecedor, 0.0, LocalDate.now(), "Pendente", FormasPagamento.BOLETO, "");
        Despesa despesa2 = new Despesa("2L", usuario, fornecedor, 0.0, LocalDate.now(), "Pendente", FormasPagamento.PIX, "");

        Mockito.when(despesaRepository.findByUsuarioId(Mockito.eq("1L"))).thenReturn(List.of(despesa1, despesa2));
        List<DespesaDto> result = despesaUseCase.get("1L");

        Mockito.verify(despesaRepository, Mockito.times(1)).findByUsuarioId(Mockito.eq("1L"));

        assertEquals(2,result.size());
        assertEquals(0.0, result.get(0).getValor());
        assertEquals("1L", result.get(0).getIdFornecedor());
        assertEquals("1L", result.get(0).getIdUsuario());

        assertEquals(0.0, result.get(1).getValor());
        assertEquals("1L", result.get(1).getIdFornecedor());
        assertEquals("1L", result.get(1).getIdUsuario());
    }

    @Test
    @DisplayName("Deve listar uma despesa por Id")
    void listarDespesasPorId() {
        Usuario usuario = getUsuario();
        Fornecedor fornecedor = getFornecedor();

        Despesa despesa = new Despesa("1L", usuario, fornecedor, 0.0, LocalDate.now(), "Pendente", FormasPagamento.BOLETO, "");

        Mockito.when(despesaRepository.findById(Mockito.eq("1L"))).thenReturn(Optional.of(despesa));
        DespesaDto result = despesaUseCase.getById("1L");

        Mockito.verify(despesaRepository, Mockito.times(1)).findById(Mockito.eq("1L"));

        assertEquals(0.0, result.getValor());
        assertEquals("1L", result.getIdFornecedor());
        assertEquals("1L", result.getIdUsuario());

    }

    @Test
    @DisplayName("Deve lancar um erro ao  buscar uma despesa por Id")
    void eeroAoListarDespesasPorId() {

        Mockito.when(despesaRepository.findById("1L")).thenThrow(new RecursoNaoEncontradoException("Despesa não encontrada"));

        RecursoNaoEncontradoException exception  = assertThrows(RecursoNaoEncontradoException.class, ()->{
            despesaUseCase.getById(Mockito.any());
        });

        assertEquals("Despesa não encontrada", exception.getMessage());

    }


    @Test
    @DisplayName("Deve deletar  uma despesa por Id")
    void excluirDespesasPorId() {
        Usuario usuario = getUsuario();
        Fornecedor fornecedor = getFornecedor();

        Despesa despesa = new Despesa("1L", usuario, fornecedor, 0.0, LocalDate.now(), "Pendente", FormasPagamento.BOLETO, "");


        Mockito.when(despesaRepository.findById(Mockito.eq("1L"))).thenReturn(Optional.of(despesa));
        despesaUseCase.delete("1L");

        Mockito.verify(despesaRepository, Mockito.times(1)).deleteById(Mockito.eq("1L"));

    }


    @Test
    @DisplayName("Deve lancar ao  deletar  uma despesa por Id nao existente")
    void erroAoExcluirDespesasPorId() {
       Mockito.when(despesaRepository.findById(Mockito.eq("1L"))).thenThrow(new RecursoNaoEncontradoException("Despesa não encontrada"));

       RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, ()->{
           despesaUseCase.delete(Mockito.anyString());
       });

       assertEquals("Despesa não encontrada", exception.getMessage());

    }



    private static Usuario getUsuario() {
        return new Usuario("1L", "Usuario", "login@domain.com", "123", UsuarioRole.ADMIN);
    }

    private static Fornecedor getFornecedor() {
        Usuario usuario = getUsuario();

        return new Fornecedor("1L", usuario, "03951550000155", "Empresa XPTO");
    }

    private static DespesaDto getDespesaDto() {
        return new DespesaDto(
                "1L",
                "1L",
                "1L",
                0.0,
                LocalDate.now(),
                "Pendente",
                "CARTAO",
                "Pedido 001");
    }


}