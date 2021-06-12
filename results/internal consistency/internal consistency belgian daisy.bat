mkdir "internal consistency/belgian daisy" 
for /l %%i in (0, 1, 29) do (
  java -jar AbaloneAI.jar -p players.json -i "int_const_b_daisy_input_match.json" -l BELGIAN_DAISY -s "internal consistency/belgian daisy/int_const_%%i.csv"
)
pause
