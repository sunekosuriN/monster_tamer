package com.example.demo.model;

import com.example.demo.entity.SkillMaster;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BattleSkill {

    private SkillMaster skillMaster;
    
    // 残り回数を管理する変数
    private int remainingUses;

    /**
     * コンストラクタ
     */
    public BattleSkill(SkillMaster master) {
        this.skillMaster = master;
        this.remainingUses = master.getMaxUses();
    }

    /**
     * 使用可能か判定
     * -1 は無限に使用可能（通常攻撃など）
     */
    public boolean canUse() {
        if (this.skillMaster.getMaxUses() == -1) {
            return true;
        }
        return this.remainingUses > 0;
    }

    /**
     * スキル使用時の消費処理
     */
    public void consume() {
        if (this.skillMaster.getMaxUses() != -1 && this.remainingUses > 0) {
            this.remainingUses--;
        }
    }

    /**
     * ★修正箇所: 回復処理
     * 変数名を remainingUses に修正しました。
     * 仲間にした時に呼び出され、回数を最大値に戻します。
     */
    public void recover() {
        this.remainingUses = this.skillMaster.getMaxUses();
    }
}