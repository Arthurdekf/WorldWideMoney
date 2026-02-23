package com.financeiro.analisador.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.financeiro.analisador.model.Ativo;

@Repository
public interface AtivoRepository extends JpaRepository<Ativo, Long> {
}