set layout=%1
if "%layout%" == "" (start "" %0 STANDARD2 & start "" %0 BELGIAN_DAISY & exit)
echo %layout%

rem swapped is nothing
set type=performance
mkdir "papadopoulos_vs_abapro/%type%/%layout%" 
for /l %%i in (0, 1, 9) do (
  java -jar AbaloneAI.jar -p players_%type%%swapped%.json  -l %layout% -s "papadopoulos_vs_abapro/%type%/%layout%/%type%%swapped%_%%i.csv"
)

set type=quality
mkdir "papadopoulos_vs_abapro/%type%/%layout%"
for /l %%i in (0, 1, 9) do (
  java -jar AbaloneAI.jar -p players_%type%%swapped%.json  -l %layout% -s "papadopoulos_vs_abapro/%type%/%layout%/%type%%swapped%_%%i.csv"
)

rem rerun the runs with the players swapped.
set swapped=_swapped
set type=performance
for /l %%i in (0, 1, 9) do (
  java -jar AbaloneAI.jar -p players_%type%%swapped%.json  -l %layout% -s "papadopoulos_vs_abapro/%type%/%layout%/%type%%swapped%_%%i.csv"
)

set type=quality
for /l %%i in (0, 1, 9) do (
  java -jar AbaloneAI.jar -p players_%type%%swapped%.json  -l %layout% -s "papadopoulos_vs_abapro/%type%/%layout%/%type%%swapped%_%%i.csv"
)
pause