package com.agendamento.repository;

import com.agendamento.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Busca por email em qualquer tipo de usuário
    @Query("SELECT u FROM Usuario u WHERE u.email = :email")
    Optional<Usuario> findByEmail(@Param("email") String email);

    // Verifica se email existe
    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);

    // Busca específica por tipo
    @Query("SELECT u FROM Usuario u WHERE TYPE(u) = :clazz AND u.email = :email")
    <T extends Usuario> Optional<T> findByEmailAndType(@Param("email") String email,
                                                       @Param("clazz") Class<T> clazz);
}