#!/bin/bash
# Document Path Validation Tool
# 用途：檢查文檔是否存放在正確的目錄
# 參考：devspec/DIRECTORY_STRUCTURE.md

set -e

FILE_PATH="$1"

if [ -z "$FILE_PATH" ]; then
    echo "Usage: $0 <file_path>"
    echo "Example: $0 docs/plan/v2.10.0_aspect_ratio_L1.md"
    exit 1
fi

# 移除開頭的 ./ 如果有
FILE_PATH="${FILE_PATH#./}"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📋 Document Path Validation"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "File: $FILE_PATH"
echo ""

# 檢測文檔類型並提供建議
SUGGESTION=""
CORRECT_PATH=""
IS_CORRECT=false

# 檢查 L1/L2/Traycer 文檔
if [[ "$FILE_PATH" =~ _L[12]\.md$ ]] || [[ "$FILE_PATH" =~ _Traycer\.md$ ]]; then
    if [[ "$FILE_PATH" =~ ^docs/features/ ]]; then
        IS_CORRECT=true
        echo "✅ 類型：功能實作計劃（L1/L2/Traycer）"
        echo "✅ 路徑：正確 - 存放在 docs/features/"
    elif [[ "$FILE_PATH" =~ ^plans/ ]]; then
        SUGGESTION="L1/L2/Traycer 計劃應存放在 docs/features/<版本>/，不是 plans/"
        if [[ "$FILE_PATH" =~ (v[0-9]+\.[0-9]+\.[0-9]+) ]]; then
            VERSION="${BASH_REMATCH[1]}"
            FILENAME=$(basename "$FILE_PATH")
            CORRECT_PATH="docs/features/$VERSION/$FILENAME"
        fi
    elif [[ "$FILE_PATH" =~ ^docs/plan/ ]]; then
        SUGGESTION="docs/plan/ 已重新命名為 docs/features/"
        if [[ "$FILE_PATH" =~ (v[0-9]+\.[0-9]+\.[0-9]+) ]]; then
            VERSION="${BASH_REMATCH[1]}"
            FILENAME=$(basename "$FILE_PATH")
            CORRECT_PATH="docs/features/$VERSION/$FILENAME"
        else
            # 可能是跨版本專題
            FILENAME=$(basename "$FILE_PATH")
            TOPIC=$(echo "$FILENAME" | sed 's/_L[12]\.md$//' | sed 's/_Traycer\.md$//')
            CORRECT_PATH="docs/features/${TOPIC}/$FILENAME"
        fi
    fi
fi

# 檢查 RELEASE_NOTES 或 PULL_REQUEST
if [[ "$FILE_PATH" =~ RELEASE_NOTES ]] || [[ "$FILE_PATH" =~ PULL_REQUEST ]]; then
    if [[ "$FILE_PATH" =~ ^docs/releases/ ]]; then
        IS_CORRECT=true
        echo "✅ 類型：版本發布記錄"
        echo "✅ 路徑：正確 - 存放在 docs/releases/"
    else
        SUGGESTION="版本發布記錄應存放在 docs/releases/<版本>/"
        if [[ "$FILE_PATH" =~ (v[0-9]+\.[0-9]+\.[0-9]+) ]] || [[ "$FILE_PATH" =~ (V[0-9]+\.[0-9]+\.[0-9]+) ]]; then
            VERSION=$(echo "${BASH_REMATCH[1]}" | tr '[:upper:]' '[:lower:]')
            FILENAME=$(basename "$FILE_PATH")
            CORRECT_PATH="docs/releases/$VERSION/$FILENAME"
        fi
    fi
fi

# 檢查 TEST_REPORT
if [[ "$FILE_PATH" =~ TEST_REPORT ]]; then
    if [[ "$FILE_PATH" =~ ^docs/test_results/ ]]; then
        IS_CORRECT=true
        echo "✅ 類型：測試報告"
        echo "✅ 路徑：正確 - 存放在 docs/test_results/"
    elif [[ "$FILE_PATH" =~ ^docs/features/ ]]; then
        SUGGESTION="測試報告應存放在 docs/test_results/<版本>/，不是 docs/features/"
        if [[ "$FILE_PATH" =~ (v[0-9]+\.[0-9]+\.[0-9]+) ]]; then
            VERSION="${BASH_REMATCH[1]}"
            FILENAME=$(basename "$FILE_PATH")
            CORRECT_PATH="docs/test_results/$VERSION/$FILENAME"
        fi
    fi
fi

# 檢查 GIT_ 開頭的文檔
if [[ "$FILE_PATH" =~ GIT_ ]]; then
    if [[ "$FILE_PATH" =~ ^docs/git/ ]]; then
        IS_CORRECT=true
        echo "✅ 類型：Git 管理文檔"
        echo "✅ 路徑：正確 - 存放在 docs/git/"
    else
        SUGGESTION="Git 管理文檔應存放在 docs/git/"
        FILENAME=$(basename "$FILE_PATH")
        CORRECT_PATH="docs/git/$FILENAME"
    fi
fi

# 檢查專案計劃（roadmap, blueprint 等）
if [[ "$FILE_PATH" =~ roadmap ]] || [[ "$FILE_PATH" =~ BLUEPRINT ]] || [[ "$FILE_PATH" =~ 專項開發計畫 ]]; then
    if [[ "$FILE_PATH" =~ ^plans/ ]]; then
        IS_CORRECT=true
        echo "✅ 類型：專案計劃/路線圖"
        echo "✅ 路徑：正確 - 存放在 plans/"
    else
        SUGGESTION="專案計劃應存放在 plans/roadmap/ 或 plans/architecture/"
        FILENAME=$(basename "$FILE_PATH")
        if [[ "$FILENAME" =~ roadmap ]]; then
            CORRECT_PATH="plans/roadmap/$FILENAME"
        else
            CORRECT_PATH="plans/architecture/$FILENAME"
        fi
    fi
fi

# 輸出結果
echo ""
if [ "$IS_CORRECT" = true ]; then
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "✅ 路徑驗證通過"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    exit 0
else
    echo "⚠️  類型：需要檢查"
    echo "⚠️  問題：$SUGGESTION"
    if [ -n "$CORRECT_PATH" ]; then
        echo ""
        echo "建議路徑："
        echo "  $CORRECT_PATH"
        echo ""
        echo "遷移指令："
        echo "  mkdir -p $(dirname "$CORRECT_PATH")"
        echo "  git mv \"$FILE_PATH\" \"$CORRECT_PATH\""
    fi
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "📖 請參考：devspec/DIRECTORY_STRUCTURE.md"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    exit 1
fi
