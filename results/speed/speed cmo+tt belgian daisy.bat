mkdir "speed/belgian daisy/cmo+tt" 
for /l %%i in (0, 1, 29) do (
  java -jar AbaloneAI.jar -p players.json -i "speed_b_daisy_input_match.json" -l BELGIAN_DAISY -s "speed/belgian daisy/cmo+tt/cmo+tt_%%i.csv"
)
pause
