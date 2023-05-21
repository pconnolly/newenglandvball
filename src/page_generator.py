from datetime import datetime, timedelta
import operator
from tournament_scraper import TournamentScraper

class PageGenerator:

    def generate_current_matches(self):
        cutoff_time = datetime.now() - timedelta(hours = 29)
        cutoff_time_iso = cutoff_time.replace(microsecond=0).isoformat()
        #print("Cutoff time: " + cutoff_time_iso)
        tournament_scraper = TournamentScraper()
        tournament_matches = tournament_scraper.get_all_tournament_matches()
        filtered_tournament_matches = [filtered_match for filtered_match in tournament_matches if filtered_match['start_time'] >= cutoff_time_iso]
        filtered_tournament_matches.sort(key=operator.itemgetter('start_time')) 
        #print("Sorted matches: " + str(filtered_tournament_matches))
        return filtered_tournament_matches

    def create_html(self, current_matches, file_name):
        output_html = "<html>"
        output_html += "<meta http-equiv=\"Pragma\" content=\"no-cache\">"
        output_html += "<meta http-equiv=\"expires\" content=\"0\">"
        output_html += "<body style=\"font-family: Verdana,Arial,Helvetica,sans-serif;\"><table cellpadding=\"1\" cellspacing=\"0\" style=\"overflow-x: scroll; white-space: nowrap; \">"
        output_html += "<tr align=\"center\">"
        output_html += "<td><b>Team Name</b></td>"
        output_html += "<td><b>Schedule</b></td>"
        output_html += "<td><b>Court</b></td>"
        output_html += "<td><b>Level</b></td>"
        output_html += "<td><b>Opponent</b></td>"
        output_html += "<td><b>Winning Team</b></td>"
        output_html += "<td><b>Scores</b></td>"
        output_html += "</tr>"

        bgcolor = "FFFFFF"
        team_number = "blah"
        event_id = "blah"
        match_level = "BB"
        for current_match in current_matches:
            output_html += "<tr bgcolor=\"" + bgcolor + "\">"
            output_html += "<td><a href=\"https://advancedeventsystems.com/EventResults/(S(vwnoq02zvf1v2h45ubsui4ec))/TeamSchedule.aspx?e=" + event_id + "&t=" + team_number + "\">" + current_match["current_team"] + "</a></td>"
            output_html += "<td style=\"padding-left:5px\">" + current_match["start_time"] + "</td>"
            output_html += "<td style=\"padding-left:5px\">" + current_match["court"] + "</td>"
            output_html += "<td>" + match_level + "</td>"
            output_html += "<td>" + current_match["opponent"] + "</td>"
            output_html += "<td>" + current_match["winning_team"] + "</td>"
            output_html += "<td>" + current_match["scores"] + "</td>"
            output_html += "</tr>"

        current_time = datetime.now().replace(microsecond=0).isoformat()
        output_html += "</table>"
        output_html += "<br />uno, dos, tres, nacho<br />"
        output_html += "<br />Last updated at: " + current_time
        output_html += "</body></html>"
        return output_html



file_name = "./matches.html"
page_generator = PageGenerator()
current_matches = page_generator.generate_current_matches()
output_html = page_generator.create_html(current_matches, file_name)
with open(file_name, "w", encoding="utf-8") as f:
    f.write(output_html)
