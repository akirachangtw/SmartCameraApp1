# Critical Files Protection - SmartCameraApp1

**Last Updated**: 2026-01-04
**Status**: ✅ Protection Measures Activated

---

## 🔴 Critical Files List

### 1. Workflow Automation

| File | Location | Purpose | Integration |
|------|----------|---------|-------------|
| `hybrid_flow.py` | `tools/` | L1/L2/PR planning automation | Claude Code, VSCode Tasks |
| `hybrid_flow-tasks.json` | `tools/` | VSCode Tasks config | VSCode Task Runner |
| `HYBRID_FLOW_CLAUDE_USAGE.md` | `tools/` | Usage documentation | Developer reference |

### 2. Claude Code Integration

| File | Location | Purpose | Integration |
|------|----------|---------|-------------|
| `settings.local.json` | `.claude/` | Bash command permissions | Claude Code workflow |
| `CLAUDE.md` | `.claude/` | Project instructions | Claude Code context |

### 3. Asset Generation

| File | Location | Purpose | Integration |
|------|----------|---------|-------------|
| `generate_assets.py` | `tools/` | Test image generation | Google Gemini API |
| `test_generate_assets.py` | `tools/` | Asset generation tests | Test suite |

---

## 🛡️ Protection Measures Implemented

### ✅ 1. File Header Warnings

**File**: `tools/hybrid_flow.py` (Lines 5-7)
```python
⚠️  CRITICAL FILE - DO NOT DELETE ⚠️
This file is integrated with Claude Code workflow and VSCode Tasks.
Deletion will break the development planning process.
```

### ✅ 2. Tools README Warning

**File**: `tools/README.md` (Lines 5-20)
- Dedicated "Critical Files" section at the top
- 🔴 Warning emoji for visibility
- Lists integration dependencies
- Links to usage documentation

### ✅ 3. Protection Marker File

**File**: `tools/.gitkeep_critical_files`
- Comprehensive protection checklist
- Deletion impact analysis
- Pre-cleanup verification steps

### ✅ 4. .gitignore Comments

**File**: `.gitignore` (Lines 48-53)
- Warning against adding critical files to ignore
- Lists protected files
- References protection documentation

### ✅ 5. Project Documentation

**File**: `.claude/CLAUDE.md` (Lines 22-35)
- Critical files section in project overview
- Visible to Claude Code in every conversation
- Links to detailed protection docs

---

## ❌ Deletion Impact Analysis

### If `hybrid_flow.py` is deleted:

**Immediate Breakage**:
- ❌ Claude Code cannot execute L1/L2/PR commands
- ❌ VSCode Tasks fail with "file not found"
- ❌ `.claude/settings.local.json` permissions become invalid

**Workflow Impact**:
- ❌ No automated planning document generation
- ❌ Manual recreation of L1/L2/PR docs required
- ❌ Loss of ChatGPT + Claude Code workflow integration

**Recovery Cost**:
- ⏱️ 2-4 hours to recreate from backup/git history
- ⏱️ 1-2 hours to reconfigure integrations
- ⏱️ Risk of version mismatch if restored from old backup

### If `hybrid_flow-tasks.json` is deleted:

**Immediate Breakage**:
- ❌ VSCode Task Runner cannot find tasks
- ❌ "Run Task" menu shows no hybrid flow options

**Workflow Impact**:
- ❌ GUI-based task execution unavailable
- ⚠️ Can still use command-line, but less convenient

**Recovery Cost**:
- ⏱️ 30-60 minutes to recreate configuration

---

## ✅ Pre-Deletion Checklist

Before deleting **ANY** file in `tools/`, verify:

- [ ] File is NOT listed in this document's Critical Files List
- [ ] File is NOT imported by any Python script (`grep -r "import.*filename"`)
- [ ] File is NOT referenced in `.claude/settings.local.json`
- [ ] File is NOT referenced in `hybrid_flow-tasks.json`
- [ ] File is NOT referenced in project documentation
- [ ] File has NOT been used in recent development sessions
- [ ] File is NOT part of a versioned workflow

**If uncertain**: Ask in team chat or check git history first!

---

## 🔄 Recovery Procedures

### If Critical File Was Accidentally Deleted

#### Option 1: Git Restore (Recommended)
```bash
git restore tools/hybrid_flow.py
git restore tools/hybrid_flow-tasks.json
```

#### Option 2: Git History Recovery
```bash
# Find the commit that deleted the file
git log --all --full-history -- tools/hybrid_flow.py

# Restore from specific commit
git checkout <commit-hash>~1 -- tools/hybrid_flow.py
```

#### Option 3: Backup Restore
```bash
# Check backup directory (if configured)
cp backup/tools/hybrid_flow.py tools/
```

### Verification After Restore

1. **Test hybrid_flow.py**:
   ```bash
   python tools/hybrid_flow.py --help
   ```

2. **Verify VSCode Tasks**:
   - Press `Ctrl+Shift+P`
   - Type "Tasks: Run Task"
   - Confirm "Hybrid: L1/L2/PR" tasks appear

3. **Test Claude Code Integration**:
   - Ask Claude to execute: `python tools/hybrid_flow.py --level L1 --topic test --input "test" --out docs/plan --repo "."`
   - Verify no permission errors

---

## 📊 Protection Status Summary

| Protection Layer | Status | File |
|------------------|--------|------|
| File Header Warning | ✅ Active | `tools/hybrid_flow.py` |
| README Warning | ✅ Active | `tools/README.md` |
| Protection Marker | ✅ Active | `tools/.gitkeep_critical_files` |
| .gitignore Comment | ✅ Active | `.gitignore` |
| Project Docs | ✅ Active | `.claude/CLAUDE.md` |
| This Document | ✅ Active | `docs/CRITICAL_FILES_PROTECTION.md` |

**Overall Protection Level**: 🟢 **STRONG** (6/6 layers active)

---

## 📞 Contacts

**Questions about file deletion?**
- Check this document first
- Review `tools/.gitkeep_critical_files`
- Ask in development team chat

**Found a bug in protection system?**
- Update this document
- Commit changes with `[Docs]` prefix

---

## 📝 Maintenance Notes

**This document should be updated when**:
- New critical files are added to the project
- File locations change
- New integration dependencies are introduced
- Protection measures are enhanced

**Review Schedule**: Quarterly or after major refactors

---

**Document Version**: 1.0
**Created**: 2026-01-04
**Maintainer**: SmartCameraApp1 Development Team
