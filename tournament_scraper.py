import json
import time
from team_scraper import TeamScraper

class TournamentScraper:

    def get_tournament_json(self):
        with open("tournament.json", encoding="utf-8") as f:
            tournament_data = f.read()
            return json.loads(tournament_data)

    def get_all_team_matches(self):
        team_scraper = TeamScraper()
        teams = []
        tournament_json = self.get_tournament_json()
        event_id = tournament_json["event_id"]
        clubs_json = tournament_json["clubs"]
        for club_id in clubs_json:
            club_info = team_scraper.get_club_info(event_id, club_id)
            club_info_values = club_info["value"]
            for club_info_value in club_info_values:
                team_id = club_info_value["TeamId"]
                division = club_info_value["Division"]
                division_id = division["DivisionId"]
                team_matches = team_scraper.get_all_team_matches(event_id=event_id, division_id=division_id, team_id=team_id)

                team_dict = {}
                team_dict["event_id"] = event_id
                team_dict["club_id"] = club_id
                team_dict["team_id"] = team_id
                team_dict["division_id"] = division_id
                team_dict["team_name"] = club_info_value["TeamName"]
                team_dict["matches_won"] = club_info_value["MatchesWon"]
                team_dict["matches_lost"] = club_info_value["MatchesLost"]
                team_dict["sets_won"] = club_info_value["SetsWon"]
                team_dict["sets_lost"] = club_info_value["SetsLost"]
                team_dict["finish_text"] = club_info_value["FinishRankText"]
                team_dict["division_name"] = division["Name"]
                team_dict["matches"] = team_matches
                teams.append(team_dict) 

                time.sleep(0.25) # Sleep so we don't kill the aes website

        return teams 

#tournament_scraper = TournamentScraper()
#tournament_scraper.get_all_tournament_matches()
