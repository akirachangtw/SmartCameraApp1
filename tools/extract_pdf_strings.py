
import sys
import re

def extract_strings(filename, min_len=10):  # Increased min_len to reduce noise
    try:
        with open(filename, "rb") as f:
            data = f.read()
            pass
    except FileNotFoundError:
        print(f"Error: File not found: {filename}")
        return

    # Regex to find sequences of min_len+ printable ASCII chars
    # PDF text is often in parentheses (...) or streams, but raw strings helps.
    # We look for standard ASCII printable range.
    pattern = b"[\x20-\x7E]{%d,}" % min_len
    text = re.findall(pattern, data)
    
    print(f"--- Extracted Strings from {filename} ---")
    for s in text:
        try:
            decoded = s.decode('ascii')
            # Filter out common PDF structure keywords to reduce noise if possible, 
            # but for now just print everything.
            print(decoded)
        except:
            pass
    print("--- End of Extraction ---")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python extract_pdf_strings.py <pdf_file>")
        sys.exit(1)
    extract_strings(sys.argv[1])
