package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.EnemyParty;
import com.example.demo.entity.MonsterMaster;
import com.example.demo.model.BattleContext;
import com.example.demo.model.BattleMonster;
import com.example.demo.model.BattleSkill;
import com.example.demo.model.GameState;
import com.example.demo.repository.EnemyPartyRepository;
import com.example.demo.repository.MonsterMasterRepository;
import com.example.demo.repository.SkillMasterRepository;
import com.example.demo.service.EnemyAIService.AIResult;

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
    
    // 行動決定ロジック（頭脳）
    @Autowired
    private EnemyAIService enemyAIService;
    
    // 行動実行ロジック（肉体）
    @Autowired
    private SkillEngineService skillEngine;

    private final Random random = new Random();

    /**
     * 戦闘開始処理
     * 現在の階層に基づいて敵パーティをDBから取得・生成し、セッションに保存する
     */
    public BattleContext startBattle() {
        BattleContext context = new BattleContext();
        int floor = gameState.getCurrentFloor();
        context.setCurrentFloor(floor);
        context.setPlayerParty(gameState.getPlayerParty());

        // 1. 敵パーティの選出
        List<EnemyParty> candidates = enemyPartyRepository.findByFloor(floor);
        
        if (candidates.isEmpty()) {
            // データ未登録階層用のフェイルセーフ
            createRandomEnemyForDebug(context);
        } else {
            EnemyParty chosenParty = candidates.get(random.nextInt(candidates.size()));
            
            // 左・中・右の敵を生成
            addMonsterIfExists(context, chosenParty.getMonsterLeftId());
            addMonsterIfExists(context, chosenParty.getMonsterCenterId());
            addMonsterIfExists(context, chosenParty.getMonsterRightId());
        }
        
     // ★追加：システムコンソールへのHPデバッグ出力
        System.out.println("========== 戦闘開始デバッグ ==========");
        System.out.println("フロア: " + floor);
        
        // 主人公側の確認
        context.getPlayerParty().getParty().forEach(p -> 
            System.out.println("味方: " + p.getName() + " [HP: " + p.getCurrentHp() + "/" + p.getMaxHp() + " / Alive: " + p.isAlive() + "]")
        );
        
        // 敵側の確認
        context.getEnemyParty().getParty().forEach(e -> 
            System.out.println("敵軍: " + e.getName() + " [HP: " + e.getCurrentHp() + "/" + e.getMaxHp() + " / Alive: " + e.isAlive() + "]")
        );
        
        System.out.println("敵の全滅判定: " + context.getEnemyParty().isAllDead());
        System.out.println("======================================");

        // 遭遇ログ
        String enemyNames = context.getEnemyParty().getParty().stream()
                .map(BattleMonster::getName)
                .collect(Collectors.joining(", "));
        context.addLog(messageService.getMessage("battle.log.encounter", enemyNames));
        
        // コンテキストを保存
        gameState.setCurrentBattleContext(context);
        
     // BattleService.java
        System.out.println("生成された敵の数: " + context.getEnemyParty().getParty().size());
        
        return context;
    }

    /**
     * Controllerから呼ばれるターン処理のエントリーポイント
     * @param playerSkillId プレイヤーが選択したスキルID
     * @param targetIndex プレイヤーが選択したターゲットのインデックス（任意）
     */
    public BattleContext processTurn(int playerSkillId, Integer targetIndex) {
        BattleContext context = gameState.getCurrentBattleContext();
        
        // バリデーション：戦闘中かどうか
        if (context == null || context.isBattleOver()) {
            return context;
        }

        // ターンの実行
        executeTurn(context, playerSkillId, targetIndex);
        
        return context;
    }

    /**
     * 1ターンの詳細な実行フロー
     * 行動順の決定 -> 行動の実行 -> 決着判定
     */
    private void executeTurn(BattleContext context, int playerSkillId, Integer targetIndex) {
        if (context.isBattleOver()) return;

        BattleMonster player = context.getPlayerParty().getParty().get(0);
        
        // 1. プレイヤーのターゲット解決
        // (UIで指定されたインデックス、なければ敵の先頭)
        BattleMonster playerTarget = resolvePlayerTarget(context, targetIndex);

        // 2. 行動リスト（ActionUnit）の構築
        List<ActionUnit> actions = new ArrayList<>();

        // プレイヤーの行動登録
        if (player.isAlive()) {
            actions.add(new ActionUnit(player, playerTarget, playerSkillId));
        }

        // 敵の行動登録（AIサービスに思考させる）
        for (BattleMonster enemy : context.getEnemyParty().getLivingMembers()) {
            // AIが「スキル」と「ターゲット」を決定して返す
            AIResult aiResult = enemyAIService.decideAction(enemy, context);
            actions.add(new ActionUnit(enemy, aiResult.getTarget(), aiResult.getSkillId()));
        }

        // 3. 素早さ順にソート（同値ならランダム）
        actions.sort((a, b) -> {
            int speedDiff = b.getAttacker().getCurrentSpeed() - a.getAttacker().getCurrentSpeed();
            return (speedDiff != 0) ? speedDiff : (random.nextBoolean() ? 1 : -1);
        });

        // 4. 行動実行ループ
        for (ActionUnit action : actions) {
            // 行動者が死んでいる、または既に戦闘が終わっている場合はスキップ
            if (!action.getAttacker().isAlive() || context.isBattleOver()) continue;

            // ★実行エンジンへの委譲
            // ダメージ計算や効果適用はすべてここで行う
            skillEngine.executeSkill(
                action.getAttacker(), 
                action.getTarget(), 
                action.getSkillId(), 
                context
            );

            // 行動のたびに勝敗判定を行う（敵全滅で即終了など）
            if (updateBattleStatus(context)) break;
        }

        context.setTurnCount(context.getTurnCount() + 1);
    }

    /**
     * プレイヤーの攻撃対象を決定するヘルパーメソッド
     */
    private BattleMonster resolvePlayerTarget(BattleContext context, Integer targetIndex) {
        List<BattleMonster> enemies = context.getEnemyParty().getLivingMembers();
        
        // 敵がいない場合（念のため）
        if (enemies.isEmpty()) return null;
        
        // 指定されたインデックスが有効かチェック
        if (targetIndex != null && targetIndex >= 0 && targetIndex < context.getEnemyParty().getParty().size()) {
            BattleMonster target = context.getEnemyParty().getParty().get(targetIndex);
            if (target.isAlive()) {
                return target;
            }
        }
        
        // デフォルト：生存している敵の先頭
        return enemies.get(0);
    }

    /**
     * 戦闘の勝敗・終了判定
     * @return 戦闘が終了した場合は true
     */
    private boolean updateBattleStatus(BattleContext context) {
        if (context.getEnemyParty().isAllDead()) {
            // 勝利
            context.setBattleOver(true);
            context.setVictory(true);
            
            // 仲間にできるモンスターがいるかチェック
            BattleMonster tameable = context.getEnemyParty().getParty().stream()
                    .filter(BattleMonster::isTameable)
                    .findFirst()
                    .orElse(null);

            if (tameable != null) {
                context.setTamed(true);
                context.addLog(messageService.getMessage("battle.log.tame_success", tameable.getName()));
            } else {
                context.addLog(messageService.getMessage("battle.log.victory"));
            }
            return true;
            
        } else if (context.getPlayerParty().isAllDead()) {
            // 敗北
            context.setBattleOver(true);
            context.setVictory(false);
            context.addLog(messageService.getMessage("battle.log.defeat"));
            return true;
        }
        return false;
    }

    /**
     * 倒した敵を仲間にする処理
     * @param swapIndex 入れ替え対象の味方インデックス
     */
    public BattleContext recruitEnemy(Integer swapIndex) {
        BattleContext context = gameState.getCurrentBattleContext();
        
        if (context == null || !context.isBattleOver() || !context.isTamed()) {
            return context;
        }

        // 仲間候補の検索
        BattleMonster candidate = context.getEnemyParty().getParty().stream()
                .filter(BattleMonster::isTameable)
                .findFirst()
                .orElse(null);

        if (candidate == null) {
            context.addLog(messageService.getMessage("error.enemy.not_found"));
            return context;
        }

        // 全回復させて仲間にする
        candidate.fullyRecover();
        
        boolean success = false;
        String logMessage = "";

        if (swapIndex != null && swapIndex >= 0) {
            // 入れ替え
            BattleMonster removed = gameState.replaceMonster(swapIndex, candidate);
            if (removed != null) {
                logMessage = messageService.getMessage("battle.log.swap", removed.getName(), candidate.getName());
                success = true;
            } else {
                logMessage = messageService.getMessage("error.cannot.remove.hero");
            }
        } else {
            // 追加
            if (gameState.addMonsterIfPossible(candidate)) {
                logMessage = messageService.getMessage("battle.log.join", candidate.getName());
                success = true;
            } else {
                logMessage = messageService.getMessage("battle.log.full");
            }
        }

        context.addLog(logMessage);
        if (success) {
            context.setTamed(false); // 処理完了のためフラグを下ろす
        }

        return context;
    }

    /**
     * 戦闘終了後の後処理（「次へ」ボタン押下時）
     */
    public boolean finishBattle() {
        BattleContext context = gameState.getCurrentBattleContext();
        if (context == null) return false;

        boolean isVictory = context.isVictory();

        if (isVictory) {
            // 1. 階層を進める
            gameState.advanceFloor();
            
            // 2. 味方のステータスクリーンアップ（バフ解除・アーマー回復）
            for (BattleMonster member : gameState.getPlayerParty().getParty()) {
                member.cleanupAfterBattle();
            }
            
            // 3. コンテキスト破棄
            gameState.setCurrentBattleContext(null);
            
            return true;
        } else {
            // 敗北時はリセットせず、Controller側でゲームオーバー画面へ誘導
            gameState.setCurrentBattleContext(null);
            return false;
        }
    }

    // --- 内部ヘルパー ---

    private void addMonsterIfExists(BattleContext context, Integer monsterId) {
        if (monsterId == null || monsterId <= 0) return;

        monsterRepository.findById(monsterId).ifPresent(master -> {
            List<Integer> skillIds = master.getSkillIdList();
            List<BattleSkill> skills = skillRepository.findAllById(skillIds)
                    .stream()
                    .map(BattleSkill::new)
                    .collect(Collectors.toList());

            BattleMonster enemy = new BattleMonster(master, skills, false);
            context.getEnemyParty().addParty(enemy);
        });
    }

    private void createRandomEnemyForDebug(BattleContext context) {
        List<MonsterMaster> all = monsterRepository.findAll();
        if (!all.isEmpty()) {
            MonsterMaster mm = all.get(random.nextInt(all.size()));
            addMonsterIfExists(context, mm.getId());
        }
    }

    /**
     * 行動順制御のための内部クラス
     */
    @Getter
    @AllArgsConstructor
    private static class ActionUnit {
        private final BattleMonster attacker;
        private final BattleMonster target;
        private final int skillId;
    }
}