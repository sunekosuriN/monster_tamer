package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.EnemyParty;
import com.example.demo.entity.SkillMaster;
import com.example.demo.model.BattleContext;
import com.example.demo.model.BattleMonster;
import com.example.demo.model.BattleSkill;
import com.example.demo.model.GameState;
import com.example.demo.repository.EnemyPartyRepository;
import com.example.demo.repository.MonsterMasterRepository;
import com.example.demo.repository.SkillMasterRepository;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Service
public class BattleService {

    @Autowired
    private MonsterMasterRepository monsterRepository;
    
    @Autowired
    private SkillMasterRepository skillRepository;

    @Autowired
    private EnemyPartyRepository enemyPartyRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private GameState gameState;
    
    @Autowired
    private EnemyAIService enemyAIService;
    
    @Autowired
    private SkillEngineService skillEngine;

    private final Random random = new Random();

    /**
     * 戦闘開始処理
     */
    public BattleContext startBattle() {
        BattleContext context = new BattleContext();
        int floor = gameState.getCurrentFloor();
        context.setCurrentFloor(floor);
        context.setPlayerParty(gameState.getPlayerParty());

        // 1. 敵パーティの選出
        List<EnemyParty> candidates = enemyPartyRepository.findByFloor(floor);
        
        if (candidates.isEmpty()) {
            createRandomEnemyForDebug(context);
        } else {
            EnemyParty chosenParty = candidates.get(random.nextInt(candidates.size()));
            addMonsterIfExists(context, chosenParty.getMonsterLeftId());
            addMonsterIfExists(context, chosenParty.getMonsterCenterId());
            addMonsterIfExists(context, chosenParty.getMonsterRightId());
        }

        // 遭遇ログ（一度全ログをクリアしてから追加）
        context.clearLogs();
        String enemyNames = context.getEnemyParty().getParty().stream()
                .map(BattleMonster::getName)
                .collect(Collectors.joining(", "));
        context.addLog(messageService.getMessage("battle.log.encounter", enemyNames));
        
        gameState.setCurrentBattleContext(context);
        return context;
    }

    /**
     * ターン処理のエントリーポイント
     */
    public BattleContext processTurn(int playerSkillId, Integer targetIndex) {
        BattleContext context = gameState.getCurrentBattleContext();
        
        if (context == null || context.isBattleOver()) {
            return context;
        }

        // 1. このターンの新しいログだけをフロントに送るため、一旦クリア
        context.clearLogs();

        // 2. 全キャラクターの行動を決定し、実行する
        executeTurn(context, playerSkillId, targetIndex);
        
        return context;
    }

    /**
     * 1ターンの詳細な実行フロー
     */
    private void executeTurn(BattleContext context, int playerSkillId, Integer targetIndex) {
        // 行動者（現在はプレイヤー側の先頭モンスター1体とする）
        BattleMonster player = context.getPlayerParty().getParty().get(0);
        
        // プレイヤーが選んだ技の情報を取得
        SkillMaster playerSkill = skillRepository.findById(playerSkillId).orElse(null);
        
        // ターゲットの解決（敵を狙う技か、味方を狙う技か）
        BattleMonster playerTarget = resolveTarget(context, playerSkill, targetIndex, true);

        // 行動リストの構築
        List<ActionUnit> actions = new ArrayList<>();

        // プレイヤーの行動を登録
        if (player.isAlive()) {
            actions.add(new ActionUnit(player, playerTarget, playerSkillId));
        }

        // 全ての生存している敵の行動を登録（AIが決定）
        for (BattleMonster enemy : context.getEnemyParty().getLivingMembers()) {
            EnemyAIService.AIResult aiResult = enemyAIService.decideAction(enemy, context);
            actions.add(new ActionUnit(enemy, aiResult.getTarget(), aiResult.getSkillId()));
        }

        // 3. 素早さ順にソート
        actions.sort((a, b) -> {
            int speedDiff = b.getAttacker().getCurrentSpeed() - a.getAttacker().getCurrentSpeed();
            return (speedDiff != 0) ? speedDiff : (random.nextBoolean() ? 1 : -1);
        });

        // 4. 行動実行ループ
        for (ActionUnit action : actions) {
            // 行動者が生存しており、かつ戦闘が終わっていない場合のみ実行
            if (action.getAttacker().isAlive() && !context.isBattleOver()) {
                
                // スキル実行エンジンへ委譲
                skillEngine.executeSkill(
                    action.getAttacker(), 
                    action.getTarget(), 
                    action.getSkillId(), 
                    context
                );

                // 行動ごとに勝敗チェック（敵が全滅したらその時点でターン終了）
                if (updateBattleStatus(context)) break;
            }
        }

        context.setTurnCount(context.getTurnCount() + 1);
    }

    /**
     * ターゲット解決ロジック
     * スキルの対象タイプ(TargetType)に基づいて、適切なモンスターを返す
     */
    private BattleMonster resolveTarget(BattleContext context, SkillMaster skill, Integer index, boolean isPlayerActor) {
        if (skill == null) return null;
        
        String type = skill.getTargetType(); // ENEMY, ALLY, SELF, ENEMY_ALL...
        
        // 味方（自分側）を対象とする技の場合
        if ("ALLY".equals(type) || "SELF".equals(type) || "ALLY_ALL".equals(type) || "ALLY_DEAD".equals(type)) {
            List<BattleMonster> mySide = isPlayerActor ? context.getPlayerParty().getParty() : context.getEnemyParty().getParty();
            if (index != null && index >= 0 && index < mySide.size()) {
                return mySide.get(index);
            }
            return isPlayerActor ? context.getPlayerParty().getParty().get(0) : context.getEnemyParty().getParty().get(0);
        }

        // 敵側を対象とする技の場合
        List<BattleMonster> opponentSide = isPlayerActor ? context.getEnemyParty().getParty() : context.getPlayerParty().getParty();
        if (index != null && index >= 0 && index < opponentSide.size()) {
            BattleMonster target = opponentSide.get(index);
            // 死んでいる敵を狙おうとした場合は、生存している別の敵に自動振り替え
            if (target.isAlive()) return target;
        }
        
        // 生存している敵の先頭をデフォルトにする
        List<BattleMonster> livingOpponents = isPlayerActor ? context.getEnemyParty().getLivingMembers() : context.getPlayerParty().getLivingMembers();
        return livingOpponents.isEmpty() ? null : livingOpponents.get(0);
    }

    /**
     * 戦闘の勝敗判定
     */
    private boolean updateBattleStatus(BattleContext context) {
        if (context.getEnemyParty().isAllDead()) {
            context.setBattleOver(true);
            context.setVictory(true);
            
            // 仲間にできるかチェック
            BattleMonster tameable = context.getEnemyParty().getParty().stream()
                    .filter(BattleMonster::isTameable)
                    .findFirst()
                    .orElse(null);

            if (tameable != null) {
                context.setTamed(true);
                context.addLog(messageService.getMessage("battle.log.tamed", tameable.getName()));
            } else {
                context.addLog(messageService.getMessage("battle.result.victory_msg"));
            }
            return true;
            
        } else if (context.getPlayerParty().isAllDead()) {
            context.setBattleOver(true);
            context.setVictory(false);
            context.addLog(messageService.getMessage("battle.result.defeat_msg"));
            return true;
        }
        return false;
    }

    /**
     * 敵を仲間にする処理
     */
    public BattleContext recruitEnemy(Integer swapIndex) {
        BattleContext context = gameState.getCurrentBattleContext();
        if (context == null || !context.isBattleOver() || !context.isTamed()) return context;

        BattleMonster candidate = context.getEnemyParty().getParty().stream()
                .filter(BattleMonster::isTameable)
                .findFirst()
                .orElse(null);

        if (candidate == null) return context;

        candidate.fullyRecover();
        context.clearLogs();

        if (swapIndex != null && swapIndex >= 0) {
            BattleMonster removed = gameState.replaceMonster(swapIndex, candidate);
            if (removed != null) {
                context.addLog(messageService.getMessage("battle.log.swap", removed.getName(), candidate.getName()));
                context.setTamed(false);
            } else {
                context.addLog(messageService.getMessage("error.cannot.remove.hero"));
            }
        } else {
            if (gameState.addMonsterIfPossible(candidate)) {
                context.addLog(messageService.getMessage("battle.log.join", candidate.getName()));
                context.setTamed(false);
            } else {
                context.addLog(messageService.getMessage("battle.log.full"));
            }
        }
        return context;
    }

    /**
     * 戦闘終了（次の階層へ）
     */
    public boolean finishBattle() {
        BattleContext context = gameState.getCurrentBattleContext();
        if (context == null) return false;

        if (context.isVictory()) {
            gameState.advanceFloor();
            gameState.getPlayerParty().getParty().forEach(BattleMonster::cleanupAfterBattle);
            gameState.setCurrentBattleContext(null);
            return true;
        }
        gameState.setCurrentBattleContext(null);
        return false;
    }

    // --- 内部ヘルパー ---

    private void addMonsterIfExists(BattleContext context, Integer monsterId) {
        if (monsterId == null || monsterId <= 0) return;
        monsterRepository.findById(monsterId).ifPresent(master -> {
            List<BattleSkill> skills = skillRepository.findAllById(master.getSkillIdList())
                    .stream().map(BattleSkill::new).collect(Collectors.toList());
            context.getEnemyParty().addParty(new BattleMonster(master, skills, false));
        });
    }

    private void createRandomEnemyForDebug(BattleContext context) {
        monsterRepository.findAll().stream().findAny().ifPresent(m -> addMonsterIfExists(context, m.getId()));
    }

    @Getter
    @AllArgsConstructor
    private static class ActionUnit {
        private final BattleMonster attacker;
        private final BattleMonster target;
        private final int skillId;
    }
}