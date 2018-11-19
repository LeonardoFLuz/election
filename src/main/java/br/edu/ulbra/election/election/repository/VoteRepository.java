package br.edu.ulbra.election.election.repository;

import br.edu.ulbra.election.election.model.Vote;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface VoteRepository extends CrudRepository<Vote, Long> {
	Vote findFirstByVoterIdAndElectionId(Long voterId, Long electionId);
	List<Vote> findByVoterId(Long voterId);
	List<Vote> findByElectionId(Long electionId);
}
