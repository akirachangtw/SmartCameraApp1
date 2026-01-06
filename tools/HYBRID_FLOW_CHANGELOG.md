# Hybrid Flow - Change Log

## Version History

### v1.1.0 (2026-01-04) - Path Normalization Update ⭐

**Enhancement**: Windows Path Support

**Changes**:
- ✅ Added automatic path separator normalization for `--issue` parameter
- ✅ Added automatic path separator normalization for `--input_file` parameter
- ✅ Now supports both Windows (`\`) and Unix (`/`) path separators
- ✅ Added fallback to check paths relative to repository root
- ✅ Updated documentation with path format examples

**Benefits**:
- Users can copy-paste Windows paths directly without manual conversion
- Consistent behavior across Windows and Unix systems
- Improved user experience for Windows developers

**Example**:
```bash
# Before (required manual conversion)
python tools/hybrid_flow.py --level L2 --issue "docs/plan/file_L1.md"

# After (both formats work)
python tools/hybrid_flow.py --level L2 --issue "docs/plan/file_L1.md"
python tools/hybrid_flow.py --level L2 --issue "docs\plan\file_L1.md"  ✅ NEW
```

**Files Modified**:
- `tools/hybrid_flow.py` (Lines 137-169)
- `tools/HYBRID_FLOW_CLAUDE_USAGE.md` (Added path format section)
- `docs/VSCODE_TASKS_INTEGRATION.md` (Updated parameter descriptions)

**Backward Compatibility**: ✅ Fully backward compatible (existing commands continue to work)

---

### v1.0.0 (2026-01-04) - Initial Integration

**Features**:
- Claude Code integration with permissions
- VSCode Tasks integration
- L1/L2/PR document generation
- Git context collection
- Multiple input methods (inline, file, stdin)

**Documentation**:
- `tools/HYBRID_FLOW_CLAUDE_USAGE.md` - User guide
- `docs/VSCODE_TASKS_INTEGRATION.md` - VSCode integration guide
- `docs/CRITICAL_FILES_PROTECTION.md` - File protection guide

**Integration Points**:
- `.claude/settings.local.json` - Claude Code permissions
- `.vscode/tasks.json` - VSCode Tasks (14 total tasks)
- `tools/hybrid_flow-tasks.json` - Reference configuration

---

## Upgrade Guide

### From v1.0.0 to v1.1.0

**No action required** - The update is fully backward compatible.

**Optional**: Update existing scripts/documentation to use Windows paths if preferred.

---

## Future Roadmap

### Planned Features
- [ ] Add validation for file existence before prompting API
- [ ] Support for multiple input files
- [ ] Template-based output formatting
- [ ] Integration with git commit message hooks
- [ ] Automatic PR creation from PR notes

### Under Consideration
- [ ] GUI wrapper for easier parameter input
- [ ] Output format options (Markdown, PDF, HTML)
- [ ] Multi-language support
- [ ] Integration with project management tools

---

## Known Issues

### Current Limitations
- Requires `OPENAI_API_KEY` environment variable (OpenAI API only)
- Maximum input size: 60,000 characters (truncated if larger)
- Git diff truncated at 30,000 characters

### Workarounds
- For large inputs: Use `--input_file` instead of `--input`
- For very large diffs: Consider using `--no_git` flag

---

## Support & Contributions

### Bug Reports
- File issues with detailed reproduction steps
- Include Python version, OS, and error messages
- Check existing documentation first

### Feature Requests
- Describe use case and expected behavior
- Provide examples of desired output
- Consider backward compatibility impact

---

**Maintainer**: SmartCameraApp1 Development Team
**Last Updated**: 2026-01-04
**Current Version**: v1.1.0
