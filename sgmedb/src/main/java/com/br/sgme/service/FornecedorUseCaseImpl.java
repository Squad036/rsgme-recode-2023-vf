package com.br.sgme.service;

import com.br.sgme.controller.fornecedor.dto.FornecedorDto;
import com.br.sgme.exceptions.ErrorDetails;
import com.br.sgme.exceptions.RecursoNaoEncontradoException;
import com.br.sgme.model.Fornecedor;
import com.br.sgme.model.usuario.Usuario;
import com.br.sgme.ports.FornecedorUseCase;
import com.br.sgme.repository.FornecedorRepository;
import com.br.sgme.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FornecedorUseCaseImpl implements FornecedorUseCase {
    private final UsuarioRepository usuarioRepository;
    private final FornecedorRepository fornecedorRepository;


    @Override
    public ResponseEntity<?> save(FornecedorDto fornecedorDto) {
        Usuario usuario = usuarioRepository.findById(fornecedorDto.getIdUsuario()).get();


        Optional<Fornecedor> fornecedorVerificado =
                fornecedorRepository.findByCnpjAndUsuarioId(fornecedorDto.getCnpj(), fornecedorDto.getIdUsuario());

        if (fornecedorVerificado.isPresent()) return new ResponseEntity<ErrorDetails>(ErrorDetails.builder()
                .codigo(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .time(LocalDateTime.now())
                .message("CNPJ ou CPF já cadastrado")
                .build(),
                HttpStatus.UNPROCESSABLE_ENTITY);

        fornecedorRepository.save(Fornecedor.builder()
                .usuario(usuario)
                .cnpj(fornecedorDto.getCnpj())
                .nome(fornecedorDto.getNome())
                .build());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<?> update(String id, FornecedorDto fornecedorDto) {


        Fornecedor fornecedorSelecionado = fornecedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor não encontrado"));

        Optional<Fornecedor> fornecedorVerificado =
                fornecedorRepository.findByCnpjAndUsuarioId(fornecedorDto.getCnpj(), fornecedorDto.getIdUsuario());

        if (fornecedorVerificado.isPresent() && !Objects.equals(fornecedorDto.getCnpj(), fornecedorSelecionado.getCnpj())) return new ResponseEntity<ErrorDetails>(ErrorDetails.builder()
                .codigo(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .time(LocalDateTime.now())
                .message("CNPJ ou CPF já cadastrado")
                .build(),
                HttpStatus.UNPROCESSABLE_ENTITY);

        fornecedorRepository.save(Fornecedor.builder()
                .id(fornecedorSelecionado.getId())
                .usuario(fornecedorSelecionado.getUsuario())
                .cnpj(fornecedorDto.getCnpj())
                .nome(fornecedorDto.getNome())
                .build());

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Override
    public List<FornecedorDto> get(String idUsuario) {
        return fornecedorRepository.findByUsuarioId(idUsuario)
                .stream()
                .map(FornecedorDto::to)
                .collect(Collectors.toList());
    }

    @Override
    public FornecedorDto getById(String id) {
        return fornecedorRepository.findById(id)
                .stream()
                .map(FornecedorDto::to)
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor não encontrado"));
    }

    @Override
    public FornecedorDto getByCnpj(String cnpj, String idUsuario) {
        return fornecedorRepository.findByCnpjAndUsuarioId(cnpj, idUsuario)
                .stream()
                .map(FornecedorDto::to).findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor não encontrado"));
    }

    @Override
    public void delete(String id) {
        if (fornecedorRepository.findById(id).isEmpty())
            throw new RecursoNaoEncontradoException("Fornecedor não encontrado");
        fornecedorRepository.deleteById(id);

    }

}
