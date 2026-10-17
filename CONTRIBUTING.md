# Contributing to A1 Swaara

Thank you for your interest in contributing to **A1 Swaara**! A1 Swaara is an open-source, sovereign high-fidelity music streaming suite engineered by **@varshuai** under the **Velorio Labs** ecosystem.

We welcome contributions from developers, designers, translators, and audio enthusiasts. Please take a moment to review this guideline before submitting issues or pull requests.

---

## 1. Code of Conduct

This project adheres to the Contributor Covenant Code of Conduct. By participating in this project, you agree to abide by its terms. Please treat everyone with respect, kindness, and professionalism.

---

## 2. General Principles & Constraints

When developing for A1 Swaara, adhere strictly to the following principles:

1. **Zero-Emoji Policy**: No emojis in source code, documentation, UI strings, commit messages, or pull request descriptions. Use clean ASCII symbols, structured markdown tables, or typography instead.
2. **Deterministic & Private**: No analytics trackers, no telemetry, no third-party behavioral profiling.
3. **High-Fidelity Audio Integrity**: Any changes to audio pipeline must preserve 320 kbps streaming fidelity and avoid unnecessary transcoding.
4. **Architectural Separation**: Maintain clean boundaries between presentation (Jetpack Compose), audio service (AndroidX Media3), and data layer (Room DB / DataStore).

---

## 3. Development Setup

### Prerequisites
* JDK 17 or higher
* Android SDK (API 34+, Build Tools 34+)
* Git
* Android Studio Ladybug (or newer)

### Cloning the Repository
```bash
git clone https://github.com/Varshuai/A1Swaara.git
cd A1Swaara
git submodule update --init --recursive
```

### Compiling & Assembling
```bash
# Debug build
./gradlew :app:assembleHttp3Debug

# Release build
./gradlew :app:assembleHttp3Release
```

---

## 4. Branching & Commit Conventions

### Branch Naming
* `feature/<feature-name>` for new features
* `fix/<bug-description>` for bug fixes
* `refactor/<scope>` for code refactoring
* `docs/<topic>` for documentation improvements

### Commit Messages
Follow Conventional Commits without emojis:
```
feat(player): add dynamic loop animation and badge indicator
fix(downloader): decouple streaming cache from persistent downloads
refactor(library): optimize room database query performance
docs(readme): update build instructions for windows and linux
```

---

## 5. Pull Request Process

1. Fork the repository and create your branch from `main`.
2. Ensure your changes compile cleanly without warnings or lint errors.
3. Test your changes on an Android device or emulator (Android 7.0+ / API 24+).
4. Verify that offline downloads, player controls, and background playback remain fully operational.
5. Submit a pull request to `main` with a clear explanation of what was changed, why, and how to verify it.

---

## 6. Reporting Bugs & Proposing Features

* Check existing issues before opening a new one to avoid duplicates.
* When reporting bugs, include device model, Android version, app version, and steps to reproduce.
* When suggesting features, explain the use case and how it benefits the A1 Swaara community.

Thank you for helping build a better, sovereign audio experience!
