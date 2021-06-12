# Abalone AI

A reimplementation of the 2012 paper "Exploring optimization strategies in board game Abalone for Alpha-Beta search" by Athanasios Papadopoulos, Konstantinos Toumpas, Antonios Chrysopoulos and Pericles A. Mitkas.  
This project was made as part of my Bachelor Thesis.

# Table of Contents  
1. [Features](#features)  
2. [Limitations](#limitations)
3. [Requirements](#requirements)
4. [Usage](#usage)
5. [Player json files](#player-json-files)

# Features
* Play Abalone on a primitive Textual User Interface
* Support for 2-4 player games
* Has an alpha-beta agent with  
    * (Advanced) combined move ordering  
    * Transposition Tables
    * Support for depth/time-bound iterative deepening depth-first search
    * Customizable weights for the evaluation function
    * Feature toggles for all heuristics
* Outputs csv files containing the statistics of the game
* Outputs the game

# Limitations
* Does not support quiescence search
* Does not support (non-linear) aspiration windows

# Requirements
The code was made to run on JDK 11. Other versions may be supported, but are untested.

# Usage

Usage: java -jar AbaloneAI.jar [-hV] [-i=\<input>] [-l=\<layout>] [-o=\<output>] [-p=\<players>] [-s=\<stats>] 
* Launches Abalone.

Options:  
short arg | long arg              | description
--------- | --------------------- | -----------
-h        | --help                | Show this message and exit.
-V        | --version             | Show the version and exit.
-i        | --input GAME_FILE     | The inputs game file of which the moves are used instead of the ones from the player. Can be thought of as a replay functionality.
-l        | --layout LAYOUT       | BELGIAN_DAISY, or empty for the STANDARD layout.
-o        | --output GAME_FILE    | The output game file where the moves that are made in the game will be stored.
-p        | --players PLAYER_FILE | The player file where the behaviour of the players are defined.
-s        | --stats STATS_FILE    | The stats file where all statistics of the game will be stored.

When no player file is given, the program will guide the user through the creation of the players on the console, after which the game can be played as normal.  

# Player json files
Two example json files are given to show how they should be constructed and which effects the settings have.  
The first json file results in a 2-player game. The first player will be the minimax player, named Player. The second player will be a replay player, named Replay.  
Because there is a replay player, an input file is required. With an input file, Player will compute its move, then ignore it and instead make the move from the game that is being replayed. Replayer will instantly make the move from the game that is being replayed. Such a setup is used to compare speed between different heuristics.

Enabling the abaPro leads to the use of the evaluation function from aba-pro AI and disables the other evaluator metrics. While the metric is disabled, the other metrics are active.  
Out of the dfs, depthBoundIddfs and timeBoundIddfs options, only one should be set to true. 
For the sorting and ordering heuristics, a depth can be specified at which they are activated, provided they are enabled with the boolean. Iteration sorting can be active on the same depths as the history heuristic and marble ordering. All other combinations are undefined.
```
[
  {
    "@class":"computer",
    "name":"Player",
    "strategy":{
      "@class":"minimax",
      "evaluator":{
        "@class":"evaluator",
        "abaPro":false,
        "considerEnemyPosition":true,
        "coherenceWeight":3,
        "distanceFromCenterWeight":8,
        "formationBreakWeight":10,
        "marblesConqueredWeight":800,
        "immediateMarbleCapWeight":0,
        "singleMarbleCapWeight":30,
        "doubleMarbleCapWeight":50
      },
      "miniBuilder":{
        "@class":"builder",
        "dfs":true,
        "depthBoundIddfs":false,
        "depth":3,
        "timeBoundIddfs":false,
        "time":15,
        "hashing":false,
        "windowNarrowing":false,
        "evaluateSorting":true,
        "evaluateSortingMinDepth":1,
        "evaluateSortingMaxDepth":2,
        "historyHeuristicSorting":true,
        "historyHeuristicSortingMinDepth":3,
        "historyHeuristicSortingMaxDepth":4,
        "marbleOrdering":true,
        "marbleOrderingMinDepth":5,
        "marbleOrderingMaxDepth":5,
        "iterationSorting":false,
        "iterationSortingMinDepth":3,
        "iterationSortingMaxDepth":5
      }
    }
  },
  {
    "@class":"replay",
    "name":"Replayer"
  }
]
```

The second json file results in a 3-player game. The game has a human player and two computer players that employ the random and aggressive strategy.

```
[
  {
    "@class":"human",
    "name":"human player"
  },
  {
    "@class":"computer",
    "name":"random AI",
    "strategy":{
      "@class":"random",
    }
  },
  {
    "@class":"computer",
    "name":"Primitive aggressive AI",
    "strategy":{
      "@class":"aggressive",
    }
  }
]
```
