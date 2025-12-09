package com.biblioteca.biblioteca_api.repository;

import com.biblioteca.biblioteca_api.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository responsável pela comunicação com o banco de dados para a entidade Livro.
 * O Spring Data JPA fornece automaticamente a implementação para as operações CRUD.
 */
@Repository
public interface LivroRepository extends JpaRepository<Livro, Long> {
    // Métodos CRUD básicos: save, findById, findAll, deleteById, etc.,
    // são herdados automaticamente do JpaRepository.

    // Você pode adicionar métodos personalizados se precisar, ex:
    // List<Livro> findByAutor(String autor);
}