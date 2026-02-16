package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.SkillMaster;
import com.example.demo.model.BattleContext;
import com.example.demo.model.BattleMonster;
import com.example.demo.model.BattleSkill;

/**
 * スキルの効果計算、ターゲット解決、適用を行う実行エンジン
 */
@Service
public class SkillEngineService {

    @Autowired
    private MessageService messageService;

    private final Random random = new Random();

    /**
     * スキルを実行し、結果をContextに反映するメインメソッド
     */
    public void executeSkill(BattleMonster attacker, BattleMonster primaryTarget, int skillId, BattleContext context) {
        // 1. スキル情報の取得
        BattleSkill skill = attacker.getSkills().stream()
                .filter(s -> s.getSkillMaster().getId() == skillId)
                .findFirst()
                .orElse(null);

        // スキルが存在しない、または使用回数切れの場合
        if (skill == null || !skill.canUse()) {
            context.addLog(messageService.getMessage("battle.log.miss", attacker.getName()));
            return;
        }

        SkillMaster master = skill.getSkillMaster();

        // 2. ターゲットの解決
        List<BattleMonster> targets = resolveSkillTargets(attacker, primaryTarget, master, context);

        // 対象がいない場合
        if (targets.isEmpty()) {
            context.addLog(messageService.getMessage("battle.log.no_target"));
            return;
        }

        // 3. 技使用のログ出力
        context.addLog(messageService.getMessage("battle.log.use_skill", attacker.getName(), master.getName()));

        // 4. 効果適用ループ
        for (BattleMonster target : targets) {
            // 蘇生技以外は、死んでいる対象には適用しない
            boolean isResurrection = "RESURRECTION".equals(master.getEffectType());
            if (!target.isAlive() && !isResurrection) {
                continue; 
            }

            applyEffect(attacker, target, master, context);
        }

        // 5. コスト消費
        skill.consume();
    }

    /**
     * 効果種別ごとの詳細な処理ロジック
     */
    private void applyEffect(BattleMonster attacker, BattleMonster target, SkillMaster skill, BattleContext context) {
        String type = skill.getEffectType();

        switch (type) {
            case "ATK":
            case "BASIC_ATTACK":
                // ダメージ計算と適用
                int damage = calculateDamage(attacker, skill);
                target.applyDamage(damage);
                context.addLog(messageService.getMessage("battle.log.damage", target.getName(), damage));
                
                // アーマーブレイク判定
                if (target.getCurrentArmor() == 0 && damage > 0 && target.getMaxArmor() > 0) {
                     context.addLog(messageService.getMessage("battle.log.armor_break", target.getName()));
                }
                
                // 対象が力尽きたかチェック
                if (!target.isAlive()) {
                    context.addLog(messageService.getMessage("battle.log.collapse", target.getName()));
                }
                break;

            case "HEAL":
                int healAmount = skill.getDamageValue(); // damage_valueを回復量として利用
                target.heal(healAmount);
                context.addLog(messageService.getMessage("battle.log.recover", target.getName(), healAmount));
                break;

            case "BUFF":
            case "DEBUFF":
                // ステータス変化適用
                target.applyStatus(skill.getChangeAtk(), skill.getChangeSpeed(), skill.getChangeArmor());
                String logKey = "BUFF".equals(type) ? "battle.log.buff" : "battle.log.debuff";
                context.addLog(messageService.getMessage(logKey, target.getName()));
                break;
                
            case "ARMOR_ADD":
                // アーマー加算
                int armorAdd = skill.getChangeArmor();
                target.applyStatus(100, 100, armorAdd);
                context.addLog(messageService.getMessage("battle.log.armor_up", target.getName(), armorAdd));
                break;
                
            case "SUICIDE_ATTACK":
                // 自爆攻撃
                int sDamage = calculateDamage(attacker, skill);
                target.applyDamage(sDamage);
                context.addLog(messageService.getMessage("battle.log.damage", target.getName(), sDamage));
                
                // 行動者は戦闘不能になる（捕獲不可にするため HP -1）
                if (attacker.isAlive()) {
                    attacker.setCurrentHp(-1); 
                    context.addLog(messageService.getMessage("battle.log.collapse", attacker.getName()));
                }
                break;
                
            case "BASIC_DEFENSE":
                // 防御（アーマーを一時的に強化）
                target.multiplyArmor(2);
                context.addLog(messageService.getMessage("battle.log.guard", target.getName()));
                break;

            case "ATK_RESERVE_HP": 
                // 強制的にHPを1にする
                if (target.getCurrentHp() > 1) {
                    target.setCurrentHp(1);
                    context.addLog(messageService.getMessage("battle.log.hp_one", target.getName()));
                } else {
                    context.addLog(messageService.getMessage("battle.log.nothing"));
                }
                break;
                
            case "RESURRECTION":
                // 蘇生処理
                if (!target.isAlive()) {
                    target.setCurrentHp(target.getMaxHp() / 2); // HP50%で復活
                    context.addLog(messageService.getMessage("battle.log.resurrection", target.getName()));
                } else {
                    context.addLog(messageService.getMessage("battle.log.nothing"));
                }
                break;

            default:
                context.addLog(messageService.getMessage("battle.log.nothing"));
                break;
        }
    }

    /**
     * ターゲット範囲の解決
     */
    private List<BattleMonster> resolveSkillTargets(BattleMonster attacker, BattleMonster primaryTarget, SkillMaster skill, BattleContext context) {
        String targetType = skill.getTargetType();
        List<BattleMonster> targets = new ArrayList<>();
        boolean isAttackerPlayer = attacker.isPlayerSide();
        
        // 生存リスト
        List<BattleMonster> enemies = isAttackerPlayer ? context.getEnemyParty().getLivingMembers() : context.getPlayerParty().getLivingMembers();
        List<BattleMonster> allies = isAttackerPlayer ? context.getPlayerParty().getLivingMembers() : context.getEnemyParty().getLivingMembers();
        
        // 全リスト（蘇生用）
        List<BattleMonster> allAllies = isAttackerPlayer ? context.getPlayerParty().getParty() : context.getEnemyParty().getParty();

        if (targetType == null) targetType = "ENEMY";

        switch (targetType) {
            case "ENEMY_ALL":
                targets.addAll(enemies);
                break;
                
            case "ALLY_ALL":
                targets.addAll(allies);
                break;
                
            case "SELF":
                targets.add(attacker);
                break;
                
            case "ALLY":
                if (primaryTarget != null && primaryTarget.isPlayerSide() == attacker.isPlayerSide()) {
                    targets.add(primaryTarget);
                } else {
                    targets.add(attacker);
                }
                break;
                
            case "ALLY_DEAD":
                // 死んでいる味方を優先的に探す
                BattleMonster deadAlly = allAllies.stream()
                        .filter(m -> !m.isAlive())
                        .findFirst()
                        .orElse(null);
                if (deadAlly != null) targets.add(deadAlly);
                break;

            case "ENEMY":
            default:
                if (primaryTarget != null && primaryTarget.isAlive() && primaryTarget.isPlayerSide() != attacker.isPlayerSide()) {
                    targets.add(primaryTarget);
                } else if (!enemies.isEmpty()) {
                    targets.add(enemies.get(0));
                }
                break;
        }
        return targets;
    }

    /**
     * ダメージ計算
     */
    private int calculateDamage(BattleMonster attacker, SkillMaster skill) {
        if (skill.getDamageValue() > 0 && skill.getDamageMultiplier() == 0) {
            return skill.getDamageValue();
        }
        
        double multiplier = skill.getDamageMultiplier() > 0 ? skill.getDamageMultiplier() / 100.0 : 1.0;
        double baseDamage = attacker.getCurrentAttack() * multiplier;
        
        // 乱数 0.95 〜 1.05
        double variance = 0.95 + (0.1 * random.nextDouble());
        
        return (int) Math.max(1, baseDamage * variance);
    }
}