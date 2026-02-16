package com.example.demo.model;

import java.io.Serializable;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import lombok.Data;

/**
 * プレイヤーの冒険全体の状態を管理するクラス。
 * @SessionScope により、ブラウザ（ユーザー）ごとに1つのインスタンスが自動生成・保持されます。
 * メッセージ生成ロジックを持たず、純粋なデータの保持と操作のみを担当します。
 */
@Component
@SessionScope
@Data
public class GameState implements Serializable {
    
    // セッション保存用のバージョンID
    private static final long serialVersionUID = 1L;
    // 現在の階層
    private int currentFloor = 1;
    // 主人公・仲間モンスターを保持するパーティ
    private BattleParty playerParty = new BattleParty();
    // 強化用ポイント
    private int upgradePoints = 0;

    
    private BattleContext currentBattleContext;
    
    
    /**
     * コンストラクタ
     * 必要に応じて初期化処理を書きます
     */
    public GameState() {
        // 初期状態のセットアップなどが必要ならここに記述
    }

    /**
     * パーティに空きがある場合のみ、新しい仲間を追加する
     * @param newMonster 新しく仲間にするモンスター
     * @return 追加に成功したら true、満員なら false
     */
    public boolean addMonsterIfPossible(BattleMonster newMonster) {
        if (this.playerParty.getParty().size() < 4) { // 主人公含め最大4体
            newMonster.setPlayerSide(true);
            this.playerParty.addParty(newMonster);
            return true;
        }
        return false;
    }

    /**
     * 指定したインデックスのモンスターと新しいモンスターを入れ替える
     * @param indexToRemove 削除するモンスターのインデックス (0は主人公なので不可)
     * @param newMonster 新しく仲間にするモンスター
     * @return 入れ替わりで削除されたモンスター。失敗時（主人公を指定など）は null
     */
    public BattleMonster replaceMonster(int indexToRemove, BattleMonster newMonster) {
        // バリデーション：主人公（0番目）や範囲外の指定は無効
        if (indexToRemove <= 0 || indexToRemove >= this.playerParty.getParty().size()) {
            return null;
        }

        newMonster.setPlayerSide(true);

        // 指定されたモンスターをリストから削除し、戻り値として取得
        BattleMonster removedMonster = this.playerParty.getParty().remove(indexToRemove);
        
        // 同じ位置に新しいモンスターを挿入
        this.playerParty.getParty().add(indexToRemove, newMonster);

        // 呼び出し元（Service）がログを作れるように、消えたモンスターを返す
        return removedMonster;
    }

    /**
     * 次の階層へ進む
     */
    public void advanceFloor() {
        this.currentFloor++;
    }

    /**
     * ゲームオーバー時などのリセット処理
     */
    public void reset() {
        this.currentFloor = 1;
        this.upgradePoints = 0;
        this.playerParty = new BattleParty();
        // 必要なら初期メンバー（主人公）の再設定などを行う
    }
}