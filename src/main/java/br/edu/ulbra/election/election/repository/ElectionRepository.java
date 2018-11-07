package br.edu.ulbra.election.election.repository;

import br.edu.ulbra.election.election.model.Election;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface ElectionRepository extends CrudRepository<Election, Long> {
	List<Election> findByYear(Integer year);
}
