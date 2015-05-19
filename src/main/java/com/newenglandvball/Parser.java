package com.newenglandvball;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Parser {

    private static final String ROOT_URL = "/EventResults/(S(fhsado5552dooknbzbhtdpvv))/";
    private static final String TEAM_SCHEDULE_PAGE = "TeamSchedule.aspx?e="; 

    private static final String TEAM_PAGE_NAME = ROOT_URL + TEAM_SCHEDULE_PAGE;

    private final String[] teamNumbers;
    private final int timeBetweenPageGets;
    private final String eventId;

    public Parser(String[] teamNumbers, int timeBetweenPageGets, String eventId){
      this.teamNumbers = teamNumbers;  
      this.timeBetweenPageGets = timeBetweenPageGets;
      this.eventId = eventId;
    }

    protected String[] getTeamNumbers() {
      return teamNumbers;
    }

    protected int getTimeBetweenPageGets() {
      return timeBetweenPageGets;
    }

    protected String getEventId() {
      return eventId;
    }

    public List<Team> getTeams() 
      throws Exception 
    {
        List<Team> teams = new ArrayList<Team>(); 

        CloseableHttpClient httpClient = HttpClients.createDefault();
        ResponseHandler<String> responseHandler = new ResponseHandler<String>(){
          public String handleResponse(final HttpResponse response) 
            throws ClientProtocolException, IOException 
          {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
              HttpEntity entity = response.getEntity();
              return entity != null ? EntityUtils.toString(entity) : null;
            } else {
              throw new ClientProtocolException("Unexpected response status: " + status);
            }
          }
        };

        try {
          for (int i = 0; i < this.getTeamNumbers().length; i++) {
            HttpGet httpget = new HttpGet("http://advancedeventsystems.com/" + TEAM_PAGE_NAME + this.getEventId() + "&t=" + this.getTeamNumbers()[i]);
            String teamHtmlPage = httpClient.execute(httpget, responseHandler);
            Team team = getTeamFromHtml(teamHtmlPage, this.getTeamNumbers()[i]);
            teams.add(team);

            Thread.sleep(this.getTimeBetweenPageGets()); //Give the AES system a breather
          }
        } finally {
          httpClient.close();
        }
      return teams;
    }
  
    private Team getTeamFromHtml(String teamHtmlPage, String teamNumber){
      String teamNameBeginToken = "value=\"" + teamNumber + "\">";
      String teamNameEndToken = "</option>";
      String teamName = this.getString(teamHtmlPage, teamNameBeginToken, teamNameEndToken);
      
      Team team = new Team();
      team.setTeamName(teamName);
      team.setTeamNumber(teamNumber);

      String teamLevelIdBeginToken = TEAM_SCHEDULE_PAGE + this.getEventId() + "&amp;d=";
      String teamLevelIdEndToken = "\">";
      String levelId = this.getString(teamHtmlPage, teamLevelIdBeginToken, teamLevelIdEndToken);
      
      String teamLevelBeginToken = "selected=\"selected\" value=\"" + levelId + "\">";
      String teamLevelEndToken = "</option>";
      String level = this.getString(teamHtmlPage, teamLevelBeginToken, teamLevelEndToken);
      team.setLevel(level);

      String playScheduleBeginToken = "Play Schedule";
      int playScheduleIndexBegin = teamHtmlPage.indexOf(playScheduleBeginToken);
      int playScheduleIndexEnd   = teamHtmlPage.indexOf("</table>", playScheduleIndexBegin); 
      String playScheduleHtml = teamHtmlPage.substring(playScheduleIndexBegin + playScheduleBeginToken.length() + 2, playScheduleIndexEnd);
      String[] matches = playScheduleHtml.split("<tr");
      List<TeamMatch> teamMatches = new ArrayList<TeamMatch>(matches.length-1);
      
      int matchesWon = 0;
      int matchesPlayed = 0;
      int setsWon = 0;
      int setsPlayed = 0;

      for(int j = 2; j < matches.length; j++){
        String matchHtml = matches[j];
        String[] matchCells = matchHtml.split("<td>"); 

        TeamMatch teamMatch = new TeamMatch();
        teamMatch.setMatchNumber(cleanup(matchCells[1]));
        teamMatch.setBracket(cleanup(matchCells[2]));
        teamMatch.setOpponent(cleanup(matchCells[3]));
        teamMatch.setSchedule(cleanup(matchCells[4]));
        teamMatch.setResult(cleanup(matchCells[5]));
        teamMatch.setScore(cleanup(matchCells[6]));
        teamMatch.setTeam(team);
        
        if(teamMatch.getResult().endsWith("Won")){
          int sets1 = 0;
          int sets2 = 0;
          try {
            sets1 = Integer.parseInt(teamMatch.getScore().substring(0, 1));
            sets2 = Integer.parseInt(teamMatch.getScore().substring(2, 3));
          } catch(Exception e){
            e.printStackTrace();
          }
          
          if(teamMatch.getResult().startsWith(team.getTeamName())){
            matchesWon++;
            setsWon = setsWon + sets1;
          } else {
            setsWon = setsWon + sets2;
          }
          matchesPlayed++;
          setsPlayed = setsPlayed + sets1 + sets2;
        }
        teamMatches.add(teamMatch);
      }
      team.setTeamMatches(teamMatches);
      team.setMatchesWon(matchesWon);
      team.setMatchesPlayed(matchesPlayed);
      team.setGamesWon(setsWon);
      team.setGamesPlayed(setsPlayed);
      return team;
    }
    
    private String getString(String page, String beginToken, String endToken){
      int indexBegin = page.indexOf(beginToken);
      int indexEnd   = page.indexOf(endToken, indexBegin);
      if(indexBegin < 0 || indexEnd < 0){
        throw new RuntimeException("Could not find markers");
      }
      return page.substring(indexBegin + beginToken.length(), indexEnd); 
    }

    private String cleanup(String originalString){
      int closeTDIndex = originalString.indexOf("</td>");  
      if(closeTDIndex > 0){
        originalString = originalString.substring(0, closeTDIndex);
      }

      int hrefIndex = originalString.indexOf("<a");
      if(hrefIndex > 0){
        int hrefOpenIndex = originalString.indexOf(">") + 1;
        int hrefCloseIndex = originalString.indexOf("</a>");
        originalString = originalString.substring(hrefOpenIndex, hrefCloseIndex); 
      }
      return originalString.trim();
    } 
    
}

