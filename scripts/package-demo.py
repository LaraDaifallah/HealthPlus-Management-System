"""Bundle the compiled app, platform runtime libraries, launchers and demo SQL."""
from pathlib import Path
import shutil
import sys
import zipfile

root = Path(__file__).resolve().parents[1]
platform = sys.argv[1] if len(sys.argv) > 1 else sys.platform
destination = root / 'target' / f'HealthPlus-{platform}'
if destination.exists():
    shutil.rmtree(destination)
destination.mkdir(parents=True)
shutil.copy2(root / 'target/healthplus.jar', destination / 'healthplus.jar')
shutil.copytree(root / 'target/lib', destination / 'lib')
shutil.copytree(root / 'database', destination / 'database')
for source in (root / 'launchers').iterdir():
    shutil.copy2(source, destination / source.name)
shutil.copy2(root / 'README.md', destination / 'README.md')
(destination / 'start-healthplus.sh').chmod(0o755)
archive = destination.with_suffix('.zip')
with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED) as bundle:
    for source in sorted(destination.rglob('*')):
        if source.is_file():
            bundle.write(source, source.relative_to(destination.parent))
print(archive)
