/**
 * 魔物使いのダンジョン - 戦闘メインロジック (battle.js)
 */

// --- グローバル変数 ---
let currentContext = null;      // サーバーから受け取った戦闘データ
let selectedAllyIndex = null;   // コマンド入力中の味方番号
let selectedSkillId = null;     // 選択された技のID

// メッセージ送り用
let messageQueue = [];          // 表示待ちログのリスト
let isDisplayingMessages = false; // メッセージ再生中フラグ

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
        
        // 背景やユニットの初期描画
        renderStaticParts();
        
        // 最初のログをキューに入れて表示開始
        enqueueMessages(currentContext.logs);
        
    } catch (e) {
        console.error(e);
        document.getElementById("log-message-area").innerText = "Error: 戦闘を開始できませんでした。";
    }
}

/**
 * 2. 画面描画 (Render)
 */

/**
 * 背景やユニットの配置を描画
 */
function renderStaticParts() {
    if (!currentContext) return;
    updateBackground();
    renderUnits();
}

/**
 * 背景切り替え (2層ごとに1枚)
 */
function updateBackground() {
    const screen = document.getElementById("battle-screen");
    const floor = currentContext.currentFloor || 1;
    const bgNum = Math.ceil(floor / 2);
    screen.style.backgroundImage = `url('/images/floor/floor${bgNum}.png')`;
    screen.style.backgroundSize = "cover";
}

/**
 * モンスターの描画
 */
function renderUnits() {
    // 敵エリア
    const enemyArea = document.getElementById("enemy-area");
    enemyArea.innerHTML = "";
    currentContext.enemyParty.party.forEach((unit, index) => {
        enemyArea.appendChild(createUnitBox(unit, index, true));
    });

    // 味方エリア
    const allyArea = document.getElementById("ally-area");
    allyArea.innerHTML = "";
    currentContext.playerParty.party.forEach((unit, index) => {
        const allyBox = createUnitBox(unit, index, false);
        
        if (unit.alive && !isDisplayingMessages) {
            allyBox.onclick = () => {
                selectedAllyIndex = index;
                selectedSkillId = null;
                renderBattleUI();
            };
        }
        
        if (selectedAllyIndex === index) {
            allyBox.classList.add("selected-unit");
        }
        allyArea.appendChild(allyBox);
    });
}

/**
 * コマンドボタンやモーダルの制御
 */
function renderBattleUI() {
    renderUnits();

    const overlay = document.getElementById("action-overlay");
    
    if (currentContext.battleOver || isDisplayingMessages) {
        overlay.classList.add("hidden");
        if (currentContext.battleOver && !isDisplayingMessages) {
            showResultModal();
        }
    } else if (selectedAllyIndex !== null) {
        overlay.classList.remove("hidden");
    } else {
        overlay.classList.add("hidden");
    }
}

/**
 * ユニットボックス生成 (アーマーバー対応)
 */
function createUnitBox(unit, index, isEnemy) {
    const div = document.createElement("div");
    div.className = `unit-box ${isEnemy ? 'enemy' : 'ally'} ${unit.alive ? '' : 'dead'}`;
    
    // HPの割合
    const hpPercent = (unit.currentHp / unit.maxHp) * 100;
    
    // アーマーの計算
    const hasArmor = unit.maxArmor > 0;
    const armorPercent = hasArmor ? (unit.currentArmor / unit.maxArmor) * 100 : 0;
    
    const imgUrl = unit.imageUrl || '/images/monsters/bomb.png';

    div.innerHTML = `
        <div class="unit-name">${unit.name}</div>
        <div class="img-container">
            <img src="${imgUrl}" class="unit-img" onerror="this.onerror=null;this.src='/images/monsters/bomb.png';">
        </div>
        
        <div class="armor-bar-container" style="${hasArmor ? '' : 'display:none;'}">
            <div class="armor-bar-fill" style="width: ${armorPercent}%"></div>
        </div>

        <div class="hp-bar-container">
            <div class="hp-bar-fill" style="width: ${hpPercent}%"></div>
        </div>
        <div class="hp-text">
            ${unit.currentHp} / ${unit.maxHp}
            ${hasArmor ? ' <span class="armor-text">(🛡️' + unit.currentArmor + ')</span>' : ''}
        </div>
    `;
    return div;
}

/**
 * 3. メッセージ送りロジック
 */

function enqueueMessages(logs) {
    if (!logs || logs.length === 0) {
        finishMessageDisplay();
        return;
    }
    messageQueue = [...logs];
    isDisplayingMessages = true;
    document.getElementById("action-overlay").classList.add("hidden");
    showNextMessage();
}

function onMessageClick() {
    if (isDisplayingMessages) {
        showNextMessage();
    }
}

function showNextMessage() {
    if (messageQueue.length === 0) {
        finishMessageDisplay();
        return;
    }

    const msg = messageQueue.shift();
    const area = document.getElementById("log-message-area");
    const indicator = document.getElementById("message-next-indicator");

    area.innerText = msg;
    indicator.classList.remove("hidden");
}

function finishMessageDisplay() {
    isDisplayingMessages = false;
    document.getElementById("message-next-indicator").classList.add("hidden");
    renderBattleUI();
}

/**
 * 4. コマンドアクション
 */

function openSkillWindow() {
    if (selectedAllyIndex === null || isDisplayingMessages) return;
    
    const skillList = document.getElementById("skill-list");
    skillList.innerHTML = "";
    const actor = currentContext.playerParty.party[selectedAllyIndex];
    document.getElementById("skill-window-title").innerText = `${actor.name} の技`;

    actor.skills.forEach(skill => {
        const btn = document.createElement("button");
        btn.className = "skill-btn";
        const uses = skill.skillMaster.maxUses === -1 ? "∞" : `${skill.remainingUses}/${skill.skillMaster.maxUses}`;
        btn.innerHTML = `<div class="s-name">${skill.skillMaster.name}</div><div class="s-uses">回数: ${uses}</div>`;

        if (skill.remainingUses !== 0) {
            btn.onclick = () => {
                selectedSkillId = skill.skillMaster.id;
                closeSkillWindow();
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

function handleTargeting(skill) {
    const type = skill.targetType;
    if (type === "SELF") {
        executeSkill(skill.id, selectedAllyIndex);
    } else if (type === "ENEMY_ALL" || type === "ALLY_ALL") {
        executeSkill(skill.id, 0); 
    } else if (type === "ALLY" || type === "ALLY_DEAD") {
        enqueueMessages(["対象の味方を選んでください"]);
        enableManualTargeting("ally", skill.id);
    } else {
        enqueueMessages(["対象の敵を選んでください"]);
        enableManualTargeting("enemy", skill.id);
    }
}

function enableManualTargeting(sideClass, skillId) {
    const units = document.querySelectorAll(`.unit-box.${sideClass}`);
    units.forEach((el, index) => {
        if (!el.classList.contains("dead") || sideClass === "ally") {
            el.style.cursor = "crosshair";
            el.style.filter = "drop-shadow(0 0 15px white) brightness(1.2)";
            el.onclick = () => executeSkill(skillId, index);
        }
    });
}

function openStatusWindow() {
    if (selectedAllyIndex === null) return;
    const unit = currentContext.playerParty.party[selectedAllyIndex];
    alert(`【ステータス】\n${unit.name}\nHP: ${unit.currentHp}/${unit.maxHp}\nATK: ${unit.currentAttack}\nSPD: ${unit.currentSpeed}\nARM: ${unit.currentArmor}`);
}

/**
 * ターン実行
 */
async function executeSkill(skillId, targetIndex) {
    try {
        document.getElementById("action-overlay").classList.add("hidden");
        selectedAllyIndex = null;

        const response = await fetch(`/battle/action?skillId=${skillId}&targetIndex=${targetIndex}`, { method: "POST" });
        currentContext = await response.json();
        
        renderStaticParts();
        enqueueMessages(currentContext.logs);

    } catch (e) {
        console.error("Action Error:", e);
    }
}

/**
 * 5. リザルト・終了処理
 */
function showResultModal() {
    const modal = document.getElementById("result-modal");
    modal.classList.remove("hidden");
    const title = document.getElementById("result-title");
    const body = document.getElementById("result-body");

    if (currentContext.victory) {
        title.innerText = "VICTORY!!";
        title.style.color = "#f1c40f";
        body.innerText = "敵を全滅させた！";
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

async function recruitEnemy() {
    try {
        await fetch("/battle/recruit", { method: "POST" });
        location.reload();
    } catch (e) { console.error(e); }
}

async function finishBattle() {
    try {
        const response = await fetch("/battle/finish", { method: "POST" });
        const success = await response.json();
        if (success) location.href = "/rest";
    } catch (e) { console.error(e); }
}