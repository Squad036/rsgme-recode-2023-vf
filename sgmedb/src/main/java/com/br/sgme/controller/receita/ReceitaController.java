package com.br.sgme.controller.receita;

import com.br.sgme.controller.receita.dto.ReceitaDto;
import com.br.sgme.ports.ReceitaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("/receitas")
public class ReceitaController {
    private final ReceitaUseCase receitaUseCase;

    @GetMapping()
    public List<ReceitaDto> getAllReceitas(@RequestParam String idUsuario){
        return receitaUseCase.get(idUsuario);
    }

    @GetMapping("/{id}")
    public ReceitaDto getById(@PathVariable String id){
        return receitaUseCase.getById(id);
    }

    @PostMapping("/cadastro")
    public ResponseEntity<?> save(@RequestBody @Validated ReceitaDto receitaDto){
        return receitaUseCase.save(receitaDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?>update(@PathVariable String id,@RequestBody ReceitaDto receitaDto){
        return receitaUseCase.update(id, receitaDto);
    }

    @DeleteMapping("/{id}")
    public void delete (@PathVariable String id){
        receitaUseCase.delete(id);
    }
}
