package org.ggp.base.player.gamer.statemachine;

import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.exception.GameAnalysisException;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

public class InfiniteTimeGamer extends StateMachineGamer {
	private final int MAX_STATE_VALUE = 100;

	@Override
	public StateMachine getInitialStateMachine() {
		return new CachedStateMachine(new ProverStateMachine());
	}

	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException {

	}

	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException {
		StateMachine stateMachine = getStateMachine();
		List<Move> legalMoves = stateMachine.getLegalMoves(getCurrentState(), getRole());
		Map<Move, List<MachineState>> nextStates = stateMachine.getNextStates(getCurrentState(), getRole());
		
		Move bestMove = null;
		int bestValue = MAX_STATE_VALUE;
		
		while(System.currentTimeMillis() < timeout) {
			for (Move nextMove : legalMoves) {
				for (MachineState state : nextStates.get(nextMove)) {
					int nextValue = getStateValue(state);
					
					if (nextValue < bestValue) {
						bestValue = nextValue;
						bestMove = nextMove;
					}
				}
			}
		}
		
		return bestMove;
	}

	@Override
	public void stateMachineStop() {

	}

	@Override
	public void stateMachineAbort() {

	}

	@Override
	public void analyze(Game g, long timeout) throws GameAnalysisException {

	}

	@Override
	public String getName() {
		return "InfiniteTime";
	}
	
	/*
	 * Returns lowest value of states which succeed currentState or -1
	 * if an error is thrown
	 */
	private int getStateValue(MachineState currentState) {
		StateMachine stateMachine = getStateMachine();
		Role currentRole = getRole();
		
		if (stateMachine.isTerminal(currentState)) {
			try {
				return stateMachine.getGoal(currentState, currentRole);
			} catch (GoalDefinitionException e) {
				System.err.println("Error: Goal definition exception");
				e.printStackTrace();
				return -1;
			}
		}
		
		Map<Move, List<MachineState>> nextStates;
		List<Move> legalMoves;
		
		try {
			nextStates = stateMachine.getNextStates(currentState, currentRole);
			legalMoves = stateMachine.getLegalMoves(currentState, currentRole);
		} catch (MoveDefinitionException e) {
			System.err.println("Error: Move definition exception");
			e.printStackTrace();
			return -1;
		} catch (TransitionDefinitionException e) {
			System.err.println("Error: Transition definition exception");
			e.printStackTrace();
			return -1;
		}
		
		int minValue = MAX_STATE_VALUE;
		
		for (Move move : legalMoves) {
			for (MachineState nextState : nextStates.get(move)) {
				int nextStateValue = getStateValue(nextState);
				if (nextStateValue < minValue) {
					minValue = nextStateValue;
				}
			}
		}	
		return minValue > 0 ? minValue : 0;
	}
	
}
