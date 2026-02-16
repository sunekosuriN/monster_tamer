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
 * BattleServiceから呼び出され、実際の数値変動やログ出力を担当する
 */
@Service
public class SkillEngineService {

    @Autowired
    private MessageService messageService;

    private final Random random = new Random();

    /**
     * スキルを実行し、結果をContextに反映するメインメソッド
     * * @param attacker 行動するモンスター
     * @param primaryTarget メインターゲット（単体攻撃の場合の対象）
     * @param skillId 使用するスキルのID
     * @param context 戦闘コンテキスト
     */
    public void executeSkill(BattleMonster attacker, BattleMonster primaryTarget, int skillId, BattleContext context) {
        // 1. スキル情報の取得
        BattleSkill skill = attacker.getSkills().stream()
                .filter(s -> s.getSkillMaster().getId() == skillId)
                .findFirst()
                .orElse(null);

        // スキルが存在しない、または使用回数切れ（MP切れ）の場合
        if (skill == null || !skill.canUse()) {
            context.addLog(messageService.getMessage("battle.log.miss", attacker.getName()));
            return;
        }

        SkillMaster master = skill.getSkillMaster();

        // 2. ターゲットの解決（単体、全体、敵味方、生存/死亡などの判定）
        List<BattleMonster> targets = resolveSkillTargets(attacker, primaryTarget, master, context);

        // 対象がいない場合（例：全員死んでいるのに回復しようとした等）
        if (targets.isEmpty()) {
            context.addLog(messageService.getMessage("battle.log.no_target"));
            return;
        }

        // 3. 技使用のログ出力
        context.addLog(messageService.getMessage("battle.log.use_skill", attacker.getName(), master.getName()));

        // 4. 効果適用ループ
        for (BattleMonster target : targets) {
            // 例外処理：蘇生技以外は、死体には効果がない
            boolean isResurrection = "RESURRECTION".equals(master.getEffectType());
            if (!target.isAlive() && !isResurrection) {
                continue; 
            }

            // 効果の適用
            applyEffect(attacker, target, master, context);
        }

        // 5. コスト消費
        skill.consume();
    }

    /**
     * 効果種別ごとの詳細な処理ロジック
     */
    private void applyEffect(BattleMonster attacker, BattleMonster target, SkillMaster skill, BattleContext context) {
        String type = skill.getEffectType(); // ATK, HEAL, BUFF...

        switch (type) {
            case "ATK":
            case "BASIC_ATTACK":
                // 通常ダメージ
                int damage = calculateDamage(attacker, skill);
                target.applyDamage(damage);
                context.addLog(messageService.getMessage("battle.log.damage", target.getName(), damage));
                
                // アーマーブレイク演出
                if (target.getCurrentArmor() == 0 && damage > 0 && target.getMaxArmor() > 0) {
                     context.addLog(messageService.getMessage("battle.log.armor_break", target.getName()));
                }
                break;

            case "HEAL":
                // 回復
                int healAmount = skill.getDamageValue(); // damage_valueを回復量として利用
                target.heal(healAmount);
                context.addLog(messageService.getMessage("battle.log.heal", target.getName(), healAmount));
                break;

            case "BUFF":
            case "DEBUFF":
                // ステータス変化 (攻撃力、素早さ、アーマー)
                target.applyStatus(skill.getChangeAtk(), skill.getChangeSpeed(), skill.getChangeArmor());
                String logKey = "BUFF".equals(type) ? "battle.log.buff" : "battle.log.debuff";
                context.addLog(messageService.getMessage(logKey, target.getName()));
                break;
                
            case "SUICIDE_ATTACK":
                // 自爆攻撃（メガンテ等）
                int suicideDamage = calculateDamage(attacker, skill);
                // 倍率など特有の計算があればここで調整
                // 例: suicideDamage *= 2; 
                
                target.applyDamage(suicideDamage);
                context.addLog(messageService.getMessage("battle.log.damage", target.getName(), suicideDamage));
                
             // ★修正: 自爆時は HP を -1 にする（捕獲不可にするため）
                if (attacker.isAlive()) {
                    attacker.setCurrentHp(-1); 
                    context.addLog(messageService.getMessage("battle.log.collapse", attacker.getName()));
                }
                break;
                
            case "ARMOR_ADD":
                // アーマー付与（防御力アップ）
                // 固定値または割合で増加させるロジック
                int armorAdd = 50; // 仮の固定値。本来はskill.getChangeArmor()などを使用
                target.applyStatus(100, 100, armorAdd);
                context.addLog(messageService.getMessage("battle.log.guard", target.getName()));
                break;
                
            case "BASIC_DEFENSE":
                // 防御（次のターンまでダメージ軽減など）
                // ※本来はBattleMonsterにisDefendingフラグを持たせるのが定石だが、
                // 簡易実装として一時的にアーマーを増やす、あるいはログだけ出す等
                context.addLog(target.getName() + "は身を守っている！");
                target.multiplyArmor(2); // アーマーを一時的に倍にする等
                break;

            case "ATK_RESERVE_HP": 
                // 特定のHPにする（例：深淵の目醒め -> HPを1にする）
                // ターゲットが生きていれば強制的にHPを1にする
                if (target.getCurrentHp() > 1) {
                    target.setCurrentHp(1);
                    context.addLog(target.getName() + "のHPが1になってしまった！");
                } else {
                    context.addLog("効果がなかった！");
                }
                break;
                
            case "RESURRECTION":
                // 蘇生
                if (!target.isAlive()) {
                    target.setCurrentHp(target.getMaxHp() / 2); // 半分で蘇生
                    context.addLog(target.getName() + "は生き返った！");
                } else {
                    context.addLog("しかし 何も起こらなかった！");
                }
                break;

            default:
                // 未定義の効果
                context.addLog("しかし 何も起こらなかった！");
                break;
        }
    }

    /**
     * ターゲット範囲の解決
     * スキルの設定(TargetType)に従い、効果を適用すべき全対象のリストを返す
     */
    private List<BattleMonster> resolveSkillTargets(BattleMonster attacker, BattleMonster primaryTarget, SkillMaster skill, BattleContext context) {
        String targetType = skill.getTargetType(); // ENEMY, ENEMY_ALL, SELF, ALLY_ALL, ALLY
        List<BattleMonster> targets = new ArrayList<>();
        
        boolean isAttackerPlayer = attacker.isPlayerSide();
        
        // 敵味方の判定
        List<BattleMonster> enemies = isAttackerPlayer ? context.getEnemyParty().getLivingMembers() : context.getPlayerParty().getLivingMembers();
        List<BattleMonster> allies = isAttackerPlayer ? context.getPlayerParty().getLivingMembers() : context.getEnemyParty().getLivingMembers();
        
        // 死者を含む全メンバー（蘇生用）
        List<BattleMonster> allAllies = isAttackerPlayer ? context.getPlayerParty().getParty() : context.getEnemyParty().getParty();

        if (targetType == null) targetType = "ENEMY"; // デフォルト

        switch (targetType) {
            case "ENEMY_ALL":
                // 敵全体
                targets.addAll(enemies);
                break;
                
            case "ALLY_ALL":
                // 味方全体
                targets.addAll(allies);
                break;
                
            case "SELF":
                // 自分自身
                targets.add(attacker);
                break;
                
            case "ALLY":
                // 味方単体
                // AIの場合はprimaryTargetが既にロジックで選ばれているはず
                // プレイヤーの場合は選択UIがないため、暫定的に「HPが減っている味方」などを選ぶか、primaryTargetを使う
                if (primaryTarget != null && primaryTarget.isPlayerSide() == attacker.isPlayerSide()) {
                    targets.add(primaryTarget);
                } else {
                    // フォールバック: 自分自身
                    targets.add(attacker);
                }
                break;
                
            case "ALLY_DEAD":
                // 蘇生対象（死んでいる味方）
                BattleMonster deadAlly = allAllies.stream()
                        .filter(m -> !m.isAlive())
                        .findFirst()
                        .orElse(null);
                if (deadAlly != null) targets.add(deadAlly);
                break;

            case "ENEMY":
            default:
                // 敵単体
                if (primaryTarget != null && primaryTarget.isAlive() && primaryTarget.isPlayerSide() != attacker.isPlayerSide()) {
                    targets.add(primaryTarget);
                } else if (!enemies.isEmpty()) {
                    // ターゲット指定がない、または無効な場合は先頭の敵
                    targets.add(enemies.get(0));
                }
                break;
        }
        return targets;
    }

    /**
     * ダメージ計算ロジック
     * (攻撃力 * 倍率 / 100) * 乱数
     */
    private int calculateDamage(BattleMonster attacker, SkillMaster skill) {
        // 固定ダメージ技の場合（倍率0かつ値設定あり）
        if (skill.getDamageValue() > 0 && skill.getDamageMultiplier() == 0) {
            return skill.getDamageValue();
        }
        
        // 倍率計算 (例: 150 -> 1.5倍)
        double multiplier = skill.getDamageMultiplier() > 0 ? skill.getDamageMultiplier() / 100.0 : 1.0;
        
        double baseDamage = attacker.getCurrentAttack() * multiplier;
        
        // 乱数補正 (0.95 〜 1.05)
        double variance = 0.95 + (0.1 * random.nextDouble());
        
        return (int) Math.max(1, baseDamage * variance);
    }
}