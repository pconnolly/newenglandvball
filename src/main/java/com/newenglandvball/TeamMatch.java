package com.newenglandvball;

public class TeamMatch
{
  private String matchNumber = null;
  private String bracket = null;
  private String opponent = null;
  private String schedule = null;
  private String result = null;
  private String score = null;
  private Team   team = null;

  public void setMatchNumber(String matchNumber){
    this.matchNumber = matchNumber;
  }

  public String getMatchNumber(){
    return this.matchNumber;
  }

  public void setBracket(String bracket){
    this.bracket = bracket;
  }

  public String getBracket(){
    return this.bracket;
  }

  public void setOpponent(String opponent){
    this.opponent = opponent;
  }

  public String getOpponent(){
    return this.opponent;
  }

  public void setSchedule(String schedule){
    this.schedule = schedule;
  }

  public String getSchedule(){
    return this.schedule;
  }

  public void setResult(String result){
    this.result = result;
  }

  public String getResult(){
    return this.result;
  }

  public void setScore(String score){
    this.score = score;
  }

  public String getScore(){
    return this.score;
  }

  public void setTeam(Team team){
    this.team = team;
  }

  public Team getTeam(){
    return this.team;
  }
}
