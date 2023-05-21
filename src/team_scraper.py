import requests
import json

class TeamScraper:

    def get_all_team_matches(self, event_id, division_id, team_id):
        past_matches = self.get_team_past_matches(event_id, division_id, team_id)
        current_matches = self.get_team_current_matches(event_id, division_id, team_id)
        return past_matches + current_matches

    def get_team_past_matches(self, event_id, division_id, team_id):
        past_matches = []
        past_url = f"https://results.advancedeventsystems.com/api/event/{event_id}/division/{division_id}/team/{team_id}/schedule/past"
        past_data = requests.get(past_url)
        past_json = json.loads(past_data.text)
        for play_and_match_data in past_json:
            match_json = play_and_match_data["Match"]
            match_info = self.parse_team_match(team_id, match_json)
            past_matches.append(match_info)
        return past_matches

    def get_team_current_matches(self, event_id, division_id, team_id):
        current_matches = []
        current_url = f"https://results.advancedeventsystems.com/api/event/{event_id}/division/{division_id}/team/{team_id}/schedule/current"
        current_data = requests.get(current_url)
        current_json = json.loads(current_data.text)
        
        # There should only be one team since we requested a team page
        for team_current in current_json:
            for match_json in team_current["Matches"]:
                match_info = self.parse_team_match(team_id, match_json)
                current_matches.append(match_info)
        return current_matches

    def parse_team_match(self, team_id, match_json):
        team_id1   = match_json["FirstTeamId"]
        team_name1 = match_json["FirstTeamName"]
    
        team_id2   = match_json["SecondTeamId"]
        team_name2 = match_json["SecondTeamName"]
        assert team_id == team_id1 or team_id == team_id2, f"Requested team id {team_id} but they aren't playing in the match"
        winning_team_name = ""
        if match_json["FirstTeamWon"]:
            winning_team_name = team_name1
        elif match_json["SecondTeamWon"]:
            winning_team_name = team_name2
   
        if(team_id1 == team_id):
            current_team_name = team_name1
            opponent_team_name = team_name2
        else:
            current_team_name = team_name2
            opponent_team_name = team_name1
  
        scheduled_start_time = match_json["ScheduledStartDateTime"]
        match_name = match_json["MatchFullName"]
        court = match_json["Court"]
        court_name = court["Name"]
        court_video_link = court["VideoLink"]
        scores = []
        if match_json["HasScores"]:
            sets = match_json["Sets"]
            for match_set in sets:
                if(team_id1 == team_id):
                    current_team_score  = match_set["FirstTeamScore"]
                    opponent_team_score = match_set["SecondTeamScore"]
                else:
                    current_team_score  = match_set["SecondTeamScore"]
                    opponent_team_score = match_set["FirstTeamScore"]

                if current_team_score is not None:
                    scores.append(f"{current_team_score}-{opponent_team_score}")

        score_text = ",".join(scores) 

        match_info = {}
        match_info["current_team"] = current_team_name
        match_info["opponent"]     = opponent_team_name
        match_info["start_time"]   = scheduled_start_time
        match_info["court"]        = court_name
        match_info["match_name"]   = match_name
        match_info["video_link"]   = court_video_link
        match_info["winning_team"] = winning_team_name
        match_info["scores"]       = score_text

        return match_info

#event_id = "PTAwMDAwMzE2NDI90"
#division_id = 143619
#team_id = 175435
#team_scraper = TeamScraper()
#all_matches = team_scraper.get_all_team_matches(event_id=event_id, division_id=division_id, team_id=team_id)
#
#print(f"All matches for team {team_id}: " + str(all_matches))
