import json
import time
from team_scraper import TeamScraper

class TournamentScraper:

    def get_tournament_json(self):
        with open("tournament.json", encoding="utf-8") as f:
            tournament_data = f.read()
            return json.loads(tournament_data)

    def get_all_tournament_matches(self):
        team_scraper = TeamScraper()
        matches = []
        tournament_json = self.get_tournament_json()
        event_id = tournament_json["event_id"]
        teams_json = tournament_json["teams"]
        for team_json in teams_json:
            division_id = team_json["division_id"]
            team_id     = team_json["team_id"]
            matches = matches + team_scraper.get_all_team_matches(event_id=event_id, division_id=division_id, team_id=team_id)
            time.sleep(0.25)  

        return matches

#tournament_scraper = TournamentScraper()
#tournament_scraper.get_all_tournament_matches()
