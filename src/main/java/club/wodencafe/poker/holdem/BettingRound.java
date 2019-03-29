package club.wodencafe.poker.holdem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.google.common.util.concurrent.AbstractScheduledService;
import club.wodencafe.data.Player;

/**
 * This class should be instantiated for each round of betting,
 * and will handle the commands related to betting.
 * 
 * @author wodencafe
 *
 */
public class BettingRound {
	
	private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	
	private List<PlayerRoundData> players;
	
	private PotManager potManager = new PotManager(players);
	
	private List<Command> previousCommands = new ArrayList<>();
	
	public BettingRound(RoundMediator roundMediator) {
		this.players = roundMediator.getPlayers();
	}
	
	private List<Command> getPreviousCommandsWithoutFolds() {
		List<Command> previousCommandsWithoutFolds = previousCommands.stream()
				.filter(x -> x.getCommandType() != CommandType.FOLD)
				.collect(Collectors.toList());
		return previousCommandsWithoutFolds;
	}
	
	private Player getCurrentPlayer() {

		List<Command> previousCommandsWithoutFolds = getPreviousCommandsWithoutFolds();
		
		List<Player> playersWithoutFolds = players.stream().filter(x -> !x.isFolded()).map(x -> x.get()).collect(Collectors.toList());
		
		if (previousCommandsWithoutFolds.isEmpty()) {
			return players.iterator().next().get();
		}
		else {
			
			Player previousPlayer = previousCommandsWithoutFolds.get(previousCommandsWithoutFolds.size() - 1).getPlayer();

			Player currentPlayer;
			
			int previousPlayerIndex = playersWithoutFolds.indexOf(previousPlayer);
			
			// If this is the last player in the list
			if (previousPlayerIndex == (playersWithoutFolds.size() - 1)) {
				currentPlayer = playersWithoutFolds.get(0);
			}
			else {
				currentPlayer = playersWithoutFolds.get(previousPlayerIndex + 1);
			}
			
			return currentPlayer;
		}
	}
	
	public void handleCommand(Command command) {
		
		if (Objects.equals(command.getPlayer(), getCurrentPlayer())) {
			CommandType commandType = command.getCommandType();
			
			List<Command> previousCommandsWithoutFolds = getPreviousCommandsWithoutFolds();
			
			if (previousCommandsWithoutFolds.isEmpty()) {
				// Applicable commands are bet and check.
				
				
			}
			else {
				Command previousCommand = previousCommandsWithoutFolds.get(previousCommandsWithoutFolds.size() - 1);
				
				CommandType previousCommandType = previousCommand.getCommandType();
	
				switch (previousCommandType) {
					case CHECK:
					{
						// Applicable commands are Check, Bet, (Fold)
						if (commandType == CommandType.CHECK) {
							check(command.getPlayer());
						}
						else if (commandType == CommandType.BET) {
							bet(command.getPlayer(), command.getData().get());
						}
					}
					break;
					case RAISE:
					case BET:
					{
						// Applicable commands are Call, Raise, (Fold)
						if (commandType == CommandType.CALL) {
							call(command.getPlayer());
						}
						else if (commandType == CommandType.RAISE) {
							raise(command.getPlayer(), command.getData().get());
						}
					}
					break;
					case CALL:
					{
						if (commandType == CommandType.CALL) {
							call(command.getPlayer());
						}
						else if (commandType == CommandType.RAISE) {
							raise(command.getPlayer(), command.getData().get());
						}
					}
					break;
				}
			}
		}
	}

	private void check(Player player) {
		Command checkCommand = new Command(CommandType.CHECK, Optional.empty(), player);
		
		previousCommands.add(checkCommand);
	}
	private void bet(Player player, long amount) {
		Command betCommand = new Command(CommandType.BET, Optional.of(amount), player);
		
		previousCommands.add(betCommand);
	}
	private void call(Player player) {
		Command callCommand = new Command(CommandType.CALL, Optional.empty(), player);
		
		previousCommands.add(callCommand);		
	}
	private void raise(Player player, long amount) {
		Command raiseCommand = new Command(CommandType.RAISE, Optional.of(amount), player);
		
		previousCommands.add(raiseCommand);
	}
	
	@Override
	protected void runOneIteration() throws Exception {

	}

	@Override
	protected Scheduler scheduler() {
		Scheduler scheduler = Scheduler.newFixedRateSchedule(0, 30, TimeUnit.SECONDS);
		return scheduler;
	}

	
}
