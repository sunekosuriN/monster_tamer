package com.example.demo.service;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.EnemyActionPattern;
import com.example.demo.model.BattleContext;
import com.example.demo.model.BattleMonster;
import com.example.demo.repository.EnemyActionPatternRepository;

import lombok.AllArgsConstructor;
import lombok.Data;

@Service
public class EnemyAIService {

    @Autowired
    private EnemyActionPatternRepository patternRepository;

    private final Random random = new Random();

    /**
     * 敵の行動を決定する
     * @param enemy 行動する敵モンスター
     * @param context 戦闘状況
     * @return 決定されたスキルIDとターゲットのペア
     */
    public AIResult decideAction(BattleMonster enemy, BattleContext context) {
        // 1. 行動パターンのリストを取得（優先度順）
        List<EnemyActionPattern> patterns = patternRepository.findByMonsterIdOrderByPriorityDesc(enemy.getId());

        EnemyActionPattern selectedPattern = null;

        // 2. パターンを上から順に判定
        for (EnemyActionPattern pattern : patterns) {
            if (checkCondition(pattern, enemy, context)) {
                // 条件一致。確率判定へ
                if (random.nextInt(100) < pattern.getActivationRate()) {
                    selectedPattern = pattern;
                    break; // 決定
                }
            }
        }

        // 3. パターンが決まらなかった場合（データ不備などの保険）は通常攻撃(901)
        int skillId = (selectedPattern != null) ? selectedPattern.getActionSkillId() : 901;
        String policy = (selectedPattern != null) ? selectedPattern.getTargetPolicy() : "RANDOM";

        // 4. ターゲット決定
        BattleMonster target = selectTarget(enemy, policy, context);

        return new AIResult(skillId, target);
    }

    /**
     * 条件判定ロジック
     */
    private boolean checkCondition(EnemyActionPattern pattern, BattleMonster enemy, BattleContext context) {
        String type = pattern.getConditionType();
        int value = pattern.getConditionValue();
        int currentTurn = context.getTurnCount(); // または enemy.actCount を使うならそちら

        switch (type) {
            case "ALWAYS":
                return true;
            case "TURN_EQ":
                return currentTurn == value;
            case "TURN_LESS":
                return currentTurn < value;
            case "TURN_GREATER":
                return currentTurn > value;
            case "TURN_MOD_3":
                return currentTurn % 3 == 0;
            case "TURN_MOD_4":
                return currentTurn % 4 == 0;
            case "HP_UNDER":
                double hpPercent = (double) enemy.getCurrentHp() / enemy.getMaxHp() * 100;
                return hpPercent <= value;
            default:
                return false;
        }
    }

    /**
     * ターゲット選択ロジック
     */
    private BattleMonster selectTarget(BattleMonster actor, String policy, BattleContext context) {
        List<BattleMonster> players = context.getPlayerParty().getLivingMembers();
        List<BattleMonster> enemies = context.getEnemyParty().getLivingMembers(); // 味方（敵同士）

        // プレイヤーが全滅している場合のガード
        if (players.isEmpty()) return actor; 

        // デフォルトターゲット（ランダム）
        BattleMonster defaultTarget = players.get(random.nextInt(players.size()));

        if (policy == null) return defaultTarget;

        switch (policy) {
            case "LOW_HP": // HPが低いプレイヤーを狙う
                return players.stream()
                        .min(Comparator.comparingInt(BattleMonster::getCurrentHp))
                        .orElse(defaultTarget);

            case "HIGH_HP": // HPが高いプレイヤーを狙う
                return players.stream()
                        .max(Comparator.comparingInt(BattleMonster::getCurrentHp))
                        .orElse(defaultTarget);

            case "PLAYER": // 主人公(と仮定される先頭)を狙う
                return players.get(0);

            case "ALLY_LOW_HP": // ピンチの味方(敵)を助ける
                return enemies.stream()
                        .min(Comparator.comparingInt(BattleMonster::getCurrentHp))
                        .orElse(actor);

            case "SELF": // 自分自身
                return actor;

            case "RANDOM":
            default:
                return defaultTarget;
        }
    }

    /**
     * 結果返却用のクラス
     */
    @Data
    @AllArgsConstructor
    public static class AIResult {
        private int skillId;
        private BattleMonster target;
    }
}