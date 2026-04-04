# ============================================
#  Git Auto-Push Script for BridgeExp
# ============================================
#  Usage:
#    .\git-push.ps1                 -> prompts for commit message
#    .\git-push.ps1 "your message"  -> uses provided message
# ============================================

param(
    [string]$CommitMessage
)

# --- Helpers ---
function Write-Step {
    param([string]$msg)
    Write-Host ""
    Write-Host "  >> $msg" -ForegroundColor Cyan
}

function Write-Success {
    param([string]$msg)
    Write-Host "  [OK] $msg" -ForegroundColor Green
}

function Write-Err {
    param([string]$msg)
    Write-Host "  [ERROR] $msg" -ForegroundColor Red
}

# --- Pre-flight: make sure we are in a git repo ---
if (-not (Test-Path ".git")) {
    Write-Err "Not a git repository. Run this script from your project root."
    exit 1
}

# --- Show current status ---
Write-Step "Current changes:"
git status --short
Write-Host ""

# --- Check if there is anything to commit ---
$status = git status --porcelain
if (-not $status) {
    Write-Host "  [INFO] Nothing to commit - working tree clean." -ForegroundColor Yellow
    exit 0
}

# --- Get commit message ---
if (-not $CommitMessage) {
    $CommitMessage = Read-Host "  Enter commit message"
    if (-not $CommitMessage) {
        Write-Err "Commit message cannot be empty. Aborting."
        exit 1
    }
}

# --- Stage all changes ---
Write-Step "Staging all changes..."
git add .
if ($LASTEXITCODE -ne 0) {
    Write-Err "git add failed."
    exit 1
}
Write-Success "All changes staged."

# --- Commit ---
Write-Step "Committing with message: $CommitMessage"
git commit -m $CommitMessage
if ($LASTEXITCODE -ne 0) {
    Write-Err "git commit failed."
    exit 1
}
Write-Success "Commit created."

# --- Get current branch ---
$branch = git branch --show-current
Write-Step "Current branch: $branch"

# --- Push ---
Write-Step "Pushing to origin/$branch..."
git push -u origin $branch
if ($LASTEXITCODE -ne 0) {
    Write-Err "Push failed. Try: git pull --rebase origin $branch"
    exit 1
}

Write-Host ""
Write-Success "All done! Changes pushed to origin/$branch."
Write-Host ""
