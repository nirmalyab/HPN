package org.nirmalya.hpn;

public class Scores {
	
	public Scores(double zScore, int penalty) {
		this.zScore = zScore;
		this.penalty = penalty;
	}
	public double getZScore() {
		return zScore;
	}
	public void setZScore(double zScore) {
		this.zScore = zScore;
	}  
	public int getPenalty() {
		return penalty;
	}
	public void setPenalty(int penalty) {
		this.penalty = penalty;
	}
	double zScore;
	int penalty;

}
