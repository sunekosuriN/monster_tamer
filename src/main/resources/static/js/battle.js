/**
 * 魔物使いのダンジョン - 戦闘メインロジック (battle.js)
 */

// --- グローバル変数 ---
let currentContext = null;      // サーバーから受け取った最新の戦闘データ
let selectedAllyIndex = null;   // 現在コマンド入力中の味方のインデックス
let selectedSkillId = null;     // 選択された技のID

/**
 * 1. 初期化処理
 */
document.addEventListener("DOMContentLoaded", () => {
    startBattle();
});

/**
 * 戦闘開始 API呼び出し
 */
async function startBattle() {
    try {
        const response = await fetch("/battle/start", { method: "POST" });
        if (!response.ok) throw new Error("API通信失敗");
        
        currentContext = await response.json();
        console.log("Battle Started Data:", currentContext);
        
        renderBattle();
    } catch (e) {
        console.error(e);
        const logArea = document.getElementById("log-message-area");
        logArea.innerHTML = `<div style="color:#ff6b6b">【エラー】戦闘を開始できませんでした。サーバーの状態を確認してください。</div>`;
    }
}

/**
 * 2. 画面描画 (Render)
 * サーバーのデータをHTML要素に変換して反映する
 */
function renderBattle() {
    if (!currentContext) return;

    // 項目3: 背景画像の更新 (2層ごとに切り替え)
    updateBackground();

    // --- 敵エリアの描画 ---
    const enemyArea = document.getElementById("enemy-area");
    enemyArea.innerHTML = "";
    currentContext.enemyParty.party.forEach((unit, index) => {
        // 敵は通常時はクリック不可。技選択後のターゲット指定時のみ有効化される。
        enemyArea.appendChild(createUnitBox(unit, index, true));
    });

    // --- 味方エリアの描画 ---
    const allyArea = document.getElementById("ally-area");
    allyArea.innerHTML = "";
    currentContext.playerParty.party.forEach((unit, index) => {
        const allyBox = createUnitBox(unit, index, false);
        
        // 項目1: 味方をクリックして操作キャラを選択可能にする
        if (unit.alive) {
            allyBox.onclick = () => {
                selectedAllyIndex = index;
                selectedSkillId = null; // 他の味方に切り替えたら技選択をリセット
                renderBattle();
            };
        }
        
        // 選択中の味方を視覚的に強調
        if (selectedAllyIndex === index) {
            allyBox.classList.add("selected-unit");
        }
        
        allyArea.appendChild(allyBox);
    });

    // --- ログの更新 ---
    const logArea = document.getElementById("log-message-area");
    if (currentContext.logs && currentContext.logs.length > 0) {
        logArea.innerHTML = currentContext.logs.map(log => `<div>${log}</div>`).join("");
        logArea.scrollTop = logArea.scrollHeight;
    }

    // --- 操作パネル（コマンド）の表示制御 ---
    const actionOverlay = document.getElementById("action-overlay");
    if (currentContext.battleOver) {
        actionOverlay.classList.add("hidden");
        showResultModal();
    } else if (selectedAllyIndex !== null) {
        // 味方を選択している場合のみ「技」「ステータス確認」を表示
        actionOverlay.classList.remove("hidden");
    } else {
        actionOverlay.classList.add("hidden");
    }
}

/**
 * 項目3: 背景切り替えロジック
 * 仕様: 1-2層=floor1, 3-4層=floor2, 5-6層=floor3...
 */
function updateBackground() {
    const screen = document.getElementById("battle-screen");
    const floor = currentContext.currentFloor || 1;
    const bgNum = Math.ceil(floor / 2);
    
    // パス: src/main/resources/static/images/floor/floorX.png
    screen.style.backgroundImage = `url('/images/floor/floor${bgNum}.png')`;
    screen.style.backgroundSize = "cover";
}

/**
 * モンスターの枠を作成 (画像エラーハンドリング付き)
 */
function createUnitBox(unit, index, isEnemy) {
    const div = document.createElement("div");
    div.className = `unit-box ${isEnemy ? 'enemy' : 'ally'} ${unit.alive ? '' : 'dead'}`;
    
    const hpPercent = (unit.currentHp / unit.maxHp) * 100;
    
    // DB修正済みだが、念のためフロントでも画像エラー時の補完を入れる
    const imgUrl = unit.imageUrl || '/images/monsters/bomb.png';

    div.innerHTML = `
        <div class="unit-name">${unit.name}</div>
        <div class="img-container">
            <img src="${imgUrl}" class="unit-img" onerror="this.onerror=null;this.src='/images/monsters/bomb.png';">
        </div>
        <div class="hp-bar-container">
            <div class="hp-bar-fill" style="width: ${hpPercent}%"></div>
        </div>
        <div class="hp-text">${unit.currentHp} / ${unit.maxHp}</div>
    `;
    return div;
}

/**
 * 3. コマンドアクション
 */

// 「技」ボタン：スキル選択モーダルを開く
function openSkillWindow() {
    if (selectedAllyIndex === null) return;
    
    const skillList = document.getElementById("skill-list");
    skillList.innerHTML = "";
    const actor = currentContext.playerParty.party[selectedAllyIndex];
    
    // 誰の技かタイトルを表示
    document.getElementById("skill-window-title").innerText = `${actor.name} の技`;

    actor.skills.forEach(skill => {
        const btn = document.createElement("button");
        btn.className = "skill-btn";
        const uses = skill.skillMaster.maxUses === -1 ? "∞" : `${skill.remainingUses}/${skill.skillMaster.maxUses}`;
        
        btn.innerHTML = `
            <div class="s-name">${skill.skillMaster.name}</div>
            <div class="s-uses">回数: ${uses}</div>
        `;

        if (skill.remainingUses !== 0) {
            btn.onclick = () => {
                selectedSkillId = skill.skillMaster.id;
                closeSkillWindow();
                // ターゲット選択へ
                handleTargeting(skill.skillMaster);
            };
        } else {
            btn.disabled = true;
            btn.style.opacity = "0.5";
        }
        skillList.appendChild(btn);
    });

    document.getElementById("skill-window").classList.remove("hidden");
}

function closeSkillWindow() {
    document.getElementById("skill-window").classList.add("hidden");
}

/**
 * 技の種類に基づいたターゲット選択フロー
 */
function handleTargeting(skill) {
    const type = skill.targetType; // ENEMY, ALLY, SELF, ENEMY_ALL, ALLY_ALL, ALLY_DEAD
    const logArea = document.getElementById("log-message-area");

    // 全体技・自分自身への技は、対象選択をスキップして即実行
    if (type === "SELF") {
        executeSkill(skill.id, selectedAllyIndex);
    } else if (type === "ENEMY_ALL" || type === "ALLY_ALL") {
        executeSkill(skill.id, 0); // インデックス0を送り、サーバー側で全体に適用
    } else if (type === "ALLY" || type === "ALLY_DEAD") {
        logArea.innerHTML += `<div style="color:#2ecc71">対象となる【味方】を選んでください</div>`;
        enableManualTargeting("ally", skill.id);
    } else {
        // デフォルトは敵単体攻撃
        logArea.innerHTML += `<div style="color:#ff4757">対象となる【敵】を選んでください</div>`;
        enableManualTargeting("enemy", skill.id);
    }
    logArea.scrollTop = logArea.scrollHeight;
}

/**
 * 手動でのターゲットクリックを有効化する
 */
function enableManualTargeting(sideClass, skillId) {
    const units = document.querySelectorAll(`.unit-box.${sideClass}`);
    units.forEach((el, index) => {
        // 死亡していない、または蘇生スキルの場合はクリック可能
        if (!el.classList.contains("dead") || sideClass === "ally") {
            el.style.cursor = "crosshair";
            el.style.filter = "drop-shadow(0 0 15px white) brightness(1.2)";
            
            el.onclick = () => {
                // ターゲット決定。APIへ送信
                executeSkill(skillId, index);
            };
        }
    });
}

/**
 * 項目2: 「ステータス確認」ボタン
 */
function openStatusWindow() {
    if (selectedAllyIndex === null) return;
    const unit = currentContext.playerParty.party[selectedAllyIndex];
    
    // ステータスをアラート形式で詳細表示
    const info = `
【${unit.name} のステータス】
HP: ${unit.currentHp} / ${unit.maxHp}
攻撃力: ${unit.currentAttack}
素早さ: ${unit.currentSpeed}
アーマー: ${unit.currentArmor}
    `;
    alert(info);
}

/**
 * 4. 通信：行動データをサーバーへ送信
 */
async function executeSkill(skillId, targetIndex) {
    try {
        // 操作をロック
        document.getElementById("action-overlay").classList.add("hidden");

        const response = await fetch(`/battle/action?skillId=${skillId}&targetIndex=${targetIndex}`, { 
            method: "POST" 
        });
        currentContext = await response.json();
        
        // 行動完了後は選択状態を解除
        selectedAllyIndex = null;
        selectedSkillId = null;
        
        renderBattle();
    } catch (e) {
        console.error("Action Error:", e);
    }
}

/**
 * 5. リザルト画面表示
 */
function showResultModal() {
    const modal = document.getElementById("result-modal");
    modal.classList.remove("hidden");

    const title = document.getElementById("result-title");
    const body = document.getElementById("result-body");

    if (currentContext.victory) {
        title.innerText = "VICTORY!!";
        title.style.color = "#f1c40f";
        body.innerText = "敵を全滅させた！探索を続けますか？";
        
        if (currentContext.tamed) {
            document.getElementById("recruit-area").classList.remove("hidden");
            document.getElementById("next-floor-btn").classList.add("hidden");
        } else {
            document.getElementById("recruit-area").classList.add("hidden");
            document.getElementById("next-floor-btn").classList.remove("hidden");
        }
    } else {
        title.innerText = "DEFEAT...";
        title.style.color = "#ff4757";
        body.innerText = "味方が全滅してしまった...";
        document.getElementById("title-btn").classList.remove("hidden");
    }
}

/**
 * 仲間にするAPI
 */
async function recruitEnemy() {
    try {
        await fetch("/battle/recruit", { method: "POST" });
        // 仲間にした後は状態が大きく変わるためリロードが安全
        location.reload();
    } catch (e) { console.error(e); }
}

/**
 * 戦闘終了API（次の階層へ）
 */
async function finishBattle() {
    try {
        const response = await fetch("/battle/finish", { method: "POST" });
        const success = await response.json();
        if (success) location.href = "/rest";
    } catch (e) { console.error(e); }
}