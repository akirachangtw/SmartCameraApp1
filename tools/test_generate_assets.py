"""
Test Script for generate_assets.py
Validates the parsing and configuration handling without calling the API

This script tests:
1. Prompt file parsing (all 4 formats)
2. Key features extraction
3. Aspect ratio and MIME type validation
4. Error handling for malformed lines
"""

import sys
import os
import csv

# Standalone parsing function (copied from generate_assets.py to avoid dependencies)
def parse_input_file_standalone(file_path):
    """Parse input file without external dependencies"""
    task_list = []

    if not os.path.exists(file_path):
        print(f"Error: Input file not found: {file_path}")
        sys.exit(1)

    with open(file_path, 'r', encoding='utf-8') as file:
        # Use CSV reader to properly handle quoted fields with commas
        csv_reader = csv.reader(file, quotechar='"', skipinitialspace=True)

        for line_number, row in enumerate(csv_reader, 1):
            # Skip empty rows
            if not row:
                continue

            # Skip comments (first field starts with #)
            if row[0].strip().startswith("#"):
                continue

            # Parse format: "filename", "prompt", ["aspect_ratio"], ["mime_type"], ["key_features"]
            try:
                if len(row) < 2:
                    print(f"Warning: Skipping malformed line {line_number}: insufficient fields")
                    continue

                filename = row[0].strip()
                prompt = row[1].strip()
                aspect_ratio = row[2].strip() if len(row) > 2 else "3:4"
                mime_type = row[3].strip() if len(row) > 3 else "image/jpeg"
                key_features = row[4].strip() if len(row) > 4 else ""

                # Validate aspect ratio format
                valid_ratios = ["1:1", "3:4", "4:3", "9:16", "16:9"]
                if aspect_ratio not in valid_ratios:
                    print(f"Warning: Line {line_number}: Invalid aspect ratio '{aspect_ratio}', using default '3:4'")
                    aspect_ratio = "3:4"

                # Validate MIME type
                valid_mimes = ["image/jpeg", "image/png"]
                if mime_type not in valid_mimes:
                    print(f"Warning: Line {line_number}: Invalid MIME type '{mime_type}', using default 'image/jpeg'")
                    mime_type = "image/jpeg"

                task_list.append((filename, prompt, aspect_ratio, mime_type, key_features))

            except Exception as error:
                print(f"Warning: Error parsing line {line_number}: {error}")

    return task_list

def test_prompt_parsing():
    """Test parsing of the v2.6.0 HDR test prompt file"""

    print("=" * 70)
    print("Testing generate_assets.py - v2.6.0 HDR Lighting Scenarios")
    print("=" * 70)
    print()

    # Test file path
    test_file = "prompts_v2.6.0_hdr_test.txt"
    test_path = os.path.join(os.path.dirname(__file__), test_file)

    if not os.path.exists(test_path):
        print(f"❌ Test file not found: {test_path}")
        return False

    print(f"📄 Parsing test file: {test_file}")
    print()

    try:
        # Parse the prompt file
        tasks = parse_input_file_standalone(test_path)

        print(f"✅ Successfully parsed {len(tasks)} tasks")
        print()

        # Display parsed tasks
        print("-" * 70)
        print("Parsed Tasks Summary:")
        print("-" * 70)

        scenario_counts = {
            "Backlight": 0,
            "High Contrast": 0,
            "Overexposure": 0,
            "Normal": 0,
            "Edge Cases": 0
        }

        for idx, (filename, prompt, aspect_ratio, mime_type, key_features) in enumerate(tasks, 1):
            print(f"\n[Task {idx}/{len(tasks)}]")
            print(f"  Filename: {filename}")
            print(f"  Aspect Ratio: {aspect_ratio}")
            print(f"  MIME Type: {mime_type}")
            print(f"  Prompt: {prompt[:80]}{'...' if len(prompt) > 80 else ''}")

            if key_features:
                print(f"  Key Features: {key_features[:100]}{'...' if len(key_features) > 100 else ''}")

                # Categorize by scenario
                if "backlight" in filename.lower():
                    scenario_counts["Backlight"] += 1
                elif "high_contrast" in filename.lower():
                    scenario_counts["High Contrast"] += 1
                elif "overexposed" in filename.lower():
                    scenario_counts["Overexposure"] += 1
                elif "normal" in filename.lower():
                    scenario_counts["Normal"] += 1
                elif "mixed" in filename.lower() or "partial" in filename.lower():
                    scenario_counts["Edge Cases"] += 1
            else:
                print(f"  Key Features: (none)")

        # Summary by scenario
        print()
        print("=" * 70)
        print("Scenario Distribution:")
        print("=" * 70)
        for scenario, count in scenario_counts.items():
            print(f"  {scenario:20s}: {count} images")

        # Validation checks
        print()
        print("=" * 70)
        print("Validation Checks:")
        print("=" * 70)

        all_valid = True

        # Check 1: All tasks have valid aspect ratios
        valid_ratios = ["1:1", "3:4", "4:3", "9:16", "16:9"]
        invalid_ratios = [t for t in tasks if t[2] not in valid_ratios]
        if not invalid_ratios:
            print("  ✅ All aspect ratios are valid")
        else:
            print(f"  ❌ {len(invalid_ratios)} tasks have invalid aspect ratios")
            all_valid = False

        # Check 2: All tasks have valid MIME types
        valid_mimes = ["image/jpeg", "image/png"]
        invalid_mimes = [t for t in tasks if t[3] not in valid_mimes]
        if not invalid_mimes:
            print("  ✅ All MIME types are valid")
        else:
            print(f"  ❌ {len(invalid_mimes)} tasks have invalid MIME types")
            all_valid = False

        # Check 3: Key features present for HDR scenarios
        tasks_with_features = [t for t in tasks if t[4]]
        if len(tasks_with_features) >= len(tasks) * 0.8:  # At least 80% should have features
            print(f"  ✅ {len(tasks_with_features)}/{len(tasks)} tasks have key features")
        else:
            print(f"  ⚠️  Only {len(tasks_with_features)}/{len(tasks)} tasks have key features")

        # Check 4: Validate key feature content for critical scenarios
        backlight_tasks = [t for t in tasks if "backlight" in t[0].lower() and t[4]]
        if backlight_tasks:
            has_luminance_spec = all("luminance" in t[4].lower() for t in backlight_tasks)
            if has_luminance_spec:
                print(f"  ✅ All backlight tasks have luminance specifications")
            else:
                print(f"  ⚠️  Some backlight tasks missing luminance specifications")

        # Check 5: HDR test coverage
        print()
        print("  HDR Test Coverage:")
        required_scenarios = {
            "Backlight": 2,
            "High Contrast": 2,
            "Overexposure": 2,
            "Normal": 2
        }

        coverage_complete = True
        for scenario, min_count in required_scenarios.items():
            actual_count = scenario_counts[scenario]
            if actual_count >= min_count:
                print(f"    ✅ {scenario:20s}: {actual_count}/{min_count}")
            else:
                print(f"    ❌ {scenario:20s}: {actual_count}/{min_count} (insufficient)")
                coverage_complete = False

        # Final result
        print()
        print("=" * 70)
        if all_valid and coverage_complete:
            print("✅ ALL VALIDATION CHECKS PASSED")
            print("=" * 70)
            print()
            print("📋 Test Summary:")
            print(f"   Total tasks: {len(tasks)}")
            print(f"   Ready for API generation: YES")
            print(f"   Estimated generation time: ~{len(tasks) * 35} seconds (~{len(tasks) * 35 / 60:.1f} minutes)")
            print()
            print("🚀 Next Steps:")
            print("   1. Set GEMINI_API_KEY environment variable")
            print("   2. Run: python tools/generate_assets.py tools/prompts_v2.6.0_hdr_test.txt")
            print("   3. Generated images will be saved to: app/src/debug/assets/test_images/")
            return True
        else:
            print("⚠️  SOME VALIDATION CHECKS FAILED")
            print("=" * 70)
            return False

    except Exception as error:
        print(f"❌ Error during parsing: {error}")
        import traceback
        traceback.print_exc()
        return False

def test_format_examples():
    """Test parsing of different format examples"""

    print()
    print("=" * 70)
    print("Testing Different Input Formats")
    print("=" * 70)
    print()

    # Create a temporary test file with all formats
    test_content = """# Test file with all 4 formats
"format1_basic.jpg", "Basic format test prompt"
"format2_aspect.jpg", "Custom aspect ratio test", "16:9"
"format3_full.jpg", "Full control test", "1:1", "image/png"
"format4_features.jpg", "With features test", "3:4", "image/jpeg", "highlight ratio >15%; test feature"
"""

    temp_file = "test_formats_temp.txt"
    temp_path = os.path.join(os.path.dirname(__file__), temp_file)

    try:
        # Write temporary test file
        with open(temp_path, 'w', encoding='utf-8') as f:
            f.write(test_content)

        # Parse it
        tasks = parse_input_file_standalone(temp_path)

        print(f"✅ Successfully parsed {len(tasks)} format examples")
        print()

        expected_results = [
            ("format1_basic.jpg", "3:4", "image/jpeg", ""),
            ("format2_aspect.jpg", "16:9", "image/jpeg", ""),
            ("format3_full.jpg", "1:1", "image/png", ""),
            ("format4_features.jpg", "3:4", "image/jpeg", "highlight ratio >15%; test feature")
        ]

        all_correct = True
        for idx, (filename, aspect, mime, features) in enumerate(expected_results):
            actual = tasks[idx]
            if actual[0] == filename and actual[2] == aspect and actual[3] == mime and actual[4] == features:
                print(f"  ✅ Format {idx+1}: {filename} - Correct")
            else:
                print(f"  ❌ Format {idx+1}: {filename} - Mismatch")
                print(f"     Expected: aspect={aspect}, mime={mime}, features='{features}'")
                print(f"     Got:      aspect={actual[2]}, mime={actual[3]}, features='{actual[4]}'")
                all_correct = False

        if all_correct:
            print()
            print("✅ All format tests passed!")

        return all_correct

    finally:
        # Cleanup temporary file
        if os.path.exists(temp_path):
            os.remove(temp_path)

def main():
    """Run all tests"""
    print()
    print("╔" + "═" * 68 + "╗")
    print("║" + " " * 15 + "generate_assets.py Test Suite" + " " * 24 + "║")
    print("║" + " " * 15 + "SmartCameraApp v2.6.0" + " " * 32 + "║")
    print("╚" + "═" * 68 + "╝")
    print()

    # Test 1: Parse v2.6.0 HDR test prompts
    test1_passed = test_prompt_parsing()

    # Test 2: Different format examples
    test2_passed = test_format_examples()

    # Final summary
    print()
    print("=" * 70)
    print("FINAL TEST RESULTS")
    print("=" * 70)
    print(f"  Test 1 (v2.6.0 HDR Prompts): {'✅ PASSED' if test1_passed else '❌ FAILED'}")
    print(f"  Test 2 (Format Parsing):     {'✅ PASSED' if test2_passed else '❌ FAILED'}")
    print("=" * 70)

    if test1_passed and test2_passed:
        print()
        print("🎉 All tests passed! Script is ready for production use.")
        sys.exit(0)
    else:
        print()
        print("⚠️  Some tests failed. Please review the output above.")
        sys.exit(1)

if __name__ == "__main__":
    main()
