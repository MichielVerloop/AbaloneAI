	//

package controller;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import model.gamelogic.StartingLayout;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "launcher", mixinStandardHelpOptions = true, version = "v1.0",
		description = "Launches Abalone.")
public class Launcher implements Callable<Integer> {
	
	@Option(names = {"-p", "--players"}, description = "The player file where the behaviour of the players are defined.")
	private File players;
	
	@Option(names = {"-l", "--layout"}, description = "BELGIAN_DAISY, or empty for the STANDARD layout.")
    private StartingLayout layout = null;
	
	@Option(names = {"-i", "--input"}, 
			description = "The inputs game file of which the moves are used instead of the ones from the player. Can be thought of as a replay functionality.")
	private File input;
	
	@Option(names = {"-o", "--output"}, 
			description = "The output game file where the moves that are made in the game will be stored.")
	private File output;
	
	@Option(names = {"-s", "--stats"},
			description = "The stats file where all statistics of the game will be stored.")
	private File stats;
	
	public static void main(String... args) {
		int exitCode = new CommandLine(new Launcher()).execute(args);
        System.exit(exitCode);
	}


	@Override
	public Integer call() throws Exception {
		Abalone.initializeUI();
		if (players == null && input == null) {
			Abalone.startLocalGame(layout);
			return 0;
		}
		Abalone.Builder gameBuilder = new Abalone.Builder()
				.withStartingLayout(layout)
				.withInputFile(input)
				.withOutputFile(output)
				.withStatsFile(stats);
		Abalone game;
		if (players == null && input != null) {
			game = gameBuilder.build();
		} else {
			try {
				game = gameBuilder.withPlayers(players).build();
			} catch (IOException e) {
				System.out.println("Initialization from Json failed: " + e.getMessage() 
					+ "\nInitializing a game from the TUI instead.");
				Abalone.startLocalGame(layout);
				return 0;
			}
		}
		game.playLocal();
		return 0;
	}
}
