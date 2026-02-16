package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.BattleContext;
import com.example.demo.service.BattleService;

/**
 * 戦闘画面のAPIコントローラー
 */
@RestController
@RequestMapping("/battle")
public class BattleController {

    @Autowired
    private BattleService battleService;

    /**
     * 戦闘開始処理
     */
    @PostMapping("/start")
    public BattleContext startBattle() {
        return battleService.startBattle();
    }

    /**
     * ターンの実行（プレイヤーの行動）
     * 修正点: JavaScript側の fetch パス "/battle/action" に合わせて修正
     */
    @PostMapping("/action")
    public BattleContext executeAction(
            @RequestParam("skillId") int skillId,
            @RequestParam(value = "targetIndex", required = false) Integer targetIndex) {
        
        // BattleServiceにターン処理を依頼
        return battleService.processTurn(skillId, targetIndex);
    }

    /**
     * 敵を仲間にする処理
     */
    @PostMapping("/recruit")
    public BattleContext recruitEnemy(
            @RequestParam(value = "swapIndex", required = false) Integer swapIndex) {
        return battleService.recruitEnemy(swapIndex);
    }

    /**
     * 戦闘終了処理
     */
    @PostMapping("/finish")
    public boolean finishBattle() {
        return battleService.finishBattle();
    }
}