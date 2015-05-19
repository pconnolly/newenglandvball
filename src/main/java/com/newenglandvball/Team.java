package com.newenglandvball;
  
import java.util.List;
import java.util.ArrayList;

public class Team
{
  private String teamName = null;
  private String level = null;
  private String teamNumber = null;
  private Integer matchesWon = 0;
  private Integer matchesPlayed = 0;
  private Integer gamesWon = 0;
  private Integer gamesPlayed = 0;
  
  private List<TeamMatch> teamMatches = new ArrayList<TeamMatch>();

  public String getTeamName(){
    return teamName;
  }

  public void setTeamName(String teamName){
    this.teamName = teamName;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public String getTeamNumber(){
    return this.teamNumber;
  }
 
  public void setTeamNumber(String teamNumber){
    this.teamNumber = teamNumber;
  }

  public List<TeamMatch> getTeamMatches(){
    return this.teamMatches;
  }

  public void setTeamMatches(List<TeamMatch> teamMatches){
    this.teamMatches = teamMatches;
  }

  protected Integer getMatchesWon() {
    return matchesWon;
  }

  protected void setMatchesWon(Integer matchesWon) {
    this.matchesWon = matchesWon;
  }

  protected Integer getMatchesPlayed() {
    return matchesPlayed;
  }

  protected void setMatchesPlayed(Integer matchesPlayed) {
    this.matchesPlayed = matchesPlayed;
  }

  protected Integer getGamesWon() {
    return gamesWon;
  }

  protected void setGamesWon(Integer gamesWon) {
    this.gamesWon = gamesWon;
  }

  protected Integer getGamesPlayed() {
    return gamesPlayed;
  }

  protected void setGamesPlayed(Integer gamesPlayed) {
    this.gamesPlayed = gamesPlayed;
  }
  
}
