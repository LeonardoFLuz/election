package br.edu.ulbra.election.election.output.v1;

import br.edu.ulbra.election.election.output.v1.VoterOutput;
import br.edu.ulbra.election.election.output.v1.ElectionOutput;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Vote Output Information")
public class VoteOutput {
	@ApiModelProperty(example = "1", notes = "Vote Unique Identification")
    private Long id;
	@ApiModelProperty(example = "1", notes = "Election Unique Identification")
    private Long electionId;
    @ApiModelProperty(example = "2", notes = "Voter Unique Identifier")
    private Long voterId;
    @ApiModelProperty(example = "77100", notes = "Candidate Number")
    private Long candidateNumber;
    @ApiModelProperty(notes = "Candidate Election Data")
    private ElectionOutput electionOutput;
    @ApiModelProperty(notes = "Candidate Voter Data")
    private VoterOutput voterOutput;

    public Long getElectionId() {
        return electionId;
    }

    public void setElectionId(Long electionId) {
        this.electionId = electionId;
    }

    public Long getVoterId() {
        return voterId;
    }

    public void setVoterId(Long voterId) {
        this.voterId = voterId;
    }

    public Long getCandidateNumber() {
        return candidateNumber;
    }

    public void setCandidateNumber(Long candidateNumber) {
        this.candidateNumber = candidateNumber;
    }
    public ElectionOutput getElectionOutput() {
        return electionOutput;
    }

    public void setVoterOutput(VoterOutput voterOutput) {
        this.voterOutput = voterOutput;
    }
    public VoterOutput getVoterOutput() {
        return voterOutput;
    }

    public void setElectionOutput(ElectionOutput electionOutput) {
        this.electionOutput = electionOutput;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}

	
