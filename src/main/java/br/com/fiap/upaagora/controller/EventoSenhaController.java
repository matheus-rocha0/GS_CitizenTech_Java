package br.com.fiap.upaagora.controller;

import br.com.fiap.upaagora.bo.EventoSenhaBO;
import br.com.fiap.upaagora.model.EventoSenha;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@CrossOrigin(origins = "*")
public class EventoSenhaController {

    private final EventoSenhaBO eventoSenhaBO;

    public EventoSenhaController(EventoSenhaBO eventoSenhaBO) {
        this.eventoSenhaBO = eventoSenhaBO;
    }

    @GetMapping
    public ResponseEntity<List<EventoSenha>> listar(@RequestParam(required = false) Long idUnidade) {
        if (idUnidade != null) {
            return ResponseEntity.ok(eventoSenhaBO.listarPorUnidade(idUnidade));
        }
        return ResponseEntity.ok(eventoSenhaBO.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoSenha> buscarPorId(@PathVariable Long id) {
        EventoSenha evento = eventoSenhaBO.buscarPorId(id);
        return ResponseEntity.ok(evento);
    }

    @PostMapping
    public ResponseEntity<EventoSenha> registrar(@RequestBody EventoSenha e) {
        EventoSenha criado = eventoSenhaBO.registrarEvento(e);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoSenha> atualizar(@PathVariable Long id, @RequestBody EventoSenha e) {
        EventoSenha atualizado = eventoSenhaBO.atualizarEvento(id, e);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        eventoSenhaBO.deletarEvento(id);
        return ResponseEntity.noContent().build();
    }
}
