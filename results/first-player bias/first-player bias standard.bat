mkdir "first-player bias/standard" 
for /l %%i in (1, 1, 29) do (
  java -jar AbaloneAI.jar -p players.json -o "first-player bias/standard/fp_bias_%%i.json" -s "first-player bias/standard/fp_bias_%%i.csv"
)
pause
