# GitHub Workflows

This directory contains automated workflows for the PluginLangCore project.

## Workflows

### 1. Release Workflow (`release.yml`)

**Trigger:** Automatically runs on push to `main` or `master` branch, or can be triggered manually.

**What it does:**
1. Extracts the version from `build.gradle`
2. Checks if a tag with that version already exists
3. If the tag doesn't exist:
   - Builds the project with Gradle
   - Creates a Git tag (e.g., `v1.0.0`)
   - Creates a GitHub Release with all built artifacts:
     - Main JAR
     - Sources JAR
     - Javadoc JAR
4. If the tag exists, skips the release process

**Usage:**
- Simply push your changes to the main branch
- Or trigger manually from the Actions tab

### 2. Version Bump Workflow (`version-bump.yml`)

**Trigger:** Manual workflow dispatch with version input.

**What it does:**
1. Updates the version in `build.gradle`
2. Commits and pushes the change
3. Automatically triggers the release workflow

**Usage:**
1. Go to the Actions tab in your GitHub repository
2. Select "Version Bump" workflow
3. Click "Run workflow"
4. Enter the new version (e.g., `1.0.1`, `1.1.0`, `2.0.0`)
5. Click "Run workflow" button

## Workflow Process

### Automatic Release Process:
```
Change version in build.gradle → Commit & Push → Release workflow runs → Tag & Release created
```

### Manual Version Bump Process:
```
Run Version Bump workflow → Enter new version → Version updated & committed → Release workflow runs → Tag & Release created
```

## Important Notes

1. **Duplicate Releases:** The workflow checks if a tag already exists to prevent duplicate releases.

2. **Permissions:** The workflows use `GITHUB_TOKEN` which is automatically provided by GitHub Actions. Make sure your repository settings allow GitHub Actions to create releases.

3. **Branch Protection:** If you have branch protection rules, you may need to allow the GitHub Actions bot to push to the protected branch.

4. **Version Format:** Keep the version format consistent in `build.gradle`:
   ```groovy
   version = '1.0.0'
   ```

## Troubleshooting

### Workflow doesn't trigger
- Check if the workflow file is on the correct branch (`main` or `master`)
- Verify repository settings allow GitHub Actions

### Can't create tags or releases
- Go to Settings → Actions → General → Workflow permissions
- Select "Read and write permissions"
- Check "Allow GitHub Actions to create and approve pull requests"

### Version not extracted correctly
- Ensure the version line in `build.gradle` follows the exact format:
  ```groovy
  version = '1.0.0'
  ```
  (Single quotes, no extra spaces)

## Examples

### Example 1: Regular Release
1. Update version in `build.gradle` to `1.0.1`
2. Commit: `git commit -am "chore: bump version to 1.0.1"`
3. Push: `git push`
4. Workflow automatically creates tag `v1.0.1` and release

### Example 2: Using Version Bump Workflow
1. Go to Actions → Version Bump → Run workflow
2. Enter version: `1.1.0`
3. Workflow updates `build.gradle`, commits, and triggers release

### Example 3: Manual Trigger
1. Go to Actions → Create Release → Run workflow
2. Select branch
3. Run workflow (uses current version in `build.gradle`)

