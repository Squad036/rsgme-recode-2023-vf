package com.br.sgme.service;

import com.br.sgme.controller.cliente.dto.ClienteDto;
import com.br.sgme.exceptions.ErrorDetails;
import com.br.sgme.exceptions.RecursoNaoEncontradoException;
import com.br.sgme.model.Cliente;
import com.br.sgme.model.usuario.Usuario;
import com.br.sgme.ports.ClienteUseCase;
import com.br.sgme.repository.ClienteRepository;
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
public class ClienteUseCaseImpl implements ClienteUseCase {
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public ResponseEntity<?> save(ClienteDto clienteDto) {

        Usuario usuario = usuarioRepository.findById(clienteDto.getIdUsuario()).get();

        Optional<Cliente> clienteVerificado =
                clienteRepository.findByCpfAndUsuarioId(clienteDto.getCpf(), clienteDto.getIdUsuario());

        if (clienteVerificado.isPresent()) return new ResponseEntity<ErrorDetails>(ErrorDetails.builder()
                .codigo(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .time(LocalDateTime.now())
                .message("Cpf já cadastrado")
                .build(), HttpStatus.UNPROCESSABLE_ENTITY);


        Cliente cliente = Cliente.builder()
                .usuario(usuario)
                .cpf(clienteDto.getCpf())
                .nome(clienteDto.getNome())
                .dataNascimento(clienteDto.getDataNascimento())
                .telefone(clienteDto.getTelefone())
                .build();

        clienteRepository.save(cliente);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<?> update(String idCliente, ClienteDto clienteDto) {

        Cliente clienteSlecionado = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado"));

        Optional<Cliente> clienteVerificado = clienteRepository.findByCpfAndUsuarioId(clienteDto.getCpf(), clienteDto.getIdUsuario());

        if (clienteVerificado.isPresent() && !Objects.equals(clienteDto.getCpf(), clienteSlecionado.getCpf())) return new ResponseEntity<ErrorDetails>(ErrorDetails.builder()
                .codigo(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .time(LocalDateTime.now())
                .message("Cpf já cadastrado")
                .build(), HttpStatus.UNPROCESSABLE_ENTITY);

        Cliente cliente = Cliente.builder()
                .id(clienteSlecionado.getId())
                .usuario(clienteSlecionado.getUsuario())
                .cpf(clienteDto.getCpf())
                .nome(clienteDto.getNome())
                .dataNascimento(clienteDto.getDataNascimento())
                .telefone(clienteDto.getTelefone())
                .build();

        clienteRepository.save(cliente);

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();

    }

    @Override
    public List<ClienteDto> get(String idUsuario) {
        return clienteRepository.findByUsuarioId(idUsuario)
                .stream()
                .map(ClienteDto::to)
                .collect(Collectors.toList());
    }


    @Override
    public ClienteDto getId(String id) {
        return clienteRepository.findById(id)
                .stream()
                .map(ClienteDto::to).findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
    }

    @Override
    public ClienteDto getByCpf(String cpf, String idUsuario) {
        return clienteRepository.findByCpfAndUsuarioId(cpf, idUsuario)
                .stream()
                .map(ClienteDto::to).findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
    }

    @Override
    public void delete(String id) {
        if (clienteRepository.findById(id).isEmpty()) throw new RecursoNaoEncontradoException("Cliente não encontrado");
        clienteRepository.deleteById(id);
    }

}
