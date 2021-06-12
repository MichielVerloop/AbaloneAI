mkdir "speed/standard/cmo+tt" 
for /l %%i in (0, 1, 29) do (
  java -jar AbaloneAI.jar -p players.json -i "speed_standard_input_match.json" -l STANDARD2 -s "speed/standard/cmo+tt/cmo+tt_%%i.csv"
)
pause
