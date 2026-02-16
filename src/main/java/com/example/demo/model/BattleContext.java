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

    // JavaScriptの currentContext.logs と名前を一致させる
    private List<String> logs = new ArrayList<>();

    private boolean isBattleOver = false;
    private boolean isVictory = false;
    private boolean isTamed = false;

    /**
     * ログを追加する
     */
    public void addLog(String message) {
        if (message != null && !message.isEmpty()) {
            this.logs.add(message);
        }
    }

    /**
     * ターン開始時に古いログを消去する
     * これにより、フロントエンドには「そのターンに起きたこと」だけが送られます
     */
    public void clearLogs() {
        this.logs.clear();
    }
}