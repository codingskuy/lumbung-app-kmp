# Panduan Kontribusi — lumbung-app-kmp

Baca `AGENTS.md` dan seluruh ADR di `docs/decisions/` sebelum menulis kode. Requirement hidup di Lumbung-TownHall, bukan di repo ini.

## Alur: fork → branch → atomic commit → PR

1. **Fork** repo ke akun sendiri: `gh repo fork Coding-Skuy/lumbung-app-kmp --clone` lalu pastikan remote `upstream` menunjuk repo org.
2. **Branch** dari `upstream/main` (satu branch = satu tiket = satu PR):
   `git fetch upstream && git switch -c feat/lmb-kmp-002-shared-core upstream/main`
   Awalan: `feat/`, `fix/`, `test/`, `docs/`, `chore/`; sisanya kode tiket huruf kecil + slug.
3. **Atomic commit**: satu perubahan logis per commit, conventional commits berbahasa Indonesia
   (`feat: ...`, `fix: ...`, `test: ...`, `docs: ...`, `chore: ...`). Setiap PR wajib memperbarui `CHANGELOG.md`.
4. **Push ke fork**, lalu buka PR ke `Coding-Skuy/lumbung-app-kmp` base `main`
   (`gh pr create --head <akun>:<branch>`). Isi template PR; cantumkan `Closes #N`.
5. **Tinjauan**: PR membutuhkan persetujuan pemilik kode (`.github/CODEOWNERS`). Jangan merge sendiri, jangan force-push ke `main`.

## Aturan

- TDD merah-hijau; tidak ada tes yang di-skip.
- Jangan commit rahasia (`.env`, kunci, token). Token tidak boleh dicetak di log.
- Dokumen `versions/v*` dan ADR yang sudah Diterima bersifat immutable (lihat `docs/decisions/README.md`).
- Perubahan kontrak API/event = bump versi + ADR baru. Perubahan backend = issue di `lumbung-backend-service`, bukan PR dari repo ini.
- Usulan proteksi branch `main` ada di ADR-0002.
