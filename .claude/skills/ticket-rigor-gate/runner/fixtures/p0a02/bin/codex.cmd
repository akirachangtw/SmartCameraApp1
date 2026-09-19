@echo off
rem RGR-P0A-02 AI-3: Windows dispatch shim. Windows' CreateProcess routes any
rem *.cmd file through cmd.exe regardless of shebang content, so the actual
rem deterministic stub logic lives in codex_stub_impl.sh (POSIX shell) and is
rem re-dispatched here via bash. Never invokes real Codex.
bash "%~dp0codex_stub_impl.sh" %*
exit /b %ERRORLEVEL%
