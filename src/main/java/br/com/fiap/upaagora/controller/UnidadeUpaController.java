package br.com.fiap.upaagora.controller;

import br.com.fiap.upaagora.bo.UnidadeUpaBO;
import br.com.fiap.upaagora.model.StatusLotacaoDTO;
import br.com.fiap.upaagora.model.UnidadeUpa;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/unidades")
@CrossOrigin(origins = "*")
public class UnidadeUpaController {

    private final UnidadeUpaBO unidadeUpaBO;

    public UnidadeUpaController(UnidadeUpaBO unidadeUpaBO) {
        this.unidadeUpaBO = unidadeUpaBO;
    }

    @GetMapping
    public ResponseEntity<List<UnidadeUpa>> listarTodas() {
        List<UnidadeUpa> unidades = unidadeUpaBO.listarTodas();
        return ResponseEntity.ok(unidades);
    }

    @GetMapping("/comparativo")
    public ResponseEntity<List<StatusLotacaoDTO>> listarComparativo() {
        List<StatusLotacaoDTO> comparativo = unidadeUpaBO.listarComparativoUnidades();
        return ResponseEntity.ok(comparativo);
    }

    @GetMapping("/catalogo")
    public ResponseEntity<List<Map<String, Object>>> listarCatalogo() {
        List<Map<String, Object>> catalogo = unidadeUpaBO.listarCatalogoFilaZero();
        return ResponseEntity.ok(catalogo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnidadeUpa> buscarPorId(@PathVariable Long id) {
        UnidadeUpa unidade = unidadeUpaBO.buscarPorId(id);
        return ResponseEntity.ok(unidade);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<StatusLotacaoDTO> obterStatusLotacao(@PathVariable Long id) {
        StatusLotacaoDTO status = unidadeUpaBO.calcularStatusLotacao(id);
        return ResponseEntity.ok(status);
    }

    @PostMapping
    public ResponseEntity<UnidadeUpa> criar(@RequestBody UnidadeUpa u) {
        UnidadeUpa criada = unidadeUpaBO.criar(u);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnidadeUpa> atualizar(@PathVariable Long id, @RequestBody UnidadeUpa u) {
        UnidadeUpa atualizada = unidadeUpaBO.atualizar(id, u);
        return ResponseEntity.ok(atualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        unidadeUpaBO.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
