This code will read data from advancedeventsystems (aes), the website used by usavolleyball for event scheduling and results, and will reformat the data into a readable schedule for the teams specified.  The aes system doesn't provide a way to show multiple teams' schedules concurrently.  Within aes it is very cumbersome to navigate to several dozen team's pages to determine when and where matches will be taking place.  

This program will take data from the aes website and reformat it and output the upcoming schedule as an html file.  It is expected that this html file will be placed in a publicly accessible html folder.

##Prerequisites
Python 3 is installed with libraries: boto3, pytz
AWS CLI has been installed and you have a valid credentials file

##How to run
First, modify tournament.json accordingly to update the event id from the AES website in addition to a listing of each club id from that tournament

Then run the script
python page_generator.py

Typically this command would be run on a cron job every 15 minutes such as:
> */15 * * * * cd /home/user/newenglandvball/; python page_generator.py

