package br.edu.ulbra.election.election.service;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.edu.ulbra.election.election.client.CandidateClientService;
import br.edu.ulbra.election.election.exception.GenericOutputException;
import br.edu.ulbra.election.election.model.Election;
import br.edu.ulbra.election.election.model.Vote;
import br.edu.ulbra.election.election.output.v1.CandidateOutput;
import br.edu.ulbra.election.election.output.v1.ElectionCandidateResultOutput;
import br.edu.ulbra.election.election.output.v1.ElectionOutput;
import br.edu.ulbra.election.election.output.v1.ResultOutput;
import br.edu.ulbra.election.election.repository.ElectionRepository;
import br.edu.ulbra.election.election.repository.VoteRepository;
import feign.FeignException;

@Service
public class ResultService {

	private final VoteRepository voteRepository;
	private final ElectionRepository electionRepository;
	private final CandidateClientService candidateClientService;
	private final ModelMapper modelMapper;

	@Autowired
	public ResultService(VoteRepository voteRepository, CandidateClientService candidateClientService, ElectionRepository electionRepository, ModelMapper modelMapper) {
		this.voteRepository = voteRepository;
		this.electionRepository = electionRepository;
		this.candidateClientService = candidateClientService;
		this.modelMapper = modelMapper;
	}

	public ElectionCandidateResultOutput getResultByCandidate(Long candidateId) {

		ElectionCandidateResultOutput resultado = new ElectionCandidateResultOutput();      
		try {
			Long totalVotes = 0L;
			CandidateOutput candidateOutput = candidateClientService.getById(candidateId);
			resultado.setCandidate(candidateOutput);

			totalVotes = (long)voteRepository.findByCandidateId(candidateOutput.getNumberElection()).size();

			resultado.setTotalVotes(totalVotes);
		} catch (FeignException e) {
			if (e.status() == 500) {
				throw new GenericOutputException("Candidate not found");
			}
		}
		return resultado;
	}
	
	public ResultOutput getResultByElection(Long electionId) {
		ResultOutput result = new ResultOutput();
		List<Vote> votes = voteRepository.findByElectionId(electionId);
		
		if (votes.size() == 0) {
			throw new GenericOutputException("Election without votes");
		}
		
		Election election = electionRepository.findById(electionId).orElse(null);
		result.setElection(modelMapper.map(election, ElectionOutput.class));
		
		Long totalVotes = 0L;
		
		try {
			List<ElectionCandidateResultOutput> electionCandidateResults = new ArrayList<>();
			List<CandidateOutput> candidates = candidateClientService.getByElectionId(electionId);
			
			for (CandidateOutput candidate : candidates) {
				ElectionCandidateResultOutput candidateResult = new ElectionCandidateResultOutput();

				Long candidateVotes = votes.stream()
						.filter(s -> s.getCandidateId() != null)
						.filter(vote -> vote.getCandidateId().equals(candidate.getNumberElection())).count();
				
				totalVotes += candidateVotes;
				candidateResult.setCandidate(candidate);
				candidateResult.setTotalVotes(candidateVotes);
				
				electionCandidateResults.add(candidateResult);
			}
			
			result.setCandidates(electionCandidateResults);
		} catch (FeignException e) {
			if (e.status() == 500) {
				throw new GenericOutputException("Candidate not found");
			}
		}
		
		Long blankVotes = votes.stream().filter(vote -> vote.getBlankVote().equals(true)).count();
		Long nullVotes = votes.stream().filter(vote -> vote.getNullVote().equals(true)).count();
		
		result.setBlankVotes(blankVotes);
		result.setNullVotes(nullVotes);
		result.setTotalVotes(totalVotes);
		
		return result;
	}
}
