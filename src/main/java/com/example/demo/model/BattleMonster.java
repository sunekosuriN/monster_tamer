package com.example.demo.model;

import java.util.List;

import com.example.demo.entity.MonsterMaster;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BattleMonster {

    // 基本情報
    private int id;
    private String name;

    // HP (現在値 / 最大値)
    private int maxHp;
    private int currentHp;

    // アーマー (現在値 / 最大値)
    private int maxArmor;
    private int currentArmor;

    // ステータス (バフ解除用にBase値を保持)
    private int baseAttack;
    private int currentAttack;

    private int baseSpeed;
    private int currentSpeed;

    // スキルと画像
    private List<BattleSkill> skills;
    private String imageUrl;
    
    // 味方フラグ
    private boolean isPlayerSide;

    /**
     * コンストラクタ
     */
    public BattleMonster(MonsterMaster master, List<BattleSkill> skills, boolean isPlayerSide) {
        this.id = master.getId();
        this.name = master.getName();
        
        this.maxHp = master.getHp();
        this.currentHp = master.getHp();
        this.maxArmor = master.getArmor();
        this.currentArmor = master.getArmor();
        
        this.baseAttack = master.getAttack();
        this.currentAttack = master.getAttack();
        
        this.baseSpeed = master.getSpeed();
        this.currentSpeed = master.getSpeed();
        
        this.skills = skills;
        this.imageUrl = master.getImageUrl();
        this.isPlayerSide = isPlayerSide;
    }
    
    // --- 状態判定 ---

    public boolean isAlive() {
        return this.currentHp > 0;
    }

    public boolean isTameable() {
        // 敵であり、かつHPがぴったり0の場合のみ捕獲可能
        return !this.isPlayerSide && this.currentHp == 0;
    }

    // --- 戦闘アクション ---

    /**
     * ダメージ適用
     */
    public void applyDamage(int damage) {
        int remainingDamage = damage;

        // アーマーで吸収
        if (this.currentArmor > 0) {
            if (this.currentArmor >= remainingDamage) {
                this.currentArmor -= remainingDamage;
                remainingDamage = 0;
            } else {
                remainingDamage -= this.currentArmor;
                this.currentArmor = 0;
            }
        }

        // HP減少（マイナスも許容）
        this.currentHp -= remainingDamage;
    }

    /**
     * ★追加: 回復処理
     * 上限（maxHp）を超えないように回復する
     */
    public void heal(int amount) {
        // 死んでいる場合は回復しない（蘇生スキルを除く）
        if (!isAlive()) return;

        this.currentHp += amount;
        if (this.currentHp > this.maxHp) {
            this.currentHp = this.maxHp;
        }
    }

    /**
     * ステータス変化
     */
    public void applyStatus(int attackRate, int speedRate, int armorAdd) {
        if (attackRate != 100) {
            this.currentAttack = (this.baseAttack * attackRate) / 100;
        }
        if (speedRate != 100) {
            this.currentSpeed = (this.baseSpeed * speedRate) / 100;
        }
        
        this.currentArmor += armorAdd;
        if (this.currentArmor > this.maxArmor) {
            this.currentArmor = this.maxArmor;
        }
        if (this.currentArmor < 0) {
            this.currentArmor = 0;
        }
    }

    public void multiplyArmor(int multiplier) {
        this.currentArmor *= multiplier;
    }

    // --- リセット系 ---

    public void cleanupAfterBattle() {
        if (isAlive()) {
            this.currentArmor = this.maxArmor;
            this.currentAttack = this.baseAttack;
            this.currentSpeed = this.baseSpeed;
        }
    }

    public void fullyRecover() {
        this.currentHp = this.maxHp;
        this.currentArmor = this.maxArmor;
        this.currentAttack = this.baseAttack;
        this.currentSpeed = this.baseSpeed;
        
        if (this.skills != null) {
            for (BattleSkill skill : this.skills) {
                skill.recover();
            }
        }
    }
}