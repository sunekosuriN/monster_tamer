package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BattleContext {

	private BattleParty playerParty = new BattleParty();
	private BattleParty enemyParty = new BattleParty();

	private int currentFloor = 1;
	private int turnCount = 1;
	private List<String> battleLogs = new ArrayList<>();

	private boolean isBattleOver = false;
	private boolean isVictory = false;
	private boolean isTamed = false;

	public void addLog(String message) {
		this.battleLogs.add(message);
	}

}
