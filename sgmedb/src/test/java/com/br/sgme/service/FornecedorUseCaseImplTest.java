package com.br.sgme.service;

import com.br.sgme.controller.fornecedor.dto.FornecedorDto;
import com.br.sgme.exceptions.ErrorDetails;
import com.br.sgme.exceptions.RecursoNaoEncontradoException;
import com.br.sgme.model.Fornecedor;
import com.br.sgme.model.usuario.Usuario;
import com.br.sgme.model.usuario.UsuarioRole;
import com.br.sgme.ports.FornecedorUseCase;
import com.br.sgme.repository.FornecedorRepository;
import com.br.sgme.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FornecedorUseCaseImplTest {

    FornecedorUseCase fornecedorUseCase;

    private final UsuarioRepository usuarioRepository = Mockito.mock(UsuarioRepository.class);
    private final FornecedorRepository fornecedorRepository = Mockito.mock(FornecedorRepository.class);

    @BeforeEach
    void setUp() {
        fornecedorUseCase = new FornecedorUseCaseImpl(usuarioRepository, fornecedorRepository);
    }

    @Test
    @DisplayName("Deve Salvar um Fornecedor com sucesso")
    void salvarFornecedor() {
        Usuario usuarioRecuperdo = getUsuario();
        FornecedorDto fornecedorDto = getFornecedor();
        Optional<Usuario> usuario = Optional.of(usuarioRecuperdo);

        Fornecedor fornecedorEntity = Fornecedor.builder()
                .usuario(usuarioRecuperdo)
                .cnpj(fornecedorDto.getCnpj())
                .nome(fornecedorDto.getNome())
                .build();

        Mockito.when(usuarioRepository.findById(Mockito.eq("1L"))).thenReturn(usuario);
        ResponseEntity<?> save = fornecedorUseCase.save(fornecedorDto);


        Mockito.verify(fornecedorRepository, Mockito.times(1)).save(Mockito.eq(fornecedorEntity));
        assertEquals(HttpStatus.CREATED, save.getStatusCode());
    }


    @Test
    @DisplayName("Deve lancar erro ao tentar salvar com cpf ja existente")
    void erroAoSalvarComCnpjDuplicado() {

        Usuario usuario = getUsuario();
        FornecedorDto fornecedorDto = new FornecedorDto("1L", "1L", "03652445222211", "Empresa Xpto New");
        Fornecedor fornecedorExistente = new Fornecedor("1L", usuario, "03652445222211", "Empresa Xpto New");

        Mockito.when(fornecedorRepository.findByCnpjAndUsuarioId(Mockito.eq("03652445222211"), Mockito.eq("1L")))
                .thenReturn(Optional.of(fornecedorExistente));

        Mockito.when(usuarioRepository.findById(Mockito.anyString())).thenReturn(Optional.of(usuario));

        ResponseEntity<?> response = fornecedorUseCase.save(fornecedorDto);

        Mockito.verify(fornecedorRepository,
                Mockito.times(1)).findByCnpjAndUsuarioId("03652445222211", "1L");

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("CNPJ ou CPF já cadastrado", ((ErrorDetails) Objects.requireNonNull(response.getBody())).getMessage());
    }


    @Test
    @DisplayName("Deve alterar um Fornecedor com sucesso")
    void alterarFornecedor() {
        Usuario usuario = getUsuario();
        Fornecedor fornecedorRecuperado = new Fornecedor("1L", usuario, "03951662003166", "Empresa Xpto Recuperada");

        FornecedorDto fornecedorDto = getFornecedor();

        Fornecedor fornecedorEntity = Fornecedor.builder()
                .id(fornecedorRecuperado.getId())
                .usuario(fornecedorRecuperado.getUsuario())
                .cnpj(fornecedorDto.getCnpj())
                .nome(fornecedorDto.getNome())
                .build();

        Mockito.when(fornecedorRepository.findById(Mockito.eq("1L")))
                .thenReturn(Optional.of(fornecedorRecuperado));

        ResponseEntity<?> update = fornecedorUseCase.update("1L", fornecedorDto);

        Mockito.verify(fornecedorRepository, Mockito.times(1))
                .save(Mockito.eq(fornecedorEntity));

        assertEquals(HttpStatus.ACCEPTED, update.getStatusCode());
    }

    @Test
    @DisplayName("Deve lancar erro ao atualizar os dados de um Fornecedor e enviar um cnpj duplicado")
    void erroAoAtualizarComCnpjDuplicado() {

        Usuario usuario = getUsuario();
        Fornecedor fornecedorRecuperado = new Fornecedor("1L", usuario, "09651556000199", "Empresa Xto Recuperada");

        Fornecedor fornecedorExistente = new Fornecedor("2L", usuario, "09651556000166", "Empresa Xto Existente");

        FornecedorDto fornecedorDto = new FornecedorDto("3L", "1L", "09651556000166", "Empresa Xto Recuperada");

        Mockito.when(fornecedorRepository.findById("1L")).thenReturn(Optional.of(fornecedorRecuperado));

        Mockito.when(fornecedorRepository.findByCnpjAndUsuarioId(Mockito.eq("09651556000166"), Mockito.eq("1L")))
                .thenReturn(Optional.of(fornecedorExistente));

        ResponseEntity<?> update =  fornecedorUseCase.update("1L", fornecedorDto);

        Mockito.verify(fornecedorRepository, Mockito.times(1))
                .findByCnpjAndUsuarioId("09651556000166", "1L");

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, update.getStatusCode());
        assertEquals("CNPJ ou CPF já cadastrado", ((ErrorDetails) Objects.requireNonNull(update.getBody())).getMessage());

    }

    @Test
    @DisplayName("Deve lancar um erro ao  alterar um Fornecedor")
    void erroAoAlterarFornecedor() {
        FornecedorDto fornecedorDto = getFornecedor();

        Mockito.when(fornecedorRepository.findById("1L")).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class,
                ()->fornecedorUseCase.update("1L", fornecedorDto)
                );
        assertEquals("Fornecedor não encontrado", exception.getMessage());
    }


    @Test
    @DisplayName("Deve listar todos os fornecedores de um usuario")
    void buscarTodosFornecedoresPorIdUsuario() {

        Usuario usuario = getUsuario();

        Fornecedor fornecedor1 = new Fornecedor("1L", usuario, "09651556000199", "Empresa Xto 01");
        Fornecedor fornecedor2 = new Fornecedor("2L", usuario, "09651556000198", "Empresa Xto 02");

        Mockito.when(fornecedorRepository.findByUsuarioId("1L")).thenReturn(List.of(fornecedor1, fornecedor2));

        List<FornecedorDto> result = fornecedorUseCase.get("1L");

        Mockito.verify(fornecedorRepository, Mockito.times(1)).findByUsuarioId("1L");

        assertEquals(2, result.size());
        assertEquals("Empresa Xto 01", result.get(0).getNome());
        assertEquals("Empresa Xto 02", result.get(1).getNome());
        assertEquals("09651556000199", result.get(0).getCnpj());
        assertEquals("09651556000198", result.get(1).getCnpj());
    }


    @Test
    @DisplayName("Deve listar um fornecedor por ID")
    void buscarFornecedorIdUsuario() {

        Usuario usuario = getUsuario();

        Fornecedor fornecedor = new Fornecedor("1L", usuario, "09651556000199", "Empresa Xto 01");

        Mockito.when(fornecedorRepository.findById("1L")).thenReturn(Optional.of(fornecedor));

        FornecedorDto result = fornecedorUseCase.getById("1L");

        Mockito.verify(fornecedorRepository, Mockito.times(1)).findById("1L");

        assertEquals("Empresa Xto 01", result.getNome());
        assertEquals("09651556000199", result.getCnpj());
    }

    @Test
    @DisplayName("Deve lancar um erro ao buscar um fornecedor por ID Inexistente")
    void erroBuscarFornecedorIdUsuario() {

        Mockito.when(fornecedorRepository.findById(Mockito.anyString()))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class,
                () -> {
                    fornecedorUseCase.getById(Mockito.anyString());
                });
        assertEquals("Fornecedor não encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("Deve buscar um fornecedor por CNPJ")
    void buscarFornecedorPorCnpj() {

        Usuario usuario = getUsuario();
        Fornecedor fornecedor = new Fornecedor("1L", usuario, "09651556000199", "Empresa Xto 01");

        Mockito.when(fornecedorRepository.findByCnpjAndUsuarioId("09651556000199", "1L"))
                .thenReturn(Optional.of(fornecedor));

        FornecedorDto result = fornecedorUseCase.getByCnpj("09651556000199", "1L");

        Mockito.verify(fornecedorRepository, Mockito.times(1))
                .findByCnpjAndUsuarioId("09651556000199","1L");

        assertEquals("Empresa Xto 01", result.getNome());
        assertEquals("09651556000199", result.getCnpj());

    }

    @Test
    @DisplayName("Deve Lancar um erro ao buscar um fornecedor por CNPJ")
    void erroBuscarFornecedorPorCnpj() {

        Mockito.when(fornecedorRepository.findByCnpjAndUsuarioId("09651556000199", "1L"))
                .thenThrow(new RecursoNaoEncontradoException("Fornecedor não encontrado"));

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, ()->{
           fornecedorUseCase.getByCnpj(Mockito.anyString(), Mockito.anyString());
        });

        assertEquals("Fornecedor não encontrado", exception.getMessage());

    }

    @Test
    @DisplayName("Deve deletar um fornecedor por ID")
    void excluirFornecedorPorID(){
        Usuario usuario = getUsuario();
        Fornecedor fornecedor = new Fornecedor("1L", usuario, "09651556000199", "Empresa Xto 01");

        Mockito.when(fornecedorRepository.findById("1L")).thenReturn(Optional.of(fornecedor));
        fornecedorUseCase.delete("1L");

        Mockito.verify(fornecedorRepository, Mockito.times(1)).deleteById("1L");
    }

    @Test
    @DisplayName("Deve lancar um erro ao encluir um fornecedor por ID")
    void erroAoExcluirFornecedorPorID(){
        Mockito.when(fornecedorRepository.findById("1L")).thenThrow(new RecursoNaoEncontradoException("Fornecedor não encontrado"));

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, ()->{
            fornecedorUseCase.delete(Mockito.anyString());
        });
        assertEquals("Fornecedor não encontrado", exception.getMessage());
    }


    private static Usuario getUsuario() {
        return new Usuario("1L", "Usuario", "login@domain.com", "123", UsuarioRole.ADMIN);
    }

    private static FornecedorDto getFornecedor() {
        return new FornecedorDto("1L", "1L", "03951550000155", "Empresa XPTO");
    }


}