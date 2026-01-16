# Test Results Directory

This directory contains manual test results from different devices and environments.

## Naming Convention

**Format**: `v[VERSION]_[DEVICE]_[DATE].md`

**Examples**:
- `v2.9.6_phase1_pixel9a.md` - Pixel 9a test results for v2.9.6 Phase 1
- `v2.9.6_phase1_sony_xq-bt52.md` - Sony XQ-BT52 test results
- `v2.9.6_phase2_emulator.md` - Emulator test results for Phase 2

## File Structure

Each test result file should include:

1. **Header**: Version, device, tester, date
2. **Test Phases**: Initialization, functionality, photo orientation
3. **Results Table**: Pass/fail status for each test case
4. **Issues**: Critical and minor issues
5. **Logcat Snapshots**: Key logs
6. **Recommendations**: Next steps based on results

## Template

Use the template from [MANUAL_TEST_HANDOFF.md](../MANUAL_TEST_HANDOFF.md#test-results-template)

## Current Test Results

<!-- Update this list as new test results are added -->

### v2.9.6 Phase 1
- [ ] Pixel 9a (Pending)
- [x] Sony XQ-BT52 (Partial - initialization only)
- [x] Emulator Medium_Phone_API_36.1 (Basic verification)

---

**Last Updated**: 2026-01-16
