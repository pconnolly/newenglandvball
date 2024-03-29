import boto3
import json
import pytz
from datetime import datetime, timedelta
import operator
from tournament_scraper import TournamentScraper

class PageGenerator:

    def get_tournament_json(self):
        with open("tournament.json", encoding="utf-8") as f:
            tournament_data = f.read()
            return json.loads(tournament_data)

    def generate_current_matches(self):
        cutoff_time = datetime.now() - timedelta(hours = 8)
        cutoff_time_iso = cutoff_time.replace(microsecond=0).isoformat()
        #print("Cutoff time: " + cutoff_time_iso)
        tournament_scraper = TournamentScraper()
        tournament_teams = tournament_scraper.get_all_team_matches()
       
        # We have extra info in the team data structure, let's simplify it for use in a table of match info
        tournament_matches = []
        for team in tournament_teams:
            for team_match in team["matches"]: 
                display_match = {}
                display_match["event_id"]        = team["event_id"]
                display_match["club_id"]         = team["club_id"]
                display_match["team_id"]         = team["team_id"]
                display_match["team_name"]       = team["team_name"]
                display_match["record"]          = str(team["matches_won"]) + "-" + str(team["matches_lost"])
                display_match["division_name"]   = team["division_name"]
                display_match["opponent"]        = team_match["opponent"]
                display_match["start_time"]      = team_match["start_time"]
                display_match["court"]           = team_match["court"]
                display_match["bracket"]         = team_match["bracket"]
                display_match["video_link"]      = team_match["video_link"]
                display_match["winning_team"]    = team_match["winning_team"]
                display_match["scores"]          = team_match["scores"]
                tournament_matches.append(display_match)
        
        # We don't necessarily want to show every match since they may be old. Filter to upcoming matches
        filtered_tournament_matches = [filtered_match for filtered_match in tournament_matches if filtered_match['start_time'] >= cutoff_time_iso]
        filtered_tournament_matches.sort(key=operator.itemgetter('start_time', 'team_name')) 
        #print("Sorted matches: " + str(filtered_tournament_matches))
        return filtered_tournament_matches

    def create_html(self, current_matches, file_name):
        tournament_json = self.get_tournament_json()
        tournament_timezone = tournament_json["timezone"]

        output_html = "<html>"
        output_html += "<meta http-equiv=\"Pragma\" content=\"no-cache\">"
        output_html += "<meta http-equiv=\"expires\" content=\"0\">"
        output_html += "<body style=\"font-family: Verdana,Arial,Helvetica,sans-serif;\"><table cellpadding=\"1\" cellspacing=\"0\" style=\"overflow-x: scroll; white-space: nowrap; border: 1px solid #ccc;\">"
        output_html += "<tr align=\"center\">"
        output_html += "<td style=\"border: 1px solid #333333;\"><b>Team Name</b></td>"
        output_html += "<td style=\"border: 1px solid #333333;\"><b>Record</b></td>"
        output_html += "<td style=\"border: 1px solid #333333;\"><b>Start Time</b></td>"
        output_html += "<td style=\"border: 1px solid #333333;\"><b>Court</b></td>"
        output_html += "<td style=\"border: 1px solid #333333;\"><b>Division Name</b></td>"
        output_html += "<td style=\"border: 1px solid #333333;\"><b>Bracket</b></td>"
        output_html += "<td style=\"border: 1px solid #333333;\"><b>Opponent</b></td>"
        output_html += "<td style=\"border: 1px solid #333333;\"><b>Winning Team</b></td>"
        output_html += "<td style=\"border: 1px solid #333333;\"><b>Scores</b></td>"
        output_html += "</tr>"

        match_index = 0
        for current_match in current_matches:
            start_time_formatted = datetime.strptime(current_match["start_time"], "%Y-%m-%dT%H:%M:%S").strftime("%m-%d %I:%M%p")
            display_opponent = '' if current_match['opponent'] is None else current_match['opponent']
            if (match_index % 2) == 0:
                bg_color = "DFDFDF"
            else: 
                bg_color = "FFFFFF"

            output_html += "<tr bgcolor=\"" + bg_color + "\">"
            output_html += "<td style=\"border: 1px solid #333333;\"><a href=\"https://results.advancedeventsystems.com/event/" + str(current_match["event_id"]) + "/clubs/" + str(current_match["club_id"]) + "/teams/" + str(current_match["team_id"]) + "\">" + current_match["team_name"] + "</a></td>"
            output_html += "<td style=\"border: 1px solid #333333;\">" + current_match["record"] + "</td>"
            output_html += "<td style=\"padding-left:5px; border: 1px solid #333333;\">" + start_time_formatted + "</td>"
            if current_match["video_link"] is not None:
                output_html += "<td style=\"padding-left:5px; border: 1px solid #333333;\"><a href=\"" + current_match["video_link"] + "\">" + current_match["court"] + "</td>"
            else:
                output_html += "<td style=\"padding-left:5px; border: 1px solid #333333;\">" + current_match["court"] + "</td>"
            output_html += "<td style=\"padding-left:5px; border: 1px solid #333333;\">" + current_match["division_name"] + "</td>"
            output_html += "<td style=\"padding-left:5px; border: 1px solid #333333;\">" + current_match["bracket"] + "</td>"
            output_html += "<td style=\"padding-left:5px; border: 1px solid #333333;\">" + display_opponent + "</td>"
            output_html += "<td style=\"padding-left:5px; border: 1px solid #333333;\">" + current_match["winning_team"] + "</td>"
            output_html += "<td style=\"padding-left:5px; border: 1px solid #333333;\">" + current_match["scores"] + "</td>"
            output_html += "</tr>"
            match_index += 1

        current_time = datetime.now().replace(microsecond=0).astimezone(pytz.timezone(tournament_timezone)) \
                .strftime("%a %b %d %Y %I:%M:%S%p %Z")
        output_html += "</table>"
        output_html += "<br />uno, dos, tres, nacho<br />"
        output_html += "<br />Last updated at: " + current_time
        output_html += "</body></html>"
        return output_html



bucket = "newenglandvball"
file_name = "matches.html"
page_generator = PageGenerator()
current_matches = page_generator.generate_current_matches()
output_html = page_generator.create_html(current_matches, file_name)
s3_client = boto3.client('s3')
response = s3_client.put_object(Key=file_name, Bucket=bucket, Body=output_html.encode('utf-8'), ContentType='text/html', ACL='public-read', CacheControl='no-cache', Expires='0')
