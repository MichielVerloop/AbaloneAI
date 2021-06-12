mkdir "first-player bias/belgian daisy" 
for /l %%i in (1, 1, 29) do (
  java -jar AbaloneAI.jar -p players.json -o "first-player bias/belgian daisy/fp_bias_%%i.json" -s "first-player bias/belgian daisy/fp_bias_%%i.csv" -l BELGIAN_DAISY
)
pause
