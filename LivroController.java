package com.biblioteca.biblioteca_api.controller;

import com.biblioteca.biblioteca_api.model.Livro;
import com.biblioteca.biblioteca_api.repository.LivroRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Year;
import java.util.List;

@RestController
@RequestMapping("/api/livros") // Rota base sugerida
public class LivroController {

    // Injeção de dependência do Repository
    private final LivroRepository livroRepository;

    @Autowired
    public LivroController(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    // Lógica de Negócio: Validação do Ano de Publicação
    private void validarAnoPublicacao(Integer ano) {
        int anoAtual = Year.now().getValue();
        if (ano == null || ano > anoAtual) {
            // Lança exceção que será mapeada para uma resposta HTTP 400 Bad Request
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O ano de publicação (" + ano + ") não pode ser nulo ou futuro.");
        }
    }

    // --- 1. CRIAR (Create) ---
    // POST /api/livros
    @PostMapping
    public ResponseEntity<Livro> criarLivro(@Valid @RequestBody Livro livro) {
        // Lógica de Negócio: Validação do ano (além das validações de Bean Validation)
        validarAnoPublicacao(livro.getAnoPublicacao());

        // Regra de Aplicação: Garantir que ID seja nulo na criação para que o DB gere
        livro.setId(null);

        Livro novoLivro = livroRepository.save(livro);
        return new ResponseEntity<>(novoLivro, HttpStatus.CREATED);
    }

    // --- 2. LER TODOS (Read All) ---
    // GET /api/livros
    @GetMapping
    public List<Livro> listarTodos() {
        return livroRepository.findAll();
    }

    // --- 3. LER POR ID (Read by ID) ---
    // GET /api/livros/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Livro> buscarPorId(@PathVariable Long id) {
        // Lógica de Negócio: Verifica se o livro existe
        return livroRepository.findById(id)
                .map(livro -> new ResponseEntity<>(livro, HttpStatus.OK))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Livro com ID " + id + " não encontrado."));
    }

    // --- 4. ATUALIZAR (Update) ---
    // PUT /api/livros/{id}
    // Implementação PUT (Atualização completa)
    @PutMapping("/{id}")
    public ResponseEntity<Livro> atualizarLivro(@PathVariable Long id, @Valid @RequestBody Livro livroAtualizado) {
        // Lógica de Negócio: 1. Verifica a existência do livro
        return livroRepository.findById(id)
                .map(livroExistente -> {
                    // Lógica de Negócio: 2. Validações de dados
                    validarAnoPublicacao(livroAtualizado.getAnoPublicacao());

                    // Aplica as atualizações no objeto existente
                    livroExistente.setTitulo(livroAtualizado.getTitulo());
                    livroExistente.setAutor(livroAtualizado.getAutor());
                    livroExistente.setIsbn(livroAtualizado.getIsbn());
                    livroExistente.setAnoPublicacao(livroAtualizado.getAnoPublicacao());
                    livroExistente.setDisponivel(livroAtualizado.isDisponivel());

                    // Salva e retorna o livro atualizado
                    Livro salvo = livroRepository.save(livroExistente);
                    return new ResponseEntity<>(salvo, HttpStatus.OK);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Livro com ID " + id + " não pode ser atualizado: não encontrado."));
    }

    // Opcional: Implementação PATCH (Atualização parcial)
    // Opcionalmente, pode-se usar PATCH para permitir a atualização de campos específicos
    @PatchMapping("/{id}")
    public ResponseEntity<Livro> atualizarParcialmente(@PathVariable Long id, @RequestBody Livro livroParcial) {
        return livroRepository.findById(id)
                .map(livroExistente -> {
                    // Lógica de Negócio: Aplica atualizações somente se o campo for fornecido
                    if (livroParcial.getTitulo() != null) {
                        livroExistente.setTitulo(livroParcial.getTitulo());
                    }
                    if (livroParcial.getAutor() != null) {
                        livroExistente.setAutor(livroParcial.getAutor());
                    }
                    if (livroParcial.getIsbn() != null) {
                        livroExistente.setIsbn(livroParcial.getIsbn());
                    }
                    if (livroParcial.getAnoPublicacao() != null) {
                        validarAnoPublicacao(livroParcial.getAnoPublicacao());
                        livroExistente.setAnoPublicacao(livroParcial.getAnoPublicacao());
                    }
                    // O booleano 'disponivel' pode ser atualizado via setter se estiver presente no JSON,
                    // mas requer uma verificação de presença mais complexa em um DTO,
                    // para simplicidade, a implementação PUT é mais direta.
                    // Para booleanos, geralmente a operação PUT é preferida ou um endpoint específico (ex: /emprestimo/{id}).

                    // Salva e retorna o livro atualizado
                    Livro salvo = livroRepository.save(livroExistente);
                    return new ResponseEntity<>(salvo, HttpStatus.OK);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Livro com ID " + id + " não pode ser atualizado: não encontrado."));
    }

    // --- 5. EXCLUIR (Delete) ---
    // DELETE /api/livros/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirLivro(@PathVariable Long id) {
        // Lógica de Negócio: Verifica a existência antes de tentar excluir
        if (!livroRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Livro com ID " + id + " não pode ser excluído: não encontrado.");
        }

        // Lógica de Negócio (Exemplo de Regra): Poderia verificar se o livro está emprestado antes de excluir.
        // if (livroRepository.findById(id).get().isDisponivel() == false) {
        //    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Livro emprestado não pode ser excluído.");
        // }

        livroRepository.deleteById(id);
        // Retorna status 204 No Content para indicar sucesso na exclusão
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}