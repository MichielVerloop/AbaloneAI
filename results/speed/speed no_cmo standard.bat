mkdir "speed/standard/no_cmo" 
for /l %%i in (0, 1, 29) do (
  java -jar AbaloneAI.jar -p players.json -i "speed_standard_input_match.json" -l STANDARD2 -s "speed/standard/no_cmo/no_cmo_%%i.csv"
)
pause
