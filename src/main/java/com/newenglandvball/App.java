package com.newenglandvball;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

public class App {

  private static final String TIME_FORMAT_WITH_TZ = "MM/dd/yyyy hh:mm aa z";
  private static final String TIME_DATE_FORMAT = "MM/dd/yyyy hh:mm aa";
  private static final String CURRENT_YEAR = (new SimpleDateFormat("yyyy")).format(new Date());

  private final String timezone;
  private final String fileLocation;
  private final int timeBetweenPageGets;
  private final String eventId;
  private final String[] teamIds;
  private final int hoursToShowMatch;

  private App(String timezone, String fileLocation, int timeBetweenPageGets, int hoursToShowMatch, String eventId, String[] teamIds){
    this.timezone = timezone;
    this.fileLocation = fileLocation;
    this.timeBetweenPageGets = timeBetweenPageGets;
    this.hoursToShowMatch = hoursToShowMatch;
    this.eventId = eventId;
    this.teamIds = teamIds;
  }

  private Date getScheduleDate(TeamMatch teamMatch) {
    Date dt = null;
    try {
      //If there is a match that isn't scheduled, set it to the time of the last match this team played
      if("Not Scheduled".equals(teamMatch.getSchedule())){
        for(TeamMatch teamMatchTemp: teamMatch.getTeam().getTeamMatches()){
          if(!"Not Scheduled".equals(teamMatchTemp.getSchedule())){
            Date tempDate = getScheduleDate(teamMatchTemp);
            if(tempDate != null){
              if(dt == null || tempDate.after(dt)){
                dt = tempDate;
              }
            }
          }
        }
      } else {
        String scheduleString = teamMatch.getSchedule();
        String[] scheduleStrings = scheduleString.split("\\s"); 

        String monthDayString = scheduleStrings[2];
        String timeString = scheduleStrings[3];
        String amPmString = scheduleStrings[4];

        String dateString = monthDayString + "/" + CURRENT_YEAR + " " + timeString + " " + amPmString;
        DateFormat dateFormat = new SimpleDateFormat(TIME_DATE_FORMAT);
   
        dt = dateFormat.parse(dateString);
      }

    } catch (Exception e){
      e.printStackTrace();
    }
    return dt;
  }

  private Comparator<TeamMatch> dateComparator = new Comparator<TeamMatch>(){
    public int compare(TeamMatch match1, TeamMatch match2){
      Date match1Date = getScheduleDate(match1); 
      Date match2Date = getScheduleDate(match2); 

      if(match1Date == null && match2Date == null){
        return 0;
      }

      if(match1Date == null){
        return 1;
      }

      if(match2Date == null){
        return 2;
      }

      return match1Date.compareTo(match2Date);
    }
  };

  public static void main(String[] args) throws Exception {
    if(args.length != 1){
      System.out.println("Usage: java com.newenglandvball.App /path/to/propertyfile");
    }

    Properties prop = new Properties();

    try {
      prop.load(new FileInputStream(args[0]));

      String timezone = prop.getProperty("timezone");
      System.setProperty("user.timezone", timezone);

      String fileLocation = prop.getProperty("file.location");
      int timeBetweenPageGets = Integer.parseInt(prop.getProperty("time.between.page.gets"));
      String eventId = prop.getProperty("event.id");
      String teamIdString = prop.getProperty("team.ids");
      int hoursToShowMatch = Integer.parseInt(prop.getProperty("hours.to.show.match"));
      String[] teamIds = teamIdString.split(",");

      App app = new App(timezone, fileLocation, timeBetweenPageGets, hoursToShowMatch, eventId, teamIds);
      app.run();  
      
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    
    
  }

  public void run() throws Exception{
    BufferedWriter out = null;
    try {
      String updatedPage = this.getUpdatedPage();
      DateFormat dateFormat = new SimpleDateFormat(TIME_DATE_FORMAT);
      FileWriter fstream = new FileWriter(this.getFileLocation());
      out = new BufferedWriter(fstream);
      out.write(updatedPage);
      System.out.println("Updated page at " + dateFormat.format(new Date()));
    } catch(Exception e){
      e.printStackTrace();
    } finally {
      if(out != null){
        out.close();
      }
    }
  }

  public String getUpdatedPage() throws Exception{
    Parser parser = new Parser(this.getTeamIds(), this.getTimeBetweenPageGets(), this.getEventId());
    List<Team> teams = parser.getTeams();

    List<TeamMatch> allTeamMatches = new ArrayList<TeamMatch>();
    for(Team team: teams){
      allTeamMatches.addAll(team.getTeamMatches()); 
    }

    Collections.sort(allTeamMatches, dateComparator);

    StringBuilder outputHtml = new StringBuilder();
    outputHtml.append("<html>");
    outputHtml.append("<meta http-equiv=\"Pragma\" content=\"no-cache\">");
    outputHtml.append("<meta http-equiv=\"expires\" content=\"0\">");
    outputHtml.append("<body style=\"font-family: Verdana,Arial,Helvetica,sans-serif;\"><table cellpadding=\"1\" cellspacing=\"0\" style=\"overflow-x: scroll; white-space: nowrap; \">");
    outputHtml.append("<tr align=\"center\"><td><b>Team Name</b></td>");
    outputHtml.append("<td><b>Record</b></td>");
    outputHtml.append("<td><b>Schedule</b></td>");
    outputHtml.append("<td><b>Level</b></td>");
    outputHtml.append("<td><b>Opponent</b></td>");
    outputHtml.append("<td><b>Bracket</b></td>");
    outputHtml.append("<td><b>Result</b></td></tr>");

    int teamIndex = 0;
    for(TeamMatch teamMatch: allTeamMatches){
      Date matchDate = getScheduleDate(teamMatch); 
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.HOUR_OF_DAY, -hoursToShowMatch);
      Date timeToDisplay = cal.getTime();
      if(matchDate == null || matchDate.after(timeToDisplay)){
        String bgcolor = "FFFFFF";
        if(teamIndex%2 == 0){
          bgcolor="DFDFDF";
        }
        outputHtml.append("<tr bgcolor=\"" + bgcolor + "\"><td><a href=\"https://advancedeventsystems.com/EventResults/(S(vwnoq02zvf1v2h45ubsui4ec))/TeamSchedule.aspx?e=" + this.getEventId() + "&t=" + teamMatch.getTeam().getTeamNumber() + "\">" + teamMatch.getTeam().getTeamName() + "</a></td>");
        outputHtml.append("<td>" + teamMatch.getTeam().getMatchesWon() + "-" + (teamMatch.getTeam().getMatchesPlayed()-teamMatch.getTeam().getMatchesWon()) + " " + teamMatch.getTeam().getGamesWon() + "-" + (teamMatch.getTeam().getGamesPlayed() - teamMatch.getTeam().getGamesWon()) + "</td>");
        outputHtml.append("<td style=\"padding-left:5px\">" + teamMatch.getSchedule() + "</td>");
        outputHtml.append("<td>" + teamMatch.getTeam().getLevel() + "</td>");
        outputHtml.append("<td>" + teamMatch.getOpponent() + "</td>");
        outputHtml.append("<td>" + teamMatch.getBracket() + "</td>");
        outputHtml.append("<td>" + teamMatch.getResult() + "</td></tr>");
        teamIndex++;
      }
    }

    DateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT_WITH_TZ);
    dateFormat.setTimeZone(TimeZone.getTimeZone(this.getTimezone()));
    outputHtml.append("</table>");
    outputHtml.append("<br />uno, dos, tres, nacho<br />");
    outputHtml.append("<br />Last updated at: " + dateFormat.format(new Date()));
    outputHtml.append("</body></html>");
    return outputHtml.toString();
  }

  protected String getTimezone() {
    return timezone;
  }

  protected String getFileLocation() {
    return fileLocation;
  }

  protected int getTimeBetweenPageGets() {
    return timeBetweenPageGets;
  }

  protected String getEventId() {
    return eventId;
  }

  protected String[] getTeamIds() {
    return teamIds;
  }

}

