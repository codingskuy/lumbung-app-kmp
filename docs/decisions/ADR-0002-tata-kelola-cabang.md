# ADR-0002 — Tata kelola cabang dan alur PR

- Status: Diterima
- Tanggal: 2026-10-21

## Konteks

Kontributor (manusia dan agent AI) bekerja paralel. Tanpa alur seragam, riwayat `main` menjadi tidak terlacak dan tinjauan manusia mudah terlewat.

## Keputusan

1. Kontribusi lewat fork; branch dari `upstream/main`, satu branch = satu tiket = satu PR.
2. Penamaan branch: `feat|fix|test|docs|chore/<kode-tiket-huruf-kecil>-<slug>`.
3. Atomic commit, conventional commits berbahasa Indonesia, dengan trailer `Co-Authored-By` bila dibantu AI.
4. PR memakai `.github/pull_request_template.md` dan `Closes #N`; penutupan issue terjadi saat merge oleh manusia.
5. `.github/CODEOWNERS` menetapkan `@Coding-Skuy` sebagai pemilik default.
6. Setiap PR memperbarui `CHANGELOG.md`.

## Usulan proteksi branch `main` (hanya usulan, belum diterapkan)

- Wajib PR dengan minimal 1 persetujuan dari pemilik kode.
- Wajib status check CI lulus (build + uji unit) sebelum merge.
- Larang force-push dan penghapusan `main`.
- Wajib riwayat linear (squash atau rebase merge).

Penerapan pengaturan repo dilakukan oleh admin organisasi, bukan lewat PR ini.

## Konsekuensi

- Alur kontribusi konsisten dan dapat diaudit.
- Perubahan pengaturan repo membutuhkan tindakan admin terpisah.
