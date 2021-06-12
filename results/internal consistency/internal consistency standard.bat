mkdir "internal consistency/standard"
for /l %%i in (0, 1, 29) do (
  java -jar AbaloneAI.jar -p players.json -i "int_const_standard_input_match.json" -l STANDARD2 -s "internal consistency/standard/int_const_%%i.csv"
)
pause
