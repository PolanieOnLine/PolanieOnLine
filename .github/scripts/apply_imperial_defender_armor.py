from pathlib import Path
import re

path = Path('data/conf/creatures/stendhal/human_imperial.xml')
text = path.read_text(encoding='utf-8')

name = 'obrońca imperium'
pattern = re.compile(
    r'(<creature name="' + re.escape(name) + r'">.*?<attributes>.*?<def value="[^"]+"/>)(?!\s*<armor )',
    re.DOTALL,
)
text, count = pattern.subn(r'\1\n\t\t\t<armor value="heavy"/>', text, count=1)
if count != 1:
    raise SystemExit(f'Expected exactly one unclassified creature named {name!r}, got {count}')

path.write_text(text, encoding='utf-8')
