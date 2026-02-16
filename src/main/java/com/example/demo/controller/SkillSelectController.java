package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.MonsterMaster;
import com.example.demo.model.BattleMonster;
import com.example.demo.model.BattleSkill;
import com.example.demo.model.GameState;
import com.example.demo.repository.MonsterMasterRepository;
import com.example.demo.repository.SkillMasterRepository;

@RestController
public class SkillSelectController {

    @Autowired
    private GameState gameState;
    
    @Autowired
    private MonsterMasterRepository monsterRepository;
    
    @Autowired
    private SkillMasterRepository skillRepository;

    // 技選択完了 -> 主人公生成API
    @PostMapping("/api/init-player")
    public boolean initPlayer(@RequestBody List<Integer> selectedSkillIds) {
        
        // 1. ゲーム状態のリセット
        gameState.reset();
        
        // 2. ★修正: デバッグ勇者 (ID:99) を取得
        // DBに ID:99 のデータが存在することを確認してください
        MonsterMaster heroMaster = monsterRepository.findById(99).orElse(null);
        
        if (heroMaster == null) {
            System.err.println("Error: Monster ID 99 not found.");
            return false;
        }

        // 3. スキルリストの作成
        // 基本技（通常攻撃901, 防御902）は必須で追加
        List<Integer> finalSkillIds = new ArrayList<>();
        finalSkillIds.add(901);
        finalSkillIds.add(902);
        
        // フロントから送られてきた選択スキル（911〜915の中から選ばれたもの）を追加
        if (selectedSkillIds != null) {
            finalSkillIds.addAll(selectedSkillIds);
        }

        List<BattleSkill> skills = skillRepository.findAllById(finalSkillIds)
                .stream()
                .map(BattleSkill::new)
                .collect(Collectors.toList());

        // 4. BattleMonster（主人公）を生成してパーティに追加
        // isPlayerSide = true
        BattleMonster hero = new BattleMonster(heroMaster, skills, true);
        gameState.getPlayerParty().addParty(hero);
        
        return true;
    }
}