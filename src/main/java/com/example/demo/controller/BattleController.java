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
 * JavaScript(Ajax)からのリクエストを受け付け、JSONデータを返します。
 */
@RestController
@RequestMapping("/battle")
public class BattleController {

    @Autowired
    private BattleService battleService;

    /**
     * 戦闘開始処理
     * URL: POST /battle/start
     */
    @PostMapping("/start")
    public BattleContext startBattle() {
        return battleService.startBattle();
    }

    /**
     * ターンの実行（プレイヤーの行動）
     * URL: POST /battle/turn
     * * ★修正点: targetIndexを受け取れるように引数を追加しました。
     * required = false にすることで、ターゲット指定がない場合（null）も許容します。
     */
    @PostMapping("/turn")
    public BattleContext executeTurn(
            @RequestParam("skillId") int skillId,
            @RequestParam(value = "targetIndex", required = false) Integer targetIndex) {
        
        // Serviceのメソッドシグネチャに合わせて引数を2つ渡します
        return battleService.processTurn(skillId, targetIndex);
    }

    /**
     * 敵を仲間にする処理
     * URL: POST /battle/recruit
     */
    @PostMapping("/recruit")
    public BattleContext recruitEnemy(
            @RequestParam(value = "swapIndex", required = false) Integer swapIndex) {
        // swapIndex: パーティがいっぱいの場合、入れ替える味方のインデックス
        return battleService.recruitEnemy(swapIndex);
    }

    /**
     * 戦闘終了処理（勝利時の階層進行など）
     * URL: POST /battle/finish
     * @return true: 次の階層へ進む, false: ゲームオーバーまたはエラー
     */
    @PostMapping("/finish")
    public boolean finishBattle() {
        return battleService.finishBattle();
    }
}