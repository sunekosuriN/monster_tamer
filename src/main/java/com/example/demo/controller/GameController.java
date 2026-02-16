package com.example.demo.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.SkillMaster;
import com.example.demo.model.GameState;
import com.example.demo.repository.SkillMasterRepository;

@Controller
public class GameController {

    @Autowired
    private GameState gameState;

    @Autowired
    private SkillMasterRepository skillRepository;

    // タイトル画面
    @GetMapping("/")
    public String index() {
        gameState.reset(); 
        return "index";
    }

    // ★修正: 技選択画面
    @GetMapping("/skill_select")
    public String skillSelect(Model model) {
        // DBに登録されている「初期選択用スキル」のIDリスト
        List<Integer> initialSkillIds = Arrays.asList(911, 912, 913, 914, 915);
        
        // DBから情報を取得
        List<SkillMaster> skillList = skillRepository.findAllById(initialSkillIds);
        
        // Thymeleafに渡す
        model.addAttribute("skillList", skillList);
        
        return "skill_select";
    }

    // 戦闘画面
    @GetMapping("/battle")
    public String battle() {
        return "battle";
    }
    
    // 以下、その他の画面遷移
    @GetMapping("/game_over")
    public String gameOver() { return "game_over"; }
    
    @GetMapping("/game_clear")
    public String gameClear() { return "game_clear"; }
    
    @GetMapping("/rest")
    public String rest() { return "rest"; }
}